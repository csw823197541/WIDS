package com.wids.service.method;

import com.wids.manager.data.AllData;
import com.wids.manager.data.VesselData;
import com.wids.manager.data.WIData;
import com.wids.manager.method.VesselDataMethod;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDefaultValue;
import com.wids.model.domain.WIDomain;
import com.wids.model.log.Logger;
import com.wids.model.vessel.VMMachine;
import com.wids.model.vessel.VMPosition;
import com.wids.model.vessel.VMSchedule;
import com.wids.model.wi.*;
import com.wids.utils.BeanCopyUtil;
import com.wids.utils.CalculateUtil;
import com.wids.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2017/10/12.
 * Description:
 */
public class WIDataMethod {

    private VesselData vesselData;
    private AllData allData;

    public WIDataMethod(VesselData vesselData, AllData allData) {
        this.vesselData = vesselData;
        this.allData = allData;
    }

    public void generateData(WIData wiData) {

        //填充WIWorkBlock对象中桥机作业位置坐标
        generateCraneWorkPosition(wiData);

        //生成WICraneMove对象
        generateWICraneMove(wiData);

        //给wiData添加所有作业块涉及到的桥机对象
        generateWICrane(wiData);
    }

    private void generateCraneWorkPosition(WIData wiData) {
        Logger logger = wiData.getLogger();
        List<WIWorkBlock> wiWorkBlockList = wiData.getAllWIWorkBlockList();
        VMSchedule vmSchedule = wiData.getVmSchedule();
        for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
            Long hatchId = wiWorkBlock.getHatchId();
            Integer bayNo = Integer.valueOf(wiWorkBlock.getBayNo());
            VesselDataMethod vesselDataMethod = new VesselDataMethod(vesselData);
            try {
                Double bayHatchPo = vesselDataMethod.getVMBayHatchPosition(bayNo, hatchId);
                if (bayHatchPo != null) {
                    double distance1 = CalculateUtil.add(vmSchedule.getPlanStartPst(), bayHatchPo);
                    double distance2 = CalculateUtil.sub(vmSchedule.getPlanEndPst(), bayHatchPo);
                    double po = vmSchedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L) ? distance1 : distance2;
                    wiWorkBlock.setCranePosition(po);
                } else {
                    logger.logError("在计算作业块(blockKey:" + wiWorkBlock.getBlockKey() + ")位置坐标时，找不到舱内的倍位坐标！");
                }
            } catch (Exception e) {
                logger.logError("在计算作业块(blockKey:" + wiWorkBlock.getBlockKey() + ")位置坐标时，发生异常！");
                e.printStackTrace();
            }
        }
        List<VMMachine> vmMachineList = vesselData.getAllVMMachineList();
        for (VMMachine vmMachine : vmMachineList) {
            Double vmMachinePo = vmMachine.getMachinePosition();
            if (vmMachinePo != null) {
                double distance1 = CalculateUtil.add(vmSchedule.getPlanStartPst(), vmMachinePo);
                double distance2 = CalculateUtil.sub(vmSchedule.getPlanEndPst(), vmMachinePo);
                double po = vmSchedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L) ? distance1 : distance2;
                vmMachine.setMachinePosition(po);
            }
        }
    }

    private void generateWICraneMove(WIData wiData) {
        Logger logger = wiData.getLogger();
        List<WICraneContainer> wiCraneContainerList = wiData.getAllWICraneContainerList();
        PublicMethod.sortWICraneContainerByMoveOrder(wiCraneContainerList);
        //改变指令状态，回收指令
//        analyzeWiCraneContainer(wiCraneContainerList);
        List<WICraneContainer> wiCraneContainerTempList = new ArrayList<>();
        String workBayKeyTemp;
        for (WICraneContainer wiCraneContainer : wiCraneContainerList) {
            wiCraneContainerTempList.add(wiCraneContainer);
            Long vpcCntId = wiCraneContainer.getVpcCntId();
            String vLocation = wiCraneContainer.getvLocation();
            Integer bayNo = wiCraneContainer.getBayNo();
            Long moveOrder = wiCraneContainer.getMoveOrder();
            try {
                String workBayKey = StringUtil.getKey(bayNo, moveOrder.toString());
                boolean isSingle = true;
                for (WICraneContainer wiCraneContainerTemp : wiCraneContainerTempList) {
                    if (wiCraneContainer.isDoubleWorkflow(wiCraneContainerTemp)) {
                        isSingle = false;
                        Integer workBayNoD = (bayNo + wiCraneContainerTemp.getBayNo()) / 2; //大倍位
                        String workBayKeyD = StringUtil.getKey(workBayNoD, moveOrder.toString());
                        workBayKeyTemp = StringUtil.getKey(wiCraneContainerTemp.getBayNo(), wiCraneContainerTemp.getMoveOrder().toString());
                        WICraneMove wiCraneMove = wiData.getWICraneMoveByWorkBayKey(workBayKeyTemp);
                        if (wiCraneMove != null) {
                            wiCraneMove.setWorkBayNo(workBayNoD);
                            wiCraneMove.setWorkBayKey(workBayKeyD);
                            wiCraneMove.getWiCraneContainerList().add(wiCraneContainer);
                        } else {
                            logger.logError("指令根据(bayNo@moveOrder: " + workBayKey + ")查找不到双箱作业的WICraneMove对象，仔细检查该倍位双箱吊工艺的指令数据！");
                        }
                        break;
                    }
                }
                if (isSingle) {
                    WICraneMove wiCraneMove = new WICraneMove();
                    wiCraneMove = (WICraneMove) BeanCopyUtil.copyBean(wiCraneContainer, wiCraneMove);
                    wiCraneMove.setWorkBayNo(bayNo);
                    wiCraneMove.setWorkBayKey(workBayKey);
                    VMPosition vmPosition = new VMPosition(wiCraneContainer.getvLocation());
                    wiCraneMove.setRowNo(vmPosition.getRowNo());
                    wiCraneMove.setTierNo(vmPosition.getTierNo());
                    wiCraneMove.getWiCraneContainerList().add(wiCraneContainer);
                    wiCraneMove.setSentSeq(WIDefaultValue.sentSeq);
                    addWICraneMoveToWiData(wiCraneMove, wiData);
                }
            } catch (Exception e) {
                logger.logError("解析WICraneContainer(vpcCntId: " + vpcCntId + ", vLocation: " + vLocation + ")生成WICraneMove过程中发生异常！");
                e.printStackTrace();
            }
        }
    }

    private void analyzeWiCraneContainer(List<WICraneContainer> wiCraneContainerList) {
        Map<String, List<WICraneContainer>> wiCntMap = new HashMap<>(); //<areaNo,>
        String areaNo;
        for (WICraneContainer wiCraneContainer : wiCraneContainerList) {
            if (wiCraneContainer.getLdFlag().equals(CWPDomain.DL_TYPE_LOAD) && PublicMethod.isSentStatus(wiCraneContainer.getWorkStatus())) {
                if (wiCraneContainer.getOriginalContainer().getyLocation() != null && CWPDomain.YES.equals(wiCraneContainer.getCanRecycleFlag())) {
                    areaNo = wiCraneContainer.getOriginalContainer().getAreaNo();
                    if (wiCntMap.get(areaNo) == null) {
                        wiCntMap.put(areaNo, new ArrayList<WICraneContainer>());
                    }
                    wiCntMap.get(areaNo).add(wiCraneContainer);
                }
            }
        }
        List<WICraneContainer> recycleCntList = new ArrayList<>();
        Set<String> areaNoSet = wiCntMap.keySet();
        for (String areaNo1 : areaNoSet) {
            List<WICraneContainer> wiCraneContainerList1 = wiCntMap.get(areaNo1);
            Collections.sort(wiCraneContainerList1, new Comparator<WICraneContainer>() {
                @Override
                public int compare(WICraneContainer o1, WICraneContainer o2) {
                    return o1.getPlanStartTime().compareTo(o2.getPlanEndTime());
                }
            });
            int n = 2; //每个箱区保留条计划作业时间最靠前的指令
            if (wiCraneContainerList1.size() > n) {//0，1，2，3
                List<WICraneContainer> wiCntTempList = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    wiCntTempList.add(wiCraneContainerList1.get(i));
                }
                boolean doubleFlag = false;
                for (WICraneContainer wiCraneContainer : wiCntTempList) {
                    if (wiCraneContainerList1.get(n).isDoubleWorkflow(wiCraneContainer)) {
                        doubleFlag = true;
                    }
                }
                int m = doubleFlag ? n + 1 : n;
                for (int i = m; i < wiCraneContainerList1.size(); i++) {
                    WICraneContainer wiCraneContainer = wiCraneContainerList1.get(i);
                    wiCraneContainer.setWorkStatus(WIDomain.S);
                    recycleCntList.add(wiCraneContainer);
                }
            }
        }
        //验证回收的合理性
        for (WICraneContainer wiCraneContainer : recycleCntList) {
            if (hasCntInUp(wiCraneContainer, wiCntMap)) {
                wiCraneContainer.setWorkStatus(WIDomain.A);
            }
        }
    }

    private boolean hasCntInUp(WICraneContainer wiCraneContainer, Map<String, List<WICraneContainer>> wiCntMap) {
        for (List<WICraneContainer> wiCraneContainers : wiCntMap.values()) {
            for (WICraneContainer wiCraneContainer1 : wiCraneContainers) {
                if (PublicMethod.isSentStatus(wiCraneContainer1.getWorkStatus())) {
                    if (wiCraneContainer1.getHatchId().equals(wiCraneContainer.getHatchId())) {
                        if (wiCraneContainer1.getRowNo().equals(wiCraneContainer.getRowNo())) {
                            if (wiCraneContainer1.getTierNo() > wiCraneContainer.getTierNo()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addWICraneMoveToWiData(WICraneMove wiCraneMove, WIData wiData) {
        wiData.addWICraneMove(wiCraneMove);
        Logger logger = wiData.getLogger();
        String workBayKey = wiCraneMove.getWorkBayKey();
        String workStatus = wiCraneMove.getWorkStatus();
        if (PublicMethod.isSentStatus(workStatus)) {
            String craneNo = wiCraneMove.getCraneNo();
            if (craneNo != null) {
                wiData.addSentWICraneMove(wiCraneMove);
            } else {
                logger.logError("在已发送(队列或作业状态)的指令中，存在没有指定桥机号的指令(bayNo@moveOrder: " + workBayKey + ")！");
                try {
                    throw new Exception("在已发送(队列或作业状态)的指令中，存在没有指定桥机号的指令(bayNo@moveOrder: " + workBayKey + ")！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (PublicMethod.isUnSentStatus(workStatus)) {
            wiData.addWaitWICraneMove(wiCraneMove);
        } else {
            if (!PublicMethod.isWorkDoneStatus(workStatus)) {
                logger.logInfo("在传入所有指令中，存在状态为(" + wiCraneMove.getWorkStatus() + ")的指令(" + workBayKey + ")，暂时没有考虑到！");
            }
        }
    }

    private void generateWICrane(WIData wiData) {
        Logger logger = wiData.getLogger();
        List<String> craneNoList = wiData.getAllCraneNoListInWorkBlock();
        for (String craneNo : craneNoList) {
            WICrane wiCrane = allData.getWICraneByCraneNo(craneNo);
            if (wiCrane != null) {
                wiData.addWICrane(wiCrane);
            } else {
                logger.logError("作业块涉及到的桥机(No:" + craneNo + ")作业计划信息没有！");
            }
        }
    }

    public VesselData getVesselData() {
        return vesselData;
    }

    public WICraneMaintainPlan getWICraneMaintainPlanByCraneNo(String craneNo) {
        return allData.getWICraneMaintainPlanByCraneNo(craneNo);
    }
}
