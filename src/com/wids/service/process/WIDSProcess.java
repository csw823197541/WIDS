package com.wids.service.process;

import com.wids.manager.data.AllData;
import com.wids.manager.data.VesselData;
import com.wids.manager.data.WIData;
import com.wids.manager.method.GenerateResultMethod;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDomain;
import com.wids.model.log.Logger;
import com.wids.model.wi.*;
import com.wids.service.method.*;
import com.wids.service.optimizer.WIOptimizer;

import java.util.*;

/**
 * Created by csw on 2017/10/11.
 * Description:
 */
public class WIDSProcess {

    private WIData wiData;
    private WIDataMethod wiDataMethod;

    private Logger logger;

    public WIDSProcess(WIData wiData, VesselData vesselData, AllData allData) {
        this.wiData = wiData;
        wiDataMethod = new WIDataMethod(vesselData, allData);
        logger = wiData.getLogger();
    }

    public void processWIDS() {

        wiDataMethod.generateData(wiData);

        analyzeCraneWithSentWICraneMove(wiData);

        analyzeCraneWithWIWorkBlock(wiData);

        changeWIBlockWorkTimeBeforeGetWaitToSendWI(wiData, wiDataMethod);

        analyzeWIWorkBlockByIntervalTime(wiData, wiDataMethod);

        GenerateResultMethod.generateWI(wiData);

        GenerateResultMethod.generateWorkBlock(wiData.getAllWIWorkBlockList(), wiData.getSmartWiResults().getSmartReCwpBlockInfoList());

    }

