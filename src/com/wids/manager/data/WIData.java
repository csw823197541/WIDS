package com.wids.manager.data;

import com.shbtos.biz.smart.cwp.service.SmartCwpValidatorResults;
import com.shbtos.biz.smart.cwp.service.SmartWiResults;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDomain;
import com.wids.model.log.Logger;
import com.wids.model.vessel.VMPosition;
import com.wids.model.vessel.VMSchedule;
import com.wids.model.wi.*;
import com.wids.service.method.PublicMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2017/10/11.
 * Description: 发箱服务涉及到的指令、桥机、箱区作业能力等数据
 */
public class WIData {

    private Logger logger;

    private SmartWiResults smartWiResults;
    private SmartCwpValidatorResults smartCwpValidatorResults;

    private WIConfiguration wiConfiguration;

    private VMSchedule vmSchedule;

    private Map<String, WICrane> wiCraneMap; //该船作业的所有桥机

    private List<WIWorkBlock> wiWorkBlockList; //所有作业块信息
    private Map<String, List<WIWorkBlock>> craneWiWorkBlockMap; //所有作业块

    private List<WICraneContainer> wiCraneContainerList; //所有指令
    private Map<String, WICraneMove> workBayKeyWiCraneMoveMap; //所有指令
    private Map<String, List<WICraneMove>> sentWiCraneMoveMap;
    private Map<Integer, List<WICraneMove>> waitWiCraneMoveMap; //所有未发送指令

    private Map<String, WIAreaAbility> areaAbilityMap;
    private Map<String, WIExchangeCnt> wiExchangeCntMap;

    //发箱处理过程中生成的结果
    private Map<String, List<WICraneMove>> waitToSendWiMap; //待发送的指令
    private Map<String, List<WICraneMove>> canAdjustWiMap;

    //test
    private Map<String, WICraneContainer> wiCraneContainerMapD;
    private Map<String, WICraneContainer> wiCraneContainerMapL;

    public WIData(VMSchedule vmSchedule) {
        this.vmSchedule = vmSchedule;
        wiConfiguration = new WIConfiguration();
        logger = new Logger();
        wiCraneMap = new HashMap<>();
        wiWorkBlockList = new ArrayList<>();
        craneWiWorkBlockMap = new HashMap<>();
        wiCraneContainerList = new ArrayList<>();
        workBayKeyWiCraneMoveMap = new HashMap<>();
        sentWiCraneMoveMap = new HashMap<>();
        waitWiCraneMoveMap = new HashMap<>();
        areaAbilityMap = new HashMap<>();
        wiExchangeCntMap = new HashMap<>();
        waitToSendWiMap = new HashMap<>();
        canAdjustWiMap = new HashMap<>();
        wiCraneContainerMapD = new HashMap<>();
        wiCraneContainerMapL = new HashMap<>();
    }

    public Logger getLogger() {
        return logger;
    }

    public SmartWiResults getSmartWiResults() {
        return smartWiResults;
    }

    public void setSmartWiResults(SmartWiResults smartWiResults) {
        this.smartWiResults = smartWiResults;
    }

    public SmartCwpValidatorResults getSmartCwpValidatorResults() {
        return smartCwpValidatorResults;
    }

    public void setSmartCwpValidatorResults(SmartCwpValidatorResults smartCwpValidatorResults) {
        this.smartCwpValidatorResults = smartCwpValidatorResults;
    }

    public WIConfiguration getWiConfiguration() {
        return wiConfiguration;
    }

    public VMSchedule getVmSchedule() {
        return vmSchedule;
    }

    public void addWICrane(WICrane wiCrane) {
        wiCraneMap.put(wiCrane.getCraneNo(), wiCrane);
    }

    public WICrane getWICraneByCraneNo(String craneNo) {
        return wiCraneMap.get(craneNo);
    }

    public List<WICrane> getAllWICraneList() {
        List<WICrane> wiCraneList = new ArrayList<>(wiCraneMap.values());
        PublicMethod.sortWICraneByCraneSeq(wiCraneList);
        return wiCraneList;
    }

