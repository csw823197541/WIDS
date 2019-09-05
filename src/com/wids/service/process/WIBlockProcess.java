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

        if (changeWIBlockWorkTime(curWiWorkBlocks, wiData)) {
            curWiWorkBlocks.clear();
            minWorkTime = getCurrentWIWorkBlock(curTime, wiCraneList, curWiWorkBlocks);
        }
        if (curWiWorkBlocks.size() == 0) {
            return;
        }

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
                if (st > curTime + WIDefaultValue.moveTime) {
                    minWorkTime = Math.min(minWorkTime, st - curTime);
                }
                if (curTime + WIDefaultValue.moveTime >= st && curTime < et) {
                    curWiWorkBlocks.add(wiWorkBlock);
//                    long workTime = wiWorkBlock.getBlockWorkTime();
                    long workTime = et - curTime;
                    minWorkTime = Math.min(minWorkTime, workTime);
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
                if (distance < 2 * wiData.getWiConfiguration().getCraneSafeSpan()) {
                    long addTimeC = wiWorkBlockRight.getEstimateEndTime().getTime() - wiWorkBlock.getEstimateStartTime().getTime(); //??????
                    long addTimeR = wiWorkBlock.getEstimateEndTime().getTime() - wiWorkBlockRight.getEstimateStartTime().getTime();
                    int compare = wiWorkBlock.getEstimateStartTime().compareTo(wiWorkBlockRight.getEstimateStartTime());
                    if (compare > 0) {
                        moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                    } else if (compare < 0) {
                        moveWIWorkBlockTime(WIDomain.SELF, addTimeR, wiWorkBlockRight, wiData);
                    } else {
                        WICrane wiCrane = wiData.getWICraneByCraneNo(wiWorkBlock.getCraneNo());
                        if (distance < 0.0 || !wiCrane.craneCanWork()) {
                            moveWIWorkBlockTime(WIDomain.SELF, addTimeC, wiWorkBlock, wiData);
                        } else if (wiWorkBlock.getHatchId().equals(wiWorkBlockRight.getHatchId())) {
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
            WIWorkBlock wiWorkBlockNext = getNextWIWorkBlock(curTime, minWorkTime, wiWorkBlock, wiData);
            if (wiWorkBlockNext != null && !wiWorkBlockNext.getBlockKey().equals(wiWorkBlock.getBlockKey())) {
                if (wiWorkBlockNext.getCranePosition().compareTo(wiWorkBlock.getCranePosition()) < 0) {
                    if (i - 1 >= 0) {
                        WIWorkBlock wiWorkBlockLeft = curWiWorkBlocks.get(i - 1);
                        long addTime = analyzeTwoWIWorkBlock(WIDomain.LEFT, wiWorkBlockNext, wiWorkBlockLeft, wiData);
                        moveWIWorkBlockTime(null, addTime, wiWorkBlock, wiData);
                    }
                } else {
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
        if (distance < 2 * wiData.getWiConfiguration().getCraneSafeSpan()) {
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
                    if (wiWorkBlock1.getCraneSeq().compareTo(wiWorkBlock.getCraneSeq()) > 0) {
                        wiWorkBlock1.setEstimateStartTime(new Date(wiWorkBlock1.getEstimateStartTime().getTime() + addTime));
                        wiWorkBlock1.setEstimateEndTime(new Date(wiWorkBlock1.getEstimateEndTime().getTime() + addTime));
                    }
                }
            }
        }
    }
}
