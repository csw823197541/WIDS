package com.wids.manager.data;

import com.wids.model.vessel.*;

import java.util.*;

/**
 * Created by csw on 2017/9/19.
 * Description: 船舶结构信息，单独封装：舱信息、倍位信息、排信息、船箱位信息、驾驶台/烟囱信息等
 */
public class VesselData {

    private String vesselCode; //船舶代码，是船舶结构信息的主键

    //船舶结构信息
    private Map<Long, VMHatch> vmHatchMap; //<hatchId, VMHatch>
    private Map<Long, VMBay> vmBayIdMap; //<bayId, VMBay>
    private Map<String, VMBay> vmBayKeyMap; //<bayKey, VMBay>
    private Map<Long, Map<Integer, VMRow>> vmRowMap; //<bayId, <rowNo, VMRow>>
    private Map<String, VMSlot> vmSlotMap; //记录vLocation船箱位与VMSlot的关系
    private Map<Long, VMHatchCover> vmHatchCoverMap; //<hatchCoverId, VMHatchCover>
    private Map<String, VMMachine> vmMachineMap; //船舶机械信息：<machineNo, VMMachine>

    public VesselData(String vesselCode) {
        this.vesselCode = vesselCode;
        vmHatchMap = new HashMap<>();
        vmBayIdMap = new HashMap<>();
        vmBayKeyMap = new HashMap<>();
        vmRowMap = new HashMap<>();
        vmSlotMap = new HashMap<>();
        vmHatchCoverMap = new HashMap<>();
        vmMachineMap = new HashMap<>();
    }

    public String getVesselCode() {
        return vesselCode;
    }

    public void addVMHatch(VMHatch vmHatch) {
        vmHatchMap.put(vmHatch.getHatchId(), vmHatch);
    }

    public VMHatch getVMHatchByHatchId(Long hatchId) {
        return vmHatchMap.get(hatchId);
    }

    public List<Long> getAllVMHatchIdList() {
        List<Long> hatchIdList = new ArrayList<>(vmHatchMap.keySet());
        Collections.sort(hatchIdList);
        return hatchIdList;
    }

    public List<VMHatch> getAllVMHatchList() {
        List<VMHatch> vmHatchList = new ArrayList<>(vmHatchMap.values());
        Collections.sort(vmHatchList, new Comparator<VMHatch>() {
            @Override
            public int compare(VMHatch o1, VMHatch o2) {
                return o1.getHatchId().compareTo(o2.getHatchId());
            }
        });
        return vmHatchList;
    }

    public void addVMBay(VMBay vmBay) {
        vmBayIdMap.put(vmBay.getBayId(), vmBay);
        vmBayKeyMap.put(vmBay.getBayKey(), vmBay);
    }

    public List<VMBay> getAllVMBays() {
        return new ArrayList<>(vmBayIdMap.values());
    }

    public void addVMRow(VMRow vmRow) {
        Long bayId = vmRow.getBayId();
        if (vmRowMap.get(bayId) == null) {
            vmRowMap.put(bayId, new HashMap<Integer, VMRow>());
        }
        vmRowMap.get(bayId).put(vmRow.getRowNo(), vmRow);
    }

    public void addVMSlot(VMSlot vmSlot) {
        vmSlotMap.put(vmSlot.getVLocation(), vmSlot);
    }

    public VMSlot getVMSlotByVLocation(String vLocation) {
        return vmSlotMap.get(vLocation);
    }

    public VMBay getVMBayByBayId(Long bayId) {
        return vmBayIdMap.get(bayId);
    }

    public List<Integer> getRowNoListByBayId(Long bayId) {
        if (vmRowMap.get(bayId) != null) {
            return new ArrayList<>(vmRowMap.get(bayId).keySet());
        } else {
            return new ArrayList<>();
        }
    }

    public VMRow getVMRowByBayIdAndRowNo(Long bayId, Integer rowNo) {
        return vmRowMap.get(bayId).get(rowNo);
    }

    public void addVMHatchCover(VMHatchCover vmHatchCover) {
        vmHatchCoverMap.put(vmHatchCover.getHatchCoverId(), vmHatchCover);
    }

    public void addVMMachine(VMMachine vmMachine) {
        vmMachineMap.put(vmMachine.getMachineNo(), vmMachine);
    }

    public List<VMMachine> getAllVMMachineList() {
        return new ArrayList<>(vmMachineMap.values());
    }
}