    public void addWIWorkBlock(WIWorkBlock wiWorkBlock) {
        String workStatus = wiWorkBlock.getWorkStatus();
        Long planAmount = wiWorkBlock.getPlanAmount();
        Long sentAmount = wiWorkBlock.getSentAmount();
        try {
            long remainAmount = planAmount - sentAmount;
            if (PublicMethod.isSentOrPreToWorkBlock(workStatus)) {
                wiWorkBlock.setRemainAmount(remainAmount);
                wiWorkBlockList.add(wiWorkBlock);
                if (craneWiWorkBlockMap.get(wiWorkBlock.getCraneNo()) == null) {
                    craneWiWorkBlockMap.put(wiWorkBlock.getCraneNo(), new ArrayList<WIWorkBlock>());
                }
                craneWiWorkBlockMap.get(wiWorkBlock.getCraneNo()).add(wiWorkBlock);
            } else {
                logger.logInfo("发现未知的作业块(" + wiWorkBlock.getBlockKey() + ")状态(workStatus: " + workStatus + ")，暂时还没有处理该作业块的逻辑！");
            }
        } catch (Exception e) {
            logger.logError("分析作业块(" + wiWorkBlock.getBlockKey() + ")这些信息(planAmount: " + planAmount + ", sentAmount: " + sentAmount + ", workStatus: " + workStatus + ")时发生异常！");
            e.printStackTrace();
        }
    }

    public List<WIWorkBlock> getAllWIWorkBlockList() {
        return wiWorkBlockList;
    }

    public List<WIWorkBlock> getWIWorkBlockListByHatchId(Long hatchId) {
        List<WIWorkBlock> wiWorkBlockList = new ArrayList<>();
        List<WIWorkBlock> wiWorkBlocks = getAllWIWorkBlockList();
        for (WIWorkBlock wiWorkBlock : wiWorkBlocks) {
            if (wiWorkBlock.getHatchId().equals(hatchId)) {
                wiWorkBlockList.add(wiWorkBlock);
            }
        }
        return wiWorkBlockList;
    }

    public List<String> getAllCraneNoListInWorkBlock() {
        return new ArrayList<>(craneWiWorkBlockMap.keySet());
    }

    public List<WIWorkBlock> getWIWorkBlockListByCraneNo(String craneNo) {
        if (craneWiWorkBlockMap.get(craneNo) != null) {
            return craneWiWorkBlockMap.get(craneNo);
        } else {
            return new ArrayList<>();
        }
    }

    public void addWICraneContainer(WICraneContainer wiCraneContainer) {
        wiCraneContainerList.add(wiCraneContainer);
    }

    public List<WICraneContainer> getAllWICraneContainerList() {
        return wiCraneContainerList;
    }

    public WICraneMove getWICraneMoveByWorkBayKey(String workBayKey) {
        return workBayKeyWiCraneMoveMap.get(workBayKey);
    }

    public void addWICraneMove(WICraneMove wiCraneMove) {
        workBayKeyWiCraneMoveMap.put(wiCraneMove.getWorkBayKey(), wiCraneMove);
    }

    public List<WICraneMove> getAllWICraneMoveList() {
        return new ArrayList<>(workBayKeyWiCraneMoveMap.values());
    }

    public void addSentWICraneMove(WICraneMove wiCraneMove) {
        if (sentWiCraneMoveMap.get(wiCraneMove.getCraneNo()) == null) {
            sentWiCraneMoveMap.put(wiCraneMove.getCraneNo(), new ArrayList<WICraneMove>());
        }
        sentWiCraneMoveMap.get(wiCraneMove.getCraneNo()).add(wiCraneMove);
    }

    public List<WICraneMove> getSentWICraneMoveListByCraneNo(String craneNo) {
        if (sentWiCraneMoveMap.get(craneNo) != null) {
            return sentWiCraneMoveMap.get(craneNo);
        } else {
            return new ArrayList<>();
        }
    }

    public void addWaitWICraneMove(WICraneMove wiCraneMove) {
        if (waitWiCraneMoveMap.get(wiCraneMove.getWorkBayNo()) == null) {
            waitWiCraneMoveMap.put(wiCraneMove.getWorkBayNo(), new ArrayList<WICraneMove>());
        }
        waitWiCraneMoveMap.get(wiCraneMove.getWorkBayNo()).add(wiCraneMove);
    }

