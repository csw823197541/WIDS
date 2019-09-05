package com.wids.service.process;

import com.wids.manager.data.WIData;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDefaultValue;
import com.wids.model.domain.WIDomain;
import com.wids.model.log.Logger;
import com.wids.model.vessel.VMMachine;
import com.wids.model.wi.WIConfiguration;
import com.wids.model.wi.WICrane;
import com.wids.model.wi.WICraneMaintainPlan;
import com.wids.model.wi.WIWorkBlock;
import com.wids.service.method.PublicMethod;
import com.wids.service.method.WIDataMethod;
import com.wids.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2018/1/18.
 * Description:
 */
public class WIBlockProcess {

    private WIDataMethod wiDataMethod;

    public WIBlockProcess(WIDataMethod wiDataMethod) {
        this.wiDataMethod = wiDataMethod;
    }

    public void processWIBlock(WIData wiData) {
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            analyzeWIBlockWorkTimeInWICrane(wiCrane, wiData);
            analyzeWIBlockWorkTimeByCraneMaintainPlan(wiCrane, wiData);
        }
        List<WIWorkBlock> wiWorkBlockList = wiData.getAllWIWorkBlockList();
        if (wiWorkBlockList.size() > 0) {
            //从所有作业块中开始时间最早的块开始，对每部桥机的作业块进行递归遍历处理
            PublicMethod.sortWIWorkBlockByEstimateStartTime(wiWorkBlockList);
            long firstTime = wiWorkBlockList.get(0).getEstimateStartTime().getTime();
            search(1, firstTime, wiCraneList, wiData);
        }
    }

    private void analyzeWIBlockWorkTimeInWICrane(WICrane wiCrane, WIData wiData) {
        long estimateWorkST = wiCrane.getActualWorkST().getTime();
//        long sentWiWorkTime = wiCrane.getSentWiWorkTime();
        List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
        for (int i = 0; i < wiWorkBlockList.size(); i++) {
            WIWorkBlock wiWorkBlock = wiWorkBlockList.get(i);
            long spanTime = 0;
            if (i > 0) {
                WIWorkBlock wiWorkBlockFront = wiWorkBlockList.get(i - 1);
                spanTime = analyzeTwoWIWorkBlock(wiWorkBlock, wiWorkBlockFront, wiData);
            }
            long workTime = wiWorkBlock.getBlockWorkTime(); //当块没有指令时，作业时间为0
            estimateWorkST += spanTime;
            if (wiWorkBlock.getRemainAmount() > 0 && wiWorkBlock.getSentAmount() > 0) {
                //remainAmount>0意味着作业块有未发送的指令，sentAmount>0意味着该作业块发送过指令
                long blockSentWiWorkTime = wiWorkBlock.getSentWiWorkTime(); //作业块发送指令的时间
                wiWorkBlock.setSentWIStartTime(new Date(estimateWorkST + blockSentWiWorkTime));
            } else {
                wiWorkBlock.setSentWIStartTime(new Date(estimateWorkST));
            }
            wiWorkBlock.setEstimateStartTime(new Date(estimateWorkST));
            estimateWorkST += workTime;
            wiWorkBlock.setEstimateEndTime(new Date(estimateWorkST));
        }
    }

    private long analyzeTwoWIWorkBlock(WIWorkBlock wiWorkBlock, WIWorkBlock wiWorkBlockFront, WIData wiData) {
        long moveTime = 0;
        List<VMMachine> vmMachineList = wiDataMethod.getVesselData().getAllVMMachineList();
        WIConfiguration wiConfiguration = wiData.getWiConfiguration();
        for (VMMachine vmMachine : vmMachineList) {
            boolean cross1 = wiWorkBlock.getCranePosition().compareTo(vmMachine.getMachinePosition()) < 0 && wiWorkBlockFront.getCranePosition().compareTo(vmMachine.getMachinePosition()) > 0;
            boolean cross2 = wiWorkBlock.getCranePosition().compareTo(vmMachine.getMachinePosition()) > 0 && wiWorkBlockFront.getCranePosition().compareTo(vmMachine.getMachinePosition()) < 0;
            if (wiConfiguration.getCrossBridge() && CWPDomain.MACHINE_TYPE_BRIDGE.equals(vmMachine.getMachineType())) {
                if (cross1 || cross2) {
                    moveTime = wiConfiguration.getCrossBarTime();
                }
            }
            if (wiConfiguration.getCrossChimney() && CWPDomain.MACHINE_TYPE_CHIMNEY.equals(vmMachine.getMachineType())) {
                if (cross1 || cross2) {
                    moveTime = wiConfiguration.getCrossBarTime();
                }
            }
        }
        return moveTime;
    }

    private void analyzeWIBlockWorkTimeByCraneMaintainPlan(WICrane wiCrane, WIData wiData) {
        String craneNo = wiCrane.getCraneNo();
        WICraneMaintainPlan wiCraneMaintainPlan = wiDataMethod.getWICraneMaintainPlanByCraneNo(craneNo);
        if (wiCraneMaintainPlan != null) {
            long addTime = wiCraneMaintainPlan.getMaintainEndTime().getTime() - wiCraneMaintainPlan.getMaintainStartTime().getTime();
            List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
            PublicMethod.sortWIWorkBlockByEstimateStartTime(wiWorkBlockList);
            for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
                if (wiCraneMaintainPlan.getMaintainStartTime().compareTo(wiWorkBlock.getEstimateStartTime()) > 0
                        && wiCraneMaintainPlan.getMaintainStartTime().compareTo(wiWorkBlock.getEstimateEndTime()) < 0) {
                    moveWIWorkBlockTime(null, addTime, wiWorkBlock, wiData);
                    break;
                } else if (wiCraneMaintainPlan.getMaintainStartTime().compareTo(wiWorkBlock.getEstimateStartTime()) <= 0) {
                    moveWIWorkBlockTime(WIDomain.SELF, addTime, wiWorkBlock, wiData);
                    break;
                }
            }
        }
    }

    private void search(int depth, long curTime, List<WICrane> wiCraneList, WIData wiData) {
        Logger logger = wiData.getLogger();
        logger.logDebug("第" + depth++ + "次search：----------------------");

        List<WIWorkBlock> curWiWorkBlocks = new ArrayList<>();
        long minWorkTime = getCurrentWIWorkBlock(curTime, wiCraneList, curWiWorkBlocks); //当前同一时刻要同时考虑的作业块，一般一部桥机一个作业块
        if (curWiWorkBlocks.size() == 0) { //递归结束
            return;
        }

        //默认第一次递归时，桥机安全距离是足够，但是可能会有异常数据发生
        if (changeWIBlockWorkTime(curWiWorkBlocks, wiData)) {
            curWiWorkBlocks.clear();
            minWorkTime = getCurrentWIWorkBlock(curTime, wiCraneList, curWiWorkBlocks);
        }
        if (curWiWorkBlocks.size() == 0) {
            return;
        }

        //分析此次递归中，所有桥机同时刻作业的后续作业块之间，桥机安全距离是否足够，决定作业块之间如何避让
        changeWIBlockWorkTime(curTime, minWorkTime, curWiWorkBlocks, wiData);

        curTime += minWorkTime;
        search(depth, curTime, wiCraneList, wiData);
    }

    private long getCurrentWIWorkBlock(long curTime, List<WICrane> wiCraneList, List<WIWorkBlock> curWiWorkBlocks) {
        long minWorkTime = Long.MAX_VALUE;
        for (WICrane wiCrane : wiCraneList) {
            List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
            for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
                long st = wiWorkBlock.getEstimateStartTime().getTime();
                long et = wiWorkBlock.getEstimateEndTime().getTime();
                if (st > curTime + WIDefaultValue.moveTime) { //如果作业块开始时间在当前时间之后，不放入此次递归考虑中，但是此次递归要在该时刻结束
                    minWorkTime = Math.min(minWorkTime, st - curTime);
                }
                if (curTime + WIDefaultValue.moveTime >= st && curTime < et) {
                    curWiWorkBlocks.add(wiWorkBlock);
//                    long workTime = wiWorkBlock.getBlockWorkTime();
                    long workTime = et - curTime;
                    minWorkTime = Math.min(minWorkTime, workTime); //找到所有桥机作业块中，作业时间最小的作业块
                    break;
                }
            }
        }
        return minWorkTime;
    }

    private boolean changeWIBlockWorkTime(List<WIWorkBlock> curWiWorkBlocks, WIData wiData) {
        boolean change = false;
        int size = curWiWorkBlocks.size();
        for (int i = 0; i < size; i++) {
            WIWorkBlock wiWorkBlock = curWiWorkBlocks.get(i);
            if (i + 1 < size) {
                WIWorkBlock wiWorkBlockRight = curWiWorkBlocks.get(i + 1);
                double distance = CalculateUtil.sub(wiWorkBlockRight.getCranePosition(), wiWorkBlock.getCranePosition());
                if (distance < 2 * wiData.getWiConfiguration().getCraneSafeSpan()) { //安全距离不够
                    long addTimeC = wiWorkBlockRight.getEstimateEndTime().getTime() - wiWorkBlock.getEstimateStartTime().getTime(); //??????
                    long addTimeR = wiWorkBlock.getEstimateEndTime().getTime() - wiWorkBlockRight.getEstimateStartTime().getTime();
                    int compare = wiWorkBlock.getEstimateStartTime().compareTo(wiWorkBlockRight.getEstimateStartTime());
                    if (compare > 0) {
                        moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                    } else if (compare < 0) {
                        moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
                    } else {
                        WICrane wiCrane = wiData.getWICraneByCraneNo(wiWorkBlock.getCraneNo());
                        if (distance < 0.0 || !wiCrane.craneCanWork()) { //不能跨过旁边的桥机、桥机暂时不能作业，自己往后推
                            moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                        } else if (wiWorkBlock.getHatchId().equals(wiWorkBlockRight.getHatchId())) {
                            //两个作业块在同一个舱，出现安全距离问题，则按舱序来
                            if (wiWorkBlock.getHatchSeq() < wiWorkBlockRight.getHatchSeq()) {
                                moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
                            } else {
                                moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                            }
                        } else {
//                            if (wiWorkBlock.getBlockWorkTime() <= wiWorkBlockRight.getBlockWorkTime()) {
//                                moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
//                            } else {
//                                moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
//                            }
                            if (wiWorkBlock.getWorkingStartTime().compareTo(wiWorkBlockRight.getWorkingStartTime()) < 0) {
                                moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
                            } else if (wiWorkBlock.getWorkingStartTime().compareTo(wiWorkBlockRight.getWorkingStartTime()) == 0){
                                if (wiWorkBlock.getBlockWorkTime() <= wiWorkBlockRight.getBlockWorkTime()) {
                                    moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
                                } else {
                                    moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                                }
                            } else {
                                moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                            }
                        }
                    }
                    change = true;
                }
            }
        }
        return change;
    }

    private void changeWIBlockWorkTime(long curTime, long minWorkTime, List<WIWorkBlock> curWiWorkBlocks, WIData wiData) {
        int size = curWiWorkBlocks.size();
        for (int i = 0; i < size; i++) {
            WIWorkBlock wiWorkBlock = curWiWorkBlocks.get(i);
            WIWorkBlock wiWorkBlockNext = getNextWIWorkBlock(curTime, minWorkTime, wiWorkBlock, wiData); //后一个作业块
            if (wiWorkBlockNext != null && !wiWorkBlockNext.getBlockKey().equals(wiWorkBlock.getBlockKey())) { //不是同一个作业块
                if (wiWorkBlockNext.getCranePosition().compareTo(wiWorkBlock.getCranePosition()) < 0) { //下一个作业块左移，考虑左边一个作业块
                    if (i - 1 >= 0) {
                        WIWorkBlock wiWorkBlockLeft = curWiWorkBlocks.get(i - 1);
                        long addTime = analyzeTwoWIWorkBlock(WIDomain.LEFT, wiWorkBlockNext, wiWorkBlockLeft, wiData);
                        //将当前桥机的后续作业块时间依次往后移动
                        moveWIWorkBlockTime(null, addTime, wiWorkBlock, wiData);
                    }
                } else { //下一个作业块右移，考虑右边一个作业块
                    if (i + 1 < size) {
                        WIWorkBlock wiWorkBlockRight = curWiWorkBlocks.get(i + 1);
                        long addTime = analyzeTwoWIWorkBlock(WIDomain.RIGHT, wiWorkBlockNext, wiWorkBlockRight, wiData);
                        moveWIWorkBlockTime(null, addTime, wiWorkBlock, wiData);
                    }
                }
            }
        }
    }

    private WIWorkBlock getNextWIWorkBlock(long curTime, long minWorkTime, WIWorkBlock wiWorkBlock, WIData wiData) {
        if (curTime + minWorkTime < wiWorkBlock.getEstimateEndTime().getTime()) {
            return wiWorkBlock;
        } else {
            WICrane wiCrane = wiData.getWICraneByCraneNo(wiWorkBlock.getCraneNo());
            if (wiCrane != null) {
                List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
                PublicMethod.sortWIWorkBlockByCraneSeq(wiWorkBlockList);
                for (int i = 0; i < wiWorkBlockList.size(); i++) {
                    WIWorkBlock wiWorkBlock1 = wiWorkBlockList.get(i);
                    if (wiWorkBlock1.getCraneSeq().equals(wiWorkBlock.getCraneSeq())) {
                        if (i + 1 < wiWorkBlockList.size()) {
                            return wiWorkBlockList.get(i + 1);
                        }
                    }
                }
            }
        }
        return null;
    }

    private long analyzeTwoWIWorkBlock(String side, WIWorkBlock wiWorkBlockNext, WIWorkBlock wiWorkBlockBeside, WIData wiData) {
        long addTime = 0;
        double distance = 2 * wiData.getWiConfiguration().getCraneSafeSpan() + 1.0;
        if (WIDomain.LEFT.equals(side)) {
            distance = CalculateUtil.sub(wiWorkBlockNext.getCranePosition(), wiWorkBlockBeside.getCranePosition());
        }
        if (WIDomain.RIGHT.equals(side)) {
            distance = CalculateUtil.sub(wiWorkBlockBeside.getCranePosition(), wiWorkBlockNext.getCranePosition());
        }
        if (distance < 2 * wiData.getWiConfiguration().getCraneSafeSpan()) { //安全距离不够
            addTime = wiWorkBlockBeside.getEstimateEndTime().getTime() - wiWorkBlockNext.getEstimateStartTime().getTime();
        }
        return addTime;
    }

    private void moveWIWorkBlockTime(String self, long addTime, WIWorkBlock wiWorkBlock, WIData wiData) {
        if (addTime > 0) {
            if (WIDomain.SELF.equals(self)) {
                wiWorkBlock.setEstimateStartTime(new Date(wiWorkBlock.getEstimateStartTime().getTime() + addTime));
                wiWorkBlock.setEstimateEndTime(new Date(wiWorkBlock.getEstimateEndTime().getTime() + addTime));
            }
            WICrane wiCrane = wiData.getWICraneByCraneNo(wiWorkBlock.getCraneNo());
            if (wiCrane != null) {
                List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
                for (WIWorkBlock wiWorkBlock1 : wiWorkBlockList) {
                    if (wiWorkBlock1.getCraneSeq().compareTo(wiWorkBlock.getCraneSeq()) > 0) { //判断移动后面的作业块
                        wiWorkBlock1.setEstimateStartTime(new Date(wiWorkBlock1.getEstimateStartTime().getTime() + addTime));
                        wiWorkBlock1.setEstimateEndTime(new Date(wiWorkBlock1.getEstimateEndTime().getTime() + addTime));
                    }
                }
            }
        }
    }
}
