package com.wids.manager.method;

import com.shbtos.biz.smart.cwp.pojo.Results.*;
import com.shbtos.biz.smart.cwp.service.SmartWiResults;
import com.wids.manager.data.AllData;
import com.wids.manager.data.WIData;
import com.wids.model.domain.WIDomain;
import com.wids.model.log.Logger;
import com.wids.model.wi.*;
import com.wids.service.method.PublicMethod;
import com.wids.utils.BeanCopyUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2017/10/17.
 * Description:
 */
public class GenerateResultMethod {

    public static void generateWI(WIData wiData) {
        SmartWiResults smartWiResults = wiData.getSmartWiResults();
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            String craneNo = wiCrane.getCraneNo();
            List<WICraneMove> wiCraneMoveList = wiData.getWICraneMoveListFromWaitToSendMap(craneNo);
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                List<WICraneContainer> wiCraneContainerList = wiCraneMove.getWiCraneContainerList();
                for (WICraneContainer wiCraneContainer : wiCraneContainerList) {
                    SmartReAutoWiInfo smartReAutoWiInfo = new SmartReAutoWiInfo();
                    smartReAutoWiInfo.setBerthId(wiCraneContainer.getBerthId());
                    smartReAutoWiInfo.setCraneNo(craneNo);
                    smartReAutoWiInfo.setVpcCntrId(wiCraneContainer.getVpcCntId());
//                    if (wiCraneContainer.getSentSeq() == null) {
//                        smartReAutoWiInfo.setCwpwkmovenum(wiCraneMove.getMoveOrder());
//                    } else {
//                        smartReAutoWiInfo.setCwpwkmovenum(wiCraneContainer.getSentSeq());
//                    }
                    smartReAutoWiInfo.setCwpwkmovenum(wiCraneContainer.getSentSeq());
                    smartReAutoWiInfo.setvLocation(wiCraneContainer.getvLocation());
                    smartReAutoWiInfo.setyLocation(wiCraneContainer.getExchangeContainer().getyLocation());
                    smartReAutoWiInfo.setWorkFlow(wiCraneContainer.getWorkflow());
                    smartReAutoWiInfo.setWorkingStartTime(wiCraneMove.getWorkingStartTime());
                    smartReAutoWiInfo.setWorkingEndTime(wiCraneMove.getWorkingEndTime());
                    if (wiCraneContainer.getExchangeContainer().getYardContainerId() != null) {
                        smartReAutoWiInfo.setYardContainerId(Long.valueOf(wiCraneContainer.getExchangeContainer().getYardContainerId()));
                    }
                    smartReAutoWiInfo.setOverCntNum(wiCraneContainer.getOverCntNum());
                    smartReAutoWiInfo.setExchangeReason(wiCraneContainer.getExchangeReason());
                    smartWiResults.getSmartReAutoWiInfos().add(smartReAutoWiInfo);
                    generateExchangeWI(wiCraneContainer, wiData);
                }
            }
        }
    }

    private static void generateExchangeWI(WICraneContainer wiCraneContainer, WIData wiData) {
        SmartWiResults smartWiResults = wiData.getSmartWiResults();
        WIContainer originalCnt = wiCraneContainer.getOriginalContainer();
        WIContainer exchangeCnt = wiCraneContainer.getExchangeContainer();
        if (originalCnt.getyLocation() != null && exchangeCnt.getyLocation() != null) {
            if (!originalCnt.getyLocation().equals(exchangeCnt.getyLocation())) {

                WIExchangeCnt wiExchangeCnt1 = new WIExchangeCnt();
                wiExchangeCnt1.setBerthId(wiCraneContainer.getBerthId());
                wiExchangeCnt1.setVpcCntId(wiCraneContainer.getVpcCntId());
                wiExchangeCnt1.setvLocation(wiCraneContainer.getvLocation());
                if (exchangeCnt.getYardContainerId() != null) {
                    wiExchangeCnt1.setYardContainerId(Long.valueOf(exchangeCnt.getYardContainerId()));
                }
                wiExchangeCnt1.setOriginalYLocation(originalCnt.getyLocation());
                wiExchangeCnt1.setExchangeYLocation(exchangeCnt.getyLocation());
                wiExchangeCnt1.setOriginalOverCntNum(wiCraneContainer.getOriginalOverCntNum());
                wiExchangeCnt1.setExchangeOverCntNum(exchangeCnt.getOverCntNumTemp());
                wiData.addWIExchangeCnt(wiExchangeCnt1);
                SmartReExchangedWiInfo smartReExchangedWiInfo1 = new SmartReExchangedWiInfo();
                smartReExchangedWiInfo1 = (SmartReExchangedWiInfo) BeanCopyUtil.copyBean(wiExchangeCnt1, smartReExchangedWiInfo1);
                smartWiResults.getSmartReExchangedWiInfoList().add(smartReExchangedWiInfo1);

                WICraneContainer exCraneContainer = exchangeCnt.getOriginalCraneContainer();
                WIExchangeCnt wiExchangeCnt2 = new WIExchangeCnt();
                wiExchangeCnt2.setBerthId(exCraneContainer.getBerthId());
                wiExchangeCnt2.setVpcCntId(exCraneContainer.getVpcCntId());
                wiExchangeCnt2.setvLocation(exCraneContainer.getvLocation());
                if (originalCnt.getYardContainerId() != null) {
                    wiExchangeCnt2.setYardContainerId(Long.valueOf(originalCnt.getYardContainerId()));
                }
                wiExchangeCnt1.setOriginalYLocation(exchangeCnt.getyLocation());
                wiExchangeCnt1.setExchangeYLocation(originalCnt.getyLocation());
                wiData.addWIExchangeCnt(wiExchangeCnt2);
                SmartReExchangedWiInfo smartReExchangedWiInfo2 = new SmartReExchangedWiInfo();
                smartReExchangedWiInfo2 = (SmartReExchangedWiInfo) BeanCopyUtil.copyBean(wiExchangeCnt2, smartReExchangedWiInfo2);
                smartWiResults.getSmartReExchangedWiInfoList().add(smartReExchangedWiInfo2);

            }
        }

    }

    public static void generateWorkBlock(List<WIWorkBlock> wiWorkBlockList, List<SmartReCwpBlockInfo> smartReCwpBlockInfoList) {
        for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
            SmartReCwpBlockInfo smartReCwpBlockInfo = new SmartReCwpBlockInfo();
            smartReCwpBlockInfo.setBayNo(wiWorkBlock.getBayNo());
            smartReCwpBlockInfo.setBerthId(wiWorkBlock.getBerthId());
            smartReCwpBlockInfo.setCraneNo(wiWorkBlock.getCraneNo());
            smartReCwpBlockInfo.setCranePosition(wiWorkBlock.getCranePosition());
            smartReCwpBlockInfo.setCraneSeq(wiWorkBlock.getCraneSeq());
            smartReCwpBlockInfo.setHatchId(wiWorkBlock.getHatchId());
            smartReCwpBlockInfo.setHatchSeq(wiWorkBlock.getHatchSeq());
            smartReCwpBlockInfo.setCurrentCraneBayNo(wiWorkBlock.getCurrentCraneBayNo());
            smartReCwpBlockInfo.setPlanAmount(wiWorkBlock.getPlanAmount());
            smartReCwpBlockInfo.setWorkingStartTime(wiWorkBlock.getEstimateStartTime());
            smartReCwpBlockInfo.setWorkingEndTime(wiWorkBlock.getEstimateEndTime());
            smartReCwpBlockInfo.setBlockId(wiWorkBlock.getBlockId());
            if (smartReCwpBlockInfo.getWorkingStartTime().equals(smartReCwpBlockInfo.getWorkingEndTime())) {
                smartReCwpBlockInfo.setDeleteFlag(WIDomain.YES);
            }
            smartReCwpBlockInfo.setSentAmount(wiWorkBlock.getSentAmount());
            smartReCwpBlockInfo.setWorkStatus(wiWorkBlock.getWorkStatus());
            smartReCwpBlockInfo.setBlockMessage(wiWorkBlock.getBlockMessage());
            smartReCwpBlockInfoList.add(smartReCwpBlockInfo);
        }
    }

    public static void generateCwpValidateResult(WIData wiData, List<SmartReCwpValidatorInfo> smartReCwpValidatorInfoList) {
        SmartReCwpValidatorInfo smartReCwpValidatorInfo = new SmartReCwpValidatorInfo();
        smartReCwpValidatorInfo.setBerthId(wiData.getVmSchedule().getBerthId());
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        StringBuilder stringBuilder = new StringBuilder();
        for (WICrane wiCrane : wiCraneList) {
            if (wiCrane.getNeedReDoCwp()) {
                smartReCwpValidatorInfo.setNeedReDoCwp(Boolean.TRUE);
                stringBuilder.append(wiCrane.getReDoCwpReason());
            }
        }
        smartReCwpValidatorInfo.setReDoCwpReason(stringBuilder.toString());
        smartReCwpValidatorInfoList.add(smartReCwpValidatorInfo);
    }

    public static void generateCarryOrder(WIData wiData) {
        Logger logger = wiData.getLogger();
        long startCarryOrder = 200000L;
        Map<Long, Long> carryOrderMap = new HashMap<>();
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            List<WICraneMove> wiCraneMoveList = wiData.getWICraneMoveListFromWaitToSendMap(wiCrane.getCraneNo());
            PublicMethod.sortWICraneMoveByStartTime(wiCraneMoveList);
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                Long hatchId = wiCraneMove.getHatchId();
                if (hatchId != null) {
                    if (carryOrderMap.get(hatchId) == null) {
                        startCarryOrder = startCarryOrder * 2;
                        carryOrderMap.put(hatchId, startCarryOrder);
                    }
                    wiCraneMove.setCarryOrder(carryOrderMap.get(hatchId) + wiCraneMove.getMoveOrder());
                } else {
                    logger.logError("指令(" + wiCraneMove.getWorkBayKey() + ")舱Id字段不能为null");
                }
            }
        }
    }

    public static void saveLogger(AllData allData, SmartReMessageInfo smartReMessageInfo) {

        String allDataErrorInfo = allData.getLogger().getError();
        smartReMessageInfo.setErrorLog(allDataErrorInfo);

        String allDataExecuteInfo = allData.getLogger().getInfo();
        smartReMessageInfo.setExecuteLog(allDataExecuteInfo);

        for (WIData wiData : allData.getAllWIData()) {
            smartReMessageInfo.putErrorLog(wiData.getVmSchedule().getBerthId(), allDataErrorInfo);
            smartReMessageInfo.putErrorLog(wiData.getVmSchedule().getBerthId(), wiData.getLogger().getError());
            smartReMessageInfo.putExecuteLog(wiData.getVmSchedule().getBerthId(), allDataExecuteInfo);
            smartReMessageInfo.putExecuteLog(wiData.getVmSchedule().getBerthId(), wiData.getLogger().getInfo());
        }
    }

    public static void saveValidatorLogger(AllData allData, SmartReMessageInfo smartReMessageInfo) {
        String allDataErrorInfo = allData.getLogger().getError();
        smartReMessageInfo.setErrorLog(allDataErrorInfo);

        for (WIData wiData : allData.getAllWIData()) {
            smartReMessageInfo.putErrorLog(wiData.getVmSchedule().getBerthId(), allDataErrorInfo);
            smartReMessageInfo.putErrorLog(wiData.getVmSchedule().getBerthId(), wiData.getLogger().getError());
            List<WICrane> wiCraneList = wiData.getAllWICraneList();
            StringBuilder stringBuilder = new StringBuilder();
            for (WICrane wiCrane : wiCraneList) {
                if (wiCrane.getNeedReDoCwp()) {
                    stringBuilder.append(wiCrane.getReDoCwpReason());
                }
            }
            smartReMessageInfo.putExecuteLog(wiData.getVmSchedule().getBerthId(), stringBuilder.toString());
        }
    }

}