    private void analyzeCraneWithSentWICraneMove(WIData wiData) {
        long intervalTime = wiData.getWiConfiguration().getIntervalTime() * 60000;//毫秒，ms
        logger.logInfo("分析当前时刻，桥机后(" + intervalTime / 60000 + ")分钟中该发送哪些指令...");
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            String craneNo = wiCrane.getCraneNo();
            List<WICraneMove> wiCraneMoveList = wiData.getSentWICraneMoveListByCraneNo(craneNo);
            long sentWiWorkTime = 0; //已经发送的指令作业耗时，ms
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                long workTime = wiCraneMove.getCntWorkTime() * 1000;
                sentWiWorkTime += workTime;
            }
            wiCrane.setSentWiWorkTime(sentWiWorkTime);
            if (sentWiWorkTime >= intervalTime) {
                wiCrane.setRemainWiWorkTime(0L);
                logger.logInfo("当前时刻，桥机(craneNo:" + craneNo + ")指令队列中还有" + sentWiWorkTime / 60000 + "分钟的指令，超过" + intervalTime / 60000 + "分钟，该桥机指令暂时不发送。");
            } else {
                wiCrane.setRemainWiWorkTime(intervalTime - sentWiWorkTime);
                logger.logInfo("当前时刻，桥机(craneNo:" + craneNo + ")还能容纳的指令作业时间：" + (intervalTime - sentWiWorkTime) / 60000 + "分钟。");
            }
        }
    }

    private void analyzeCraneWithWIWorkBlock(WIData wiData) {
        List<Long> hatchIdList = wiDataMethod.getVesselData().getAllVMHatchIdList();
        for (Long hatchId : hatchIdList) {
            List<WIWorkBlock> wiWorkBlockList = wiData.getWIWorkBlockListByHatchId(hatchId);
            PublicMethod.sortWIWorkBlockByHatchSeq(wiWorkBlockList);
            List<WICraneMove> waitWICraneMoveList = wiData.getWaitWICraneMoveListByHatchId(hatchId); //该舱待发指令
            List<WICraneMove> sentWICraneMoveLIst = wiData.getSentWICraneMoveListByHatchId(hatchId); //该舱已发指令（队列、作业中）
            PublicMethod.sortWICraneMoveByMoveOrder(waitWICraneMoveList);
            PublicMethod.sortWICraneMoveByMoveOrder(sentWICraneMoveLIst);
            int n = 0, m = 0; //依次给这个舱内剩余的指令进行绑定
            for (WIWorkBlock wiWorkBlock : wiWorkBlockList) { //TODO：该方法需要给出正确舱块和舱序
                try {
                    long remainAmount = wiWorkBlock.getRemainAmount();
                    long count = 0;
                    int i = n;
                    for (; i < waitWICraneMoveList.size(); i++) {
                        WICraneMove wiCraneMove = waitWICraneMoveList.get(i);
                        if (wiCraneMove.getSelectedWorkBlock() == null) {
                            if (count == remainAmount) {
                                break;
                            }
                            if (wiCraneMove.getWorkBayNo().equals(Integer.valueOf(wiWorkBlock.getBayNo()))) {
                                count += 1;
                                wiWorkBlock.getWiCraneMoveList().add(wiCraneMove);
                                wiCraneMove.setSelectedWorkBlock(wiWorkBlock.getBlockKey()); //该关箱子已被该作业块选中
                                n = i + 1;
                            }
                        }
                    }
                    //绑定已发(队列、作业)指令给作业块，完成状态的指令不考虑
                    int j = m;
                    for (; j < sentWICraneMoveLIst.size(); j++) {
                        WICraneMove wiCraneMove = sentWICraneMoveLIst.get(j);
                        if (wiCraneMove.getSelectedWorkBlock() == null) {
                            String craneNo = wiCraneMove.getCraneNo();
                            if (craneNo != null) {
                                if (craneNo.equals(wiWorkBlock.getCraneNo()) && wiCraneMove.getWorkBayNo().equals(Integer.valueOf(wiWorkBlock.getBayNo()))) {
                                    wiWorkBlock.getWiCraneMoveList().add(wiCraneMove);
                                    wiCraneMove.setSelectedWorkBlock(wiWorkBlock.getBlockKey()); //该关箱子已被该作业块选中
                                    m = j + 1;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.logError("给每个作业块(blockKey:" + wiWorkBlock.getBlockKey() + ")绑定箱指令时发生异常(可能是已发的指令中没有桥机号)！");
                    e.printStackTrace();
                }
            }

            //统计箱区指令数目
            WIAreaAbility wiAreaAbilityTemp;
            WIContainer wiContainerTemp;
            List<WICraneMove> wiCraneMoveList = new ArrayList<>();
            wiCraneMoveList.addAll(waitWICraneMoveList);
            wiCraneMoveList.addAll(sentWICraneMoveLIst);
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                if (CWPDomain.DL_TYPE_LOAD.equals(wiCraneMove.getLdFlag())) {
                    for (WICraneContainer wiCraneContainer : wiCraneMove.getWiCraneContainerList()) {
                        wiContainerTemp = wiCraneContainer.getOriginalContainer();
                        if (wiContainerTemp.getAreaNo() != null) {
                            wiAreaAbilityTemp = wiData.getWIAreaAbilityByAreaNo(wiContainerTemp.getAreaNo());
                            if (PublicMethod.isUnSentStatus(wiContainerTemp.getWorkStatus())) {
                                if (wiAreaAbilityTemp != null) {
                                    wiAreaAbilityTemp.setAllTaskNum(wiAreaAbilityTemp.getAllTaskNum() + 1);
                                    wiAreaAbilityTemp.getUnSendCntList().add(wiContainerTemp);
                                } else {
                                    WIAreaAbility wiAreaAbility = new WIAreaAbility(wiContainerTemp.getAreaNo());
                                    wiAreaAbility.setAllTaskNum(1);
                                    wiAreaAbility.getUnSendCntList().add(wiContainerTemp);
                                    wiData.addWIAreaAbility(wiAreaAbility);
                                }
                            } else if (PublicMethod.isSentStatus(wiContainerTemp.getWorkStatus()) &&
                                    PublicMethod.areaMoveStage(wiContainerTemp.getMoveStage())) { //已发送，箱子还在yard里面
                                if (wiAreaAbilityTemp != null) {
                                    wiAreaAbilityTemp.setAllTaskNum(wiAreaAbilityTemp.getAllTaskNum() + 1);
                                    wiAreaAbilityTemp.getSentCntList().add(wiContainerTemp);
                                } else {
                                    WIAreaAbility wiAreaAbility = new WIAreaAbility(wiContainerTemp.getAreaNo());
                                    wiAreaAbility.setAllTaskNum(1);
                                    wiAreaAbility.getSentCntList().add(wiContainerTemp);
                                    wiData.addWIAreaAbility(wiAreaAbility);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        StringBuilder blockMessage = new StringBuilder();
        long time = 0;
        for (WICrane wiCrane : wiCraneList) {
            String craneNo = wiCrane.getCraneNo();
            List<WIWorkBlock> wiWorkBlockList = wiData.getWIWorkBlockListByCraneNo(craneNo);
            PublicMethod.sortWIWorkBlockByCraneSeq(wiWorkBlockList);
            wiCrane.setWiWorkBlockList(wiWorkBlockList);
            //判断作业块的量是否与实际指令数目符合
            for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
                if (time > wiData.getWiConfiguration().getIntervalTime() * 120000) {
                    break;
                }
                time += wiWorkBlock.getBlockWorkTime();
                //可作业的指令数目（剔除装船状态为P的指令）与 作业块剩余需要发送的指令数目相同
                long num = 0;
                for (WICraneMove wiCraneMove : wiWorkBlock.getWiCraneMoveList()) {
                    if (CWPDomain.DL_TYPE_LOAD.equals(wiCraneMove.getLdFlag()) && PublicMethod.canLoadState(wiCraneMove.getWorkStatus())) {
                        num++;
                    } else if (CWPDomain.DL_TYPE_DISC.equals(wiCraneMove.getLdFlag())) {
                        num++;
                    }
                }
                if (wiWorkBlock.getRemainAmount() - num > 0) {
                    blockMessage.append("桥机(craneNo:").append(wiWorkBlock.getCraneNo()).append(")在倍位(bayNo:").append(wiWorkBlock.getBayNo()).append(")的作业块缺少").append(wiWorkBlock.getRemainAmount() - num).append("关可作业指令");
                    wiWorkBlock.setBlockMessage(blockMessage.toString());
                    logger.logInfo(blockMessage.toString());
                }
                blockMessage.setLength(0);
            }
            time = 0;
        }
    }

    private void changeWIBlockWorkTimeBeforeGetWaitToSendWI(WIData wiData, WIDataMethod wiDataMethod) {
        logger.logInfo("分析每部桥机的作业块，根据当前时间和作业块的桥机序号进行推迟/提前处理...");
        WIBlockProcess wiBlockProcess = new WIBlockProcess(wiDataMethod);
        wiBlockProcess.processWIBlock(wiData);
    }

    private void analyzeWIWorkBlockByIntervalTime(WIData wiData, WIDataMethod wiDataMethod) {
        long intervalTime = wiData.getWiConfiguration().getIntervalTime() * 60000;//毫秒，ms
        logger.logInfo("分析每部桥机在时间间隔(" + intervalTime / 60000 + "分钟)内的作业块有哪些...");
        List<WIWorkBlock> wiWorkBlockList = wiData.getAllWIWorkBlockList();
        if (wiWorkBlockList.size() > 0) {
            PublicMethod.sortWIWorkBlockByEstimateStartTime(wiWorkBlockList);
            long firstTime = wiWorkBlockList.get(0).getEstimateStartTime().getTime();
            WIOptimizer wiOptimizer = new WIOptimizer(wiDataMethod);
            wiOptimizer.optimizeWi(firstTime, wiData);
            getCraneWIByIntervalTime(1, firstTime, intervalTime, wiData);
        }
    }

    private void getCraneWIByIntervalTime(int t, long firstTime, long intervalTime, WIData wiData) {
        Logger logger = wiData.getLogger();
        StringBuilder blockMessage = new StringBuilder();
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            String craneNo = wiCrane.getCraneNo();
            try {
                if (wiCrane.craneCanWork()) {
                    StringBuilder lockWiStr = new StringBuilder("");
                    long craneRemainWorkTime = wiCrane.getRemainWiWorkTime() * t; //桥机能容纳多长时间的指令
                    long workTime = 0;
                    List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
                    F:
                    for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
                        long st = wiWorkBlock.getSentWIStartTime().getTime();
                        if (wiWorkBlock.getEstimateStartTime().getTime() > firstTime + intervalTime) { //作业块的开始时间已经超过了要求发送指令数目
                            if (workTime <= craneRemainWorkTime) {
                                blockMessage.append("桥机(craneNo:").append(wiWorkBlock.getCraneNo()).append(")作业倍位(bayNo:").append(wiWorkBlock.getBayNo()).append(")的作业时间在(").append(intervalTime / 60000).append(")分钟以后，暂时无法提前发送后一个作业块的指令，请检查作业块");
                                wiWorkBlock.setBlockMessage(blockMessage.toString());
                                logger.logInfo(blockMessage.toString());
                                blockMessage.setLength(0);
                            }
                            break;
                        }
                        List<WICraneMove> wiCraneMoveList = wiWorkBlock.getWiCraneMoveList();
                        PublicMethod.sortWICraneMoveBySentSeq(wiCraneMoveList);
                        for (WICraneMove wiCraneMove : wiCraneMoveList) {
                            if (PublicMethod.isUnSentStatus(wiCraneMove.getWorkStatus())) {
                                if (!WIDomain.YES.equals(wiCraneMove.getCwoManualWi())) {
                                    if (CWPDomain.DL_TYPE_DISC.equals(wiCraneMove.getLdFlag()) ||
                                            (CWPDomain.DL_TYPE_LOAD.equals(wiCraneMove.getLdFlag()) && PublicMethod.canLoadState(wiCraneMove.getWorkStatus()))) {
                                        if (workTime >= craneRemainWorkTime) {
                                            break F;
                                        }
                                        //判断指令是否可以作业，即验证发出去指令的合理性
                                        List<WICraneMove> waitToSendMoveList = wiData.getWICraneMoveListFromWaitToSendMap(craneNo);
                                        if (!PublicMethod.isSelected(wiCraneMove, waitToSendMoveList)) { //避免指令重复发送
                                            wiCraneMove.setWorkingStartTime(new Date(st));
                                            st += wiCraneMove.getCntWorkTime() * 1000;
                                            wiCraneMove.setWorkingEndTime(new Date(st));
                                            wiCraneMove.setCraneNo(craneNo); //设置桥机号
                                            wiData.addWICraneMoveToWaitToSendMap(wiCraneMove);
                                            workTime += wiCraneMove.getCntWorkTime() * 1000; //ms
                                        }
                                    }
                                } else {
                                    lockWiStr.append(wiCraneMove.getWorkBayKey()).append("、");
                                }
                            }
                        }
                    }
                    if (!lockWiStr.toString().equals("")) {
                        logger.logInfo("桥机(craneNo: " + craneNo + ")被人工锁住的指令有(bayNo@moveOrder)：" + lockWiStr.toString());
                    }
                } else {
                    logger.logInfo("桥机(craneNo:" + craneNo + ")作业状态(" + wiCrane.getWorkStatus() + ")不能作业，该桥机的指令暂时不发送!");
                }
            } catch (Exception e) {
                logger.logError("分析桥机(craneNo:" + craneNo + ")的待发送指令过程中发生异常！");
                e.printStackTrace();
            }
        }
    }

    //test
    public void adjustWorkBlock() {
        wiDataMethod.generateData(wiData);

        analyzeCraneWithSentWICraneMove(wiData);

        analyzeCraneWithWIWorkBlock(wiData);

        changeWIBlockWorkTimeBeforeGetWaitToSendWI(wiData, wiDataMethod);

        GenerateResultMethod.generateWorkBlock(wiData.getAllWIWorkBlockList(), wiData.getSmartWiResults().getSmartReCwpBlockInfoList());
    }

    //test
    public void validateCwp() {

        wiDataMethod.generateData(wiData);

        analyzeCraneWithSentWICraneMove(wiData);

        analyzeCraneWithWIWorkBlock(wiData);

        changeWIBlockWorkTimeBeforeGetWaitToSendWI(wiData, wiDataMethod);

        GenerateResultMethod.generateWorkBlock(wiData.getAllWIWorkBlockList(), wiData.getSmartCwpValidatorResults().getSmartReCwpBlockInfoList());

        //判断是否产生重点路、或者触发阈值，反馈需要重排CWP的提示信息
        CwpValidator.validateKeyRoadAndSchedule(wiData);

        GenerateResultMethod.generateCwpValidateResult(wiData, wiData.getSmartCwpValidatorResults().getSmartReCwpValidatorInfoList());
    }
}