    public List<WICraneMove> getWaitWICraneMoveListByWorkBayNo(Integer bayNo) {
        if (waitWiCraneMoveMap.get(bayNo) != null) {
            return waitWiCraneMoveMap.get(bayNo);
        } else {
            return new ArrayList<>();
        }
    }

    public List<WICraneMove> getWaitWICraneMoveListByHatchId(Long hatchId) {
        List<WICraneMove> wiCraneMoveList = new ArrayList<>();
        for (List<WICraneMove> wiCraneMoves : waitWiCraneMoveMap.values()) {
            for (WICraneMove wiCraneMove : wiCraneMoves) {
                if (hatchId.equals(wiCraneMove.getHatchId())) {
                    wiCraneMoveList.add(wiCraneMove);
                }
            }
        }
        return wiCraneMoveList;
    }

    public List<WICraneMove> getSentWICraneMoveListByHatchId(Long hatchId) {
        List<WICraneMove> wiCraneMoveList = new ArrayList<>();
        for (List<WICraneMove> wiCraneMoves : sentWiCraneMoveMap.values()) {
            for (WICraneMove wiCraneMove : wiCraneMoves) {
                if (hatchId.equals(wiCraneMove.getHatchId())) {
                    wiCraneMoveList.add(wiCraneMove);
                }
            }
        }
        return wiCraneMoveList;
    }

    public void addWIAreaAbility(WIAreaAbility wiAreaAbility) {
        areaAbilityMap.put(wiAreaAbility.getAreaNo(), wiAreaAbility);
    }

    public WIAreaAbility getWIAreaAbilityByAreaNo(String areaNo) {
        return areaAbilityMap.get(areaNo);
    }

    public Map<String, WIAreaAbility> getAreaAbilityMap() {
        return areaAbilityMap;
    }

    public void addWIExchangeCnt(WIExchangeCnt wiExchangeCnt) {
        wiExchangeCntMap.put(wiExchangeCnt.getvLocation(), wiExchangeCnt);
    }

    public List<WIExchangeCnt> getAllWiExchangeCntList() {
        return new ArrayList<>(wiExchangeCntMap.values());
    }

    public void addWICraneMoveToWaitToSendMap(WICraneMove wiCraneMove) {
        if (waitToSendWiMap.get(wiCraneMove.getCraneNo()) == null) {
            waitToSendWiMap.put(wiCraneMove.getCraneNo(), new ArrayList<WICraneMove>());
        }
        waitToSendWiMap.get(wiCraneMove.getCraneNo()).add(wiCraneMove);
    }

    public List<WICraneMove> getWICraneMoveListFromWaitToSendMap(String craneNo) {
        if (waitToSendWiMap.get(craneNo) != null) {
            return waitToSendWiMap.get(craneNo);
        } else {
            return new ArrayList<>();
        }
    }

    //test
    public void putVMContainer(VMPosition moSlotPosition, WICraneContainer moContainer) {
        Integer bayNo = moSlotPosition.getBayNo();
        Integer tierNo = moSlotPosition.getTierNo();
        Integer rowNo = moSlotPosition.getRowNo();
        if (bayNo % 2 == 0) {//大倍位
            if (moContainer.getLdFlag().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                wiCraneContainerMapD.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), moContainer);
                wiCraneContainerMapD.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), moContainer);
            }
            if (moContainer.getLdFlag().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                wiCraneContainerMapL.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), moContainer);
                wiCraneContainerMapL.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), moContainer);
            }
        } else {
            if (moContainer.getLdFlag().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                wiCraneContainerMapD.put(moSlotPosition.getVLocation(), moContainer);
            }
            if (moContainer.getLdFlag().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                wiCraneContainerMapL.put(moSlotPosition.getVLocation(), moContainer);
            }
        }
    }

    public WICraneContainer getVMContainerByVMSlot(String vLocation, String dlType) {
        if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            return wiCraneContainerMapD.get(vLocation);
        }
        if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            return wiCraneContainerMapL.get(vLocation);
        }
        return null;
    }
}
