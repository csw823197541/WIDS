package com.wids.manager.method;

import com.shbtos.biz.smart.cwp.pojo.*;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReMessageInfo;
import com.shbtos.biz.smart.cwp.service.SmartCwpValidatorResults;
import com.shbtos.biz.smart.cwp.service.SmartWiResults;
import com.wids.manager.data.AllData;
import com.wids.manager.data.VesselData;
import com.wids.manager.data.WIData;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDefaultValue;
import com.wids.model.log.Logger;
import com.wids.model.vessel.*;
import com.wids.model.wi.*;
import com.wids.service.method.PublicMethod;
import com.wids.utils.BeanCopyUtil;
import com.wids.utils.StringUtil;
import com.wids.utils.ValidatorUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2017/10/12.
 * Description:
 */
public class ParseDataMethod {

    private AllData allData;
    private Logger logger;

    public ParseDataMethod(AllData allData) {
        this.allData = allData;
        logger = allData.getLogger();
    }

    public void parseSchedule(List<SmartScheduleIdInfo> smartScheduleIdInfoList, SmartReMessageInfo smartReMessageInfo) {
        smartReMessageInfo.setWiVersion(WIDefaultValue.wiVersion);
        logger.logInfo("当前运行的算法版本号为：" + smartReMessageInfo.getWiVersion());
        logger.logInfo("解析航次信息");
        logger.logError("航次信息", ValidatorUtil.isEmpty(smartScheduleIdInfoList));
        for (SmartScheduleIdInfo smartScheduleIdInfo : smartScheduleIdInfoList) {
            Boolean needSend = smartScheduleIdInfo.getSendWorkInstruction();
            needSend = needSend == null || needSend;
            if (needSend) {
                Long berthId = smartScheduleIdInfo.getBerthId();
                String vesselCode = smartScheduleIdInfo.getVesselCode();
                String planBerthDirect = smartScheduleIdInfo.getPlanBerthDirect();
                String vesselType = smartScheduleIdInfo.getVesselType();
                try {
                    logger.logError("航次信息-靠泊Id", ValidatorUtil.isNull(berthId));
                    logger.logError("航次信息-船舶代码", ValidatorUtil.isNull(vesselCode));
                    logger.logError("航次信息-停靠方向", ValidatorUtil.isNull(planBerthDirect));
                    VMSchedule vmSchedule = new VMSchedule(berthId, vesselCode);
                    vmSchedule.setPlanBeginWorkTime(smartScheduleIdInfo.getPlanBeginWorkTime());
                    vmSchedule.setPlanEndWorkTime(smartScheduleIdInfo.getPlanEndWorkTime());
                    vmSchedule.setPlanStartPst(smartScheduleIdInfo.getPlanStartPst());
                    vmSchedule.setPlanEndPst(smartScheduleIdInfo.getPlanEndPst());
                    vmSchedule.setSendWorkInstruction(smartScheduleIdInfo.getSendWorkInstruction());
                    logger.logInfo("berthId: " + berthId + ", vesselCode: " + vesselCode + ", planBerthDirect: " + planBerthDirect + ", vesselType: " + vesselType);
                    vmSchedule.setVesselType(vesselType);
                    planBerthDirect = planBerthDirect.equals("L") ? CWPDomain.VES_BER_DIRECT_L : CWPDomain.VES_BER_DIRECT_R;
                    vmSchedule.setPlanBerthDirect(planBerthDirect);
                    //往全码头变量中填初始化数据
                    WIData wiData = new WIData(vmSchedule);
                    VesselData vesselData = new VesselData(vesselCode);
                    allData.addWIData(wiData);
                    allData.addVesselData(vesselData);
                } catch (Exception e) {
                    logger.logError("解析航次(berthId:" + berthId + ", vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                    e.printStackTrace();
                }
            }
        }
    }

    public void insertSmartWiResults(SmartWiResults smartWiResults) {
        logger.logInfo("将指令结果对象赋值给WIData数据对象");
        for (WIData wiData : allData.getAllWIData()) {
            wiData.setSmartWiResults(smartWiResults);
        }
    }

    public void insertSmartCwpValidatorResults(SmartCwpValidatorResults smartCwpValidatorResults) {
        logger.logInfo("将验证是否重排结果对象赋值给WIData数据对象");
        for (WIData wiData : allData.getAllWIData()) {
            wiData.setSmartCwpValidatorResults(smartCwpValidatorResults);
        }
    }

    public void parseConfiguration(List<SmartWiConfiguration> smartWiConfigurationList) {
        logger.logInfo("解析指令配置参数信息");
        logger.logError("指令配置参数信息", ValidatorUtil.isEmpty(smartWiConfigurationList));
        for (SmartWiConfiguration smartWiConfiguration : smartWiConfigurationList) {
            Long berthId = smartWiConfiguration.getBerthId();
            WIData wiData = allData.getWIDataByBerthId(berthId);
            if (wiData != null) {
                WIConfiguration wiConfiguration = wiData.getWiConfiguration();
                wiConfiguration = (WIConfiguration) BeanCopyUtil.copyBean(smartWiConfiguration, wiConfiguration);
                if (smartWiConfiguration.getIntervalTime() == null) {
                    logger.logInfo("船舶(berthId: " + berthId + ")配置参数中没有取多长时间段指令，算法默认取" + WIDefaultValue.intervalTime + "分钟内的指令");
                    wiConfiguration.setIntervalTime(WIDefaultValue.intervalTime);
                } else {
                    wiConfiguration.setIntervalTime(smartWiConfiguration.getIntervalTime());
                }
                //桥机安全距离，14米
                Double craneSafeSpan = smartWiConfiguration.getCraneSafeSpan();
                craneSafeSpan = craneSafeSpan != null ? craneSafeSpan : WIDefaultValue.craneSafeSpan;
                wiConfiguration.setCraneSafeSpan(craneSafeSpan);
                //桥机跨机械起趴大梁移动时间，45分钟
                Long crossBarTime = smartWiConfiguration.getCrossBarTime();
                crossBarTime = crossBarTime != null ? crossBarTime * 60 : WIDefaultValue.crossBarTime;
                wiConfiguration.setCrossBarTime(crossBarTime);
                //是否过驾驶台起大梁
                Boolean crossBridge = smartWiConfiguration.getCrossBridge();
                crossBridge = crossBridge != null ? crossBridge : WIDefaultValue.crossBridge;
                wiConfiguration.setCrossBridge(crossBridge);
                //是否过烟囱起大梁
                Boolean crossChimney = smartWiConfiguration.getCrossChimney();
                crossChimney = crossChimney != null ? crossChimney : WIDefaultValue.crossChimney;
                wiConfiguration.setCrossChimney(crossChimney);
                //甲板上重量差
                Long deckWeightDifference = smartWiConfiguration.getDeckWeightDifference();
                deckWeightDifference = deckWeightDifference != null ? deckWeightDifference : WIDefaultValue.deckWeightDifference;
                wiConfiguration.setDeckWeightDifference(deckWeightDifference);
                //舱内重量差
                Long hatchWeightDifference = smartWiConfiguration.getHatchWeightDifference();
                hatchWeightDifference = hatchWeightDifference != null ? hatchWeightDifference : WIDefaultValue.hatchWeightDifference;
                wiConfiguration.setHatchWeightDifference(hatchWeightDifference);
                //甲板上里外两边重量差
                Long hatchSideWeightDifference = smartWiConfiguration.getHatchSideWeightDifference();
                hatchSideWeightDifference = hatchSideWeightDifference != null ? hatchSideWeightDifference : WIDefaultValue.hatchSideWeightDifference;
                wiConfiguration.setHatchSideWeightDifference(hatchSideWeightDifference);
                //允许甲板上同槽交换
                String deckRowExchange = smartWiConfiguration.getDeckRowExchange();
                deckRowExchange = StringUtil.notBlank(deckRowExchange) ? deckRowExchange : WIDefaultValue.deckRowExchange;
                wiConfiguration.setDeckRowExchange(deckRowExchange);
                //允许甲板上同舱（槽与槽）交换
                String deckBayExchange = smartWiConfiguration.getDeckBayExchange();
                deckBayExchange = StringUtil.notBlank(deckBayExchange) ? deckBayExchange : WIDefaultValue.deckBayExchange;
                wiConfiguration.setDeckBayExchange(deckBayExchange);
                //允许舱下同槽交换
                String hatchRowExchange = smartWiConfiguration.getHatchRowExchange();
                hatchRowExchange = StringUtil.notBlank(hatchRowExchange) ? hatchRowExchange : WIDefaultValue.hatchRowExchange;
                wiConfiguration.setHatchRowExchange(hatchRowExchange);
                //允许舱下同舱（槽与槽）交换
                String hatchBayExchange = smartWiConfiguration.getHatchBayExchange();
                hatchBayExchange = StringUtil.notBlank(hatchBayExchange) ? hatchBayExchange : WIDefaultValue.hatchBayExchange;
                wiConfiguration.setHatchBayExchange(hatchBayExchange);
                //允许甲板上/下间交换
                String deckAndHatchExchange = smartWiConfiguration.getDeckAndHatchExchange();
                deckAndHatchExchange = StringUtil.notBlank(deckAndHatchExchange) ? deckAndHatchExchange : WIDefaultValue.deckAndHatchExchange;
                wiConfiguration.setDeckAndHatchExchange(deckAndHatchExchange);
                //允许整船（舱与舱）交换
                String allVesselExchange = smartWiConfiguration.getAllVesselExchange();
                allVesselExchange = StringUtil.notBlank(allVesselExchange) ? allVesselExchange : WIDefaultValue.allVesselExchange;
                wiConfiguration.setAllVesselExchange(allVesselExchange);
                //允许空箱整船交换
                String emptyCntExchange = smartWiConfiguration.getEmptyCntExchange();
                emptyCntExchange = StringUtil.notBlank(emptyCntExchange) ? emptyCntExchange : WIDefaultValue.emptyCntExchange;
                wiConfiguration.setEmptyCntExchange(emptyCntExchange);
                //允许交换作业顺序
                String moveOrderExchange = smartWiConfiguration.getMoveOrderExchange();
                moveOrderExchange = StringUtil.notBlank(moveOrderExchange) ? moveOrderExchange : WIDefaultValue.moveOrderExchange;
                wiConfiguration.setMoveOrderExchange(moveOrderExchange);
                //甲板上发箱层高限制
                Integer deckTierNum = smartWiConfiguration.getDeckTierNum();
                deckTierNum = deckTierNum != null ? deckTierNum : WIDefaultValue.deckTierNum;
                wiConfiguration.setDeckTierNum(deckTierNum);
                //舱内发箱层高限制
                Integer hatchTierNum = smartWiConfiguration.getHatchTierNum();
                hatchTierNum = hatchTierNum != null ? hatchTierNum : WIDefaultValue.hatchTierNum;
                wiConfiguration.setHatchTierNum(hatchTierNum);
                // 任意多少分钟发送多少个箱子，发箱数目
                Integer sendContainerNum = smartWiConfiguration.getSendContainerNum();
                sendContainerNum = sendContainerNum != null ? sendContainerNum : WIDefaultValue.sendContainerNum;
                wiConfiguration.setSendContainerNum(sendContainerNum);
                // 任意多少分钟发送多少个箱子，时间参数，单位分钟
                Long sendIntervalTime = smartWiConfiguration.getSendIntervalTime();
                sendIntervalTime = sendIntervalTime != null ? sendIntervalTime : WIDefaultValue.sendIntervalTime;
                wiConfiguration.setSendIntervalTime(sendIntervalTime * 60000);
            }
        }
    }

    public void parseCrane(List<SmartCraneWorkStartTmInfo> smartCraneWorkStartTmInfoList, List<SmartCraneBaseInfo> smartCraneBaseInfoList) {
        logger.logInfo("解析桥机作业计划信息");
        logger.logError("桥机作业计划信息", ValidatorUtil.isEmpty(smartCraneWorkStartTmInfoList));
        for (SmartCraneWorkStartTmInfo smartCraneWorkStartTmInfo : smartCraneWorkStartTmInfoList) {
            String craneNo = smartCraneWorkStartTmInfo.getCraneNo();
            String workStatus = smartCraneWorkStartTmInfo.getWorkStatus();
            WICrane wiCrane = new WICrane(craneNo, workStatus);
            for (SmartCraneBaseInfo smartCraneBaseInfo : smartCraneBaseInfoList) {
                if (smartCraneBaseInfo.getCraneNo().equals(wiCrane.getCraneNo())) {
                    Integer craneSeq = smartCraneBaseInfo.getCraneSeq();
                    logger.logError("桥机(craneNo:" + craneNo + ")序号(craneSeq:" + craneSeq + ")为null", ValidatorUtil.isNull(craneSeq));
                    wiCrane.setCraneSeq(craneSeq);
                    break;
                }
            }
            long currentTime = System.currentTimeMillis();
            wiCrane.setActualWorkST(new Date(currentTime));
//            wiCrane.setWiWorkTime(new Date(currentTime));
            allData.addWICrane(wiCrane);
        }
    }

    public void parseCraneMaintainPlan(List<SmartCraneMaintainPlanInfo> smartCraneMaintainPlanInfoList) {
        logger.logInfo("解析桥机维修计划信息");
        logger.logInfo("桥机维修计划信息", ValidatorUtil.isEmpty(smartCraneMaintainPlanInfoList));
        for (SmartCraneMaintainPlanInfo smartCraneMaintainPlanInfo : smartCraneMaintainPlanInfoList) {
            String craneNo = smartCraneMaintainPlanInfo.getCraneNo();
            if (craneNo != null) {
                Date st = smartCraneMaintainPlanInfo.getMaintainStartTime();
                Date et = smartCraneMaintainPlanInfo.getMaintainEndTime();
                logger.logError("桥机(craneNo:" + craneNo + ")维修计划开始时间为null", ValidatorUtil.isNull(st));
                logger.logError("桥机(craneNo:" + craneNo + ")维修计划结束时间为null", ValidatorUtil.isNull(et));
                if (st.compareTo(et) < 0) {
                    WICraneMaintainPlan wiCraneMaintainPlan = new WICraneMaintainPlan();
                    wiCraneMaintainPlan = (WICraneMaintainPlan) BeanCopyUtil.copyBean(smartCraneMaintainPlanInfo, wiCraneMaintainPlan);
                    allData.addWICraneMaintainPlan(wiCraneMaintainPlan);
                } else {
                    logger.logInfo("桥机(craneNo:" + craneNo + ")维修计划开始时间大于或等于结束时间！");
                }
            }
        }
    }

    public void parseWorkBlock(List<SmartCwpWorkBlockInfo> smartCwpWorkBlockInfoList) {
        logger.logInfo("解析桥机作业块信息");
        logger.logError("桥机作业块信息", ValidatorUtil.isEmpty(smartCwpWorkBlockInfoList));
        for (SmartCwpWorkBlockInfo smartCwpWorkBlockInfo : smartCwpWorkBlockInfoList) {

//            smartCwpWorkBlockInfo.setWorkStatus("3");
//            smartCwpWorkBlockInfo.setSentAmount(0L);

            Long berthId = smartCwpWorkBlockInfo.getBerthId();
            WIData wiData = allData.getWIDataByBerthId(berthId);
            if (wiData != null) {
                String craneNo = smartCwpWorkBlockInfo.getCraneNo();
                Long craneSeq = smartCwpWorkBlockInfo.getCraneSeq(); //桥机作业块作业顺序
                Long hatchId = smartCwpWorkBlockInfo.getHatchId();
                String workStatus = smartCwpWorkBlockInfo.getWorkStatus();
                logger.logError("作业块桥机号(craneNo:" + craneNo + ")为空", ValidatorUtil.isNull(craneNo));
                logger.logError("作业块桥机序号(craneSeq:" + craneSeq + ")为空", ValidatorUtil.isNull(craneSeq));
                logger.logError("作业块舱Id(hatchId:" + hatchId + ")为空", ValidatorUtil.isNull(hatchId));
                logger.logError("作业块状态(workStatus:" + workStatus + ")为空", ValidatorUtil.isNull(workStatus));
                String blockKey = StringUtil.getKey(craneNo, craneSeq.toString());
                WIWorkBlock wiWorkBlock = new WIWorkBlock(blockKey);
                wiWorkBlock = (WIWorkBlock) BeanCopyUtil.copyBean(smartCwpWorkBlockInfo, wiWorkBlock);
                wiWorkBlock.setEstimateStartTime(wiWorkBlock.getWorkingStartTime()); //TODO: 作业块的计划开始和结束时间很重要
                wiWorkBlock.setEstimateEndTime(wiWorkBlock.getWorkingEndTime());
                wiData.addWIWorkBlock(wiWorkBlock);
            }
        }
    }

    public void parseLocationInfo(List<SmartVpsVslLocationsInfo> smartVpsVslLocationsInfoList) {
        logger.logError("船箱位信息", ValidatorUtil.isEmpty(smartVpsVslLocationsInfoList));
        for (SmartVpsVslLocationsInfo smartVpsVslLocationsInfo : smartVpsVslLocationsInfoList) {
            String vLocation = smartVpsVslLocationsInfo.getLocation();
            Long bayId = smartVpsVslLocationsInfo.getBayId();
            String vesselCode = smartVpsVslLocationsInfo.getVesselCode();
            String size = smartVpsVslLocationsInfo.getSize();
            try {
                VesselData vesselData = allData.getVesselDataByVesselCode(vesselCode);
                if (vesselData != null) {
                    VMPosition vmPosition = new VMPosition(vLocation);
                    VMBay vmBay = vesselData.getVMBayByBayId(bayId);
                    String aboveOrBelow = vmBay.getAboveOrBelow();
                    VMSlot vmSlot = new VMSlot(vmPosition, aboveOrBelow, size, bayId);
                    vesselData.addVMSlot(vmSlot);
                    //要根据船箱位信息，初始化该倍位下每排的最大层号和最小层号
                    Integer rowNo = vmPosition.getRowNo();
                    VMRow vmRow = vesselData.getVMRowByBayIdAndRowNo(bayId, rowNo);
                    logger.logError("船箱位信息-查找不到排(" + rowNo + ")信息！", ValidatorUtil.isNull(vmRow));
                    vmRow.addVMSlot(vmSlot);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")船箱位(vLocation:" + vLocation + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    public void parseRowInfo(List<SmartVpsVslRowsInfo> smartVpsVslRowsInfoList) {
        logger.logError("排信息", ValidatorUtil.isEmpty(smartVpsVslRowsInfoList));
        for (SmartVpsVslRowsInfo smartVpsVslRowsInfo : smartVpsVslRowsInfoList) {
            Long bayId = smartVpsVslRowsInfo.getBayId();
            String vesselCode = smartVpsVslRowsInfo.getVesselCode();
            Integer rowNo = null;
            try {
                VesselData vesselData = allData.getVesselDataByVesselCode(vesselCode);
                if (vesselData != null) {
                    logger.logError("排信息-排号", ValidatorUtil.isNull(smartVpsVslRowsInfo.getRowNo()));
                    rowNo = Integer.valueOf(smartVpsVslRowsInfo.getRowNo());
                    VMRow vmRow = new VMRow(bayId, rowNo);
                    vesselData.addVMRow(vmRow);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")排(rowNo:" + rowNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    public void parseBayInfo(List<SmartVpsVslBaysInfo> smartVpsVslBaysInfoList) {
        logger.logError("倍位信息", ValidatorUtil.isEmpty(smartVpsVslBaysInfoList));
        for (SmartVpsVslBaysInfo smartVpsVslBaysInfo : smartVpsVslBaysInfoList) {
            Long bayId = smartVpsVslBaysInfo.getBayId();
            Long hatchId = smartVpsVslBaysInfo.getHatchId();
            String aboveOrBelow = smartVpsVslBaysInfo.getDeckOrHatch();
            String vesselCode = smartVpsVslBaysInfo.getVesselCode();
            Integer bayNo = null;
            try {
                VesselData vesselData = allData.getVesselDataByVesselCode(vesselCode);
                if (vesselData != null) {
                    logger.logError("倍位信息-倍位号", ValidatorUtil.isNull(smartVpsVslBaysInfo.getBayNo()));
                    bayNo = Integer.valueOf(smartVpsVslBaysInfo.getBayNo());
                    logger.logError("倍位信息-倍位信息中甲板上、下字段为null", ValidatorUtil.isNull(aboveOrBelow));
                    aboveOrBelow = aboveOrBelow.equals("D") ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                    VMBay vmBay = new VMBay(bayId, bayNo, aboveOrBelow, hatchId);
                    vesselData.addVMBay(vmBay);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")倍位(bayNo:" + bayNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    public void parseHatchInfo(List<SmartVpsVslHatchsInfo> smartVpsVslHatchsInfoList) {
        logger.logError("舱信息", ValidatorUtil.isEmpty(smartVpsVslHatchsInfoList));
        for (SmartVpsVslHatchsInfo smartVpsVslHatchsInfo : smartVpsVslHatchsInfoList) {
            String vesselCode = smartVpsVslHatchsInfo.getVesselCode();
            Long hatchId = smartVpsVslHatchsInfo.getHatchId();
            try {
                VesselData vesselData = allData.getVesselDataByVesselCode(vesselCode);
                if (vesselData != null) {
                    logger.logError("舱信息-舱(Id:" + hatchId + ")信息为null", ValidatorUtil.isNull(hatchId));
                    VMHatch vmHatch = new VMHatch(hatchId);
                    logger.logError("舱信息-舱(Id:" + hatchId + ")位置坐标为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchPosition()));
                    vmHatch.setHatchPosition(smartVpsVslHatchsInfo.getHatchPosition());
                    logger.logError("舱信息-舱(Id:" + hatchId + ")长度为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchLength()));
                    vmHatch.setHatchLength(smartVpsVslHatchsInfo.getHatchLength());
                    vesselData.addVMHatch(vmHatch);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")舱(Id:" + hatchId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }

        }
    }

    public void parseVesselMachine(List<SmartVesselMachinesInfo> smartVesselMachinesInfoList) {
        logger.logInfo("船舶器械信息(烟囱、驾驶台等)", ValidatorUtil.isEmpty(smartVesselMachinesInfoList));
        for (SmartVesselMachinesInfo smartVesselMachinesInfo : smartVesselMachinesInfoList) {
            String vesselCode = smartVesselMachinesInfo.getVesselCode();
            Double machinePosition = smartVesselMachinesInfo.getMachinePosition();
            String machineType = smartVesselMachinesInfo.getMachineType();
            try {
                VesselData vesselData = allData.getVesselDataByVesselCode(vesselCode);
                if (vesselData != null) {
                    logger.logError("船舶机械(machineType:" + machineType + ")-位置坐标为null", ValidatorUtil.isNull(machinePosition));
                    VMMachine vmMachine = new VMMachine();
                    vmMachine = (VMMachine) BeanCopyUtil.copyBean(smartVesselMachinesInfo, vmMachine);
                    vesselData.addVMMachine(vmMachine);
                }
            } catch (Exception e) {
                logger.logError("解析船舶机械(vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    public void parseHatchCover(List<SmartVpsVslHatchcoversInfo> smartVpsVslHatchcoversInfoList) {

    }

    public void parseVesselContainer(List<SmartVesselContainerInfo> smartVesselContainerInfoList) {
        logger.logInfo("解析接口传入的指令信息");
        logger.logError("指令信息", ValidatorUtil.isEmpty(smartVesselContainerInfoList));
        StringBuilder sb = new StringBuilder("由于没有作业工艺和作业顺序，发箱服务暂不考虑的指令有：");
        for (SmartVesselContainerInfo smartVesselContainerInfo : smartVesselContainerInfoList) {
            Long berthId = smartVesselContainerInfo.getBerthId();
            String vLocation = smartVesselContainerInfo.getvLocation();
            Long vpcCntId = smartVesselContainerInfo.getVpcCntrId();
            String dlType = smartVesselContainerInfo.getLduldfg();
            String workFlow = smartVesselContainerInfo.getWorkflow();
            Long moveOrder = smartVesselContainerInfo.getCwpwkMoveNum();
            Long cntWorkTime = smartVesselContainerInfo.getContainerWorkInterval(); //单位秒
            String dgCd = smartVesselContainerInfo.getDtpDnggcd(); //危险品：
            String isHeight = smartVesselContainerInfo.getIsHeight(); //是否是高箱：Y/N
            String rfFlag = smartVesselContainerInfo.getRfcfg(); //冷藏标记：Y/N
            String overrunCd = smartVesselContainerInfo.getOvlmtcd(); //超限箱标记：Y/N
            String efFlag = smartVesselContainerInfo.getEffg();
            String workStatus = smartVesselContainerInfo.getWorkStatus();
            String moveStage = smartVesselContainerInfo.getMoveStage();
            Date st = smartVesselContainerInfo.getWorkingStartTime();
            Date ed = smartVesselContainerInfo.getWorkingEndTime();
            String cwoManualLocation = StringUtil.notBlank(smartVesselContainerInfo.getCwoManualLocation()) ? smartVesselContainerInfo.getCwoManualLocation() : CWPDomain.YES;
            String throughFlag = smartVesselContainerInfo.getThroughFlag();
            throughFlag = StringUtil.notBlank(throughFlag) ? throughFlag : CWPDomain.NO;
            String canRecycle = smartVesselContainerInfo.getCanRecycleFlag();
            canRecycle = StringUtil.isBlank(canRecycle) ? CWPDomain.NO : canRecycle;
            try {
                WIData wiData = allData.getWIDataByBerthId(berthId);
                if (wiData != null) {
                    if (moveOrder != null && workFlow != null) {
                        logger.logError("船舶(berthId:" + berthId + ")船箱位(" + vLocation + ")的指令没有当前作业状态信息！", ValidatorUtil.isNull(workStatus));
                        logger.logError("船舶(berthId:" + berthId + ")船箱位(" + vLocation + ")的指令没有作业耗时信息！", ValidatorUtil.isNull(cntWorkTime));
                        VMPosition vmPosition = new VMPosition(vLocation);
                        Integer bayNo = vmPosition.getBayNo();
                        String bayRow = StringUtil.getKey(bayNo, vmPosition.getRowNo());
                        String cntKey = StringUtil.getKey(bayRow, moveOrder.toString());
                        cntKey = StringUtil.getKey(cntKey, dlType);
                        if (cntWorkTime <= 0) {
                            cntWorkTime = 120L;
                        }
                        WICraneContainer wiCraneContainer = new WICraneContainer(vpcCntId, cntKey, bayNo, moveOrder, workFlow, workStatus, cntWorkTime);
                        wiCraneContainer.setBerthId(berthId);
                        wiCraneContainer.setCraneNo(smartVesselContainerInfo.getCraneNo());
                        wiCraneContainer.setHatchId(smartVesselContainerInfo.getHatchId());
                        wiCraneContainer.setLdFlag(smartVesselContainerInfo.getLduldfg());
                        wiCraneContainer.setvLocation(vLocation);
                        wiCraneContainer.setVoyageId(smartVesselContainerInfo.getVoyageId());
                        wiCraneContainer.setCwoManualWi(smartVesselContainerInfo.getCwoManualWi());
                        wiCraneContainer.setCraneNo(smartVesselContainerInfo.getCraneNo());
                        wiCraneContainer.setCwoManualLocation(cwoManualLocation);
                        wiCraneContainer.setCanRecycleFlag(canRecycle);
                        wiCraneContainer.setPlanStartTime(smartVesselContainerInfo.getWorkingStartTime());
                        wiCraneContainer.setPlanEndTime(smartVesselContainerInfo.getWorkingEndTime());
                        //集装箱信息
                        WIContainer wiContainer = new WIContainer(smartVesselContainerInfo.getyLocation());
                        wiContainer.setYardContainerId(smartVesselContainerInfo.getYardContainerId());
                        wiContainer.setSize(smartVesselContainerInfo.getcSzCsizecd());
                        wiContainer.setyLocation(smartVesselContainerInfo.getyLocation());
                        wiContainer.setCntHeight(smartVesselContainerInfo.getCntHeightDesc());
                        wiContainer.setWeightKg(smartVesselContainerInfo.getWeight());
                        wiContainer.setDstPort(smartVesselContainerInfo.getDstPort());
                        wiContainer.setType(smartVesselContainerInfo.getcTypeCd());
                        rfFlag = !StringUtil.notBlank(rfFlag) || "N".equals(rfFlag) ? CWPDomain.NO : CWPDomain.YES;
                        wiContainer.setRfFlag(rfFlag);
                        dgCd = !StringUtil.notBlank(dgCd) || "N".equals(dgCd) ? CWPDomain.NO : CWPDomain.YES;
                        wiContainer.setDgCd(dgCd);
                        overrunCd = !StringUtil.notBlank(overrunCd) || "N".equals(overrunCd) ? CWPDomain.NO : CWPDomain.YES;
                        wiContainer.setOverrunCd(overrunCd);
                        isHeight = !StringUtil.notBlank(isHeight) || "N".equals(isHeight) ? CWPDomain.NO : CWPDomain.YES;
                        wiContainer.setIsHeight(isHeight);
                        efFlag = !StringUtil.notBlank(efFlag) || "F".equals(efFlag) ? CWPDomain.NO : CWPDomain.YES;
                        wiContainer.setEfFlag(efFlag);
                        wiContainer.setWorkStatus(workStatus);
                        wiContainer.setMoveStage(moveStage);
                        wiContainer.setCwoManualLocation(cwoManualLocation);
                        wiContainer.setOriginalCraneContainer(wiCraneContainer);
                        wiContainer.setExchangeCraneContainer(wiCraneContainer);
                        wiCraneContainer.setOriginalContainer(wiContainer);
                        wiCraneContainer.setOriginalContainerTemp(wiContainer);
                        wiCraneContainer.setExchangeContainer(wiContainer);
                        wiCraneContainer.setExchangeContainerTemp(wiContainer);
                        wiData.addWICraneContainer(wiCraneContainer);
                    } else {
                        sb.append("船箱位(").append(vLocation).append(")").append("、");
                    }
                }
            } catch (Exception e) {
                logger.logError("解析接口传入的指令(berthId:" + berthId + ", vLocation:" + vLocation + ", dlType:" + dlType + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
        logger.logInfo(sb.toString());
    }

    public void parseAreaTask(List<SmartAreaTaskInfo> smartAreaTaskInfoList) {
        logger.logInfo("解析接口传入的当前一个小时内箱区任务信息");
        logger.logError("箱区任务信息", ValidatorUtil.isEmpty(smartAreaTaskInfoList));
        for (SmartAreaTaskInfo smartAreaTaskInfo : smartAreaTaskInfoList) {
            try {

            } catch (Exception e) {

            }
        }
    }

}
