package com.wids.manager.method;


import com.wids.manager.data.VesselData;
import com.wids.model.domain.CWPDomain;
import com.wids.model.vessel.*;
import com.wids.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2017/8/14.
 * Description: 船舶结构相关的计算方法集
 */
public class VesselDataMethod {

    private VesselData vesselData;

    public VesselDataMethod(VesselData vesselData) {
        this.vesselData = vesselData;
    }

    public List<VMBay> getVMBayListByHatchId(Long hatchId) {
        List<VMBay> vmBayList = new ArrayList<>();
        List<VMBay> vmBays = vesselData.getAllVMBays();
        for (VMBay vmBay : vmBays) {
            if (vmBay.getHatchId().equals(hatchId)) {
                vmBayList.add(vmBay);
            }
        }
        return vmBayList;
    }

    public List<Integer> getVMBayNoListByHatchId(Long hatchId) {
        Set<Integer> bayNos = new HashSet<>();
        List<VMBay> vmBays = vesselData.getAllVMBays();
        for (VMBay vmBay : vmBays) {
            if (vmBay.getHatchId().equals(hatchId)) {
                bayNos.add(vmBay.getBayNo());
            }
        }
        List<Integer> bayNoList = new ArrayList<>(bayNos);
        Collections.sort(bayNoList);
        return bayNoList;
    }

    public int getMaxTierNoByBayId(Long bayId) {
        List<Integer> rowNoList = vesselData.getRowNoListByBayId(bayId);
        int maxTierNo = -1;
        for (Integer rowNo : rowNoList) {
            VMRow vmRow = vesselData.getVMRowByBayIdAndRowNo(bayId, rowNo);
            maxTierNo = maxTierNo > vmRow.getTopTierNo() ? maxTierNo : vmRow.getTopTierNo();
        }
        return maxTierNo;
    }

    public int getMinTierNoByBayId(Long bayId) {
        List<Integer> rowNoList = vesselData.getRowNoListByBayId(bayId);
        int minTierNo = 1000;
        for (Integer rowNo : rowNoList) {
            VMRow vmRow = vesselData.getVMRowByBayIdAndRowNo(bayId, rowNo);
            minTierNo = minTierNo < vmRow.getBottomTierNo() ? minTierNo : vmRow.getBottomTierNo();
        }
        return minTierNo;
    }

    public List<Integer> getRowSeqListByOddOrEven(Long bayId, String oddOrEven) {
        List<Integer> rowSeqListAsc = vesselData.getRowNoListByBayId(bayId);
        Collections.sort(rowSeqListAsc);
        LinkedList<Integer> rowSeqListEO = new LinkedList<>();
        for (int rowNo : rowSeqListAsc) {
            if (rowNo % 2 == 0) { //偶数排号，添加到前面
                rowSeqListEO.addFirst(rowNo);
            } else {
                rowSeqListEO.addLast(rowNo);
            }
        }
        if (oddOrEven.equals(CWPDomain.ROW_SEQ_EVEN_ODD)) {
            return rowSeqListEO;
        } else {
            Collections.reverse(rowSeqListEO);
            return rowSeqListEO;
        }
    }

    public List<Integer> getRowSeqListByHatchId(Long hatchId, String board, String oddOrEven) {
        LinkedList<Integer> rowNoList = new LinkedList<>();
        Set<Integer> rowNoSet = new HashSet<>();
        List<VMBay> vmBayList = this.getVMBayListByHatchId(hatchId);
        for (VMBay vmBay : vmBayList) {
            if (vmBay.getAboveOrBelow().equals(board)) {
                List<Integer> rowNos = this.getRowSeqListByOddOrEven(vmBay.getBayId(), oddOrEven);
                rowNoSet.addAll(rowNos);
            }
        }
        List<Integer> rowNoListAsc = new ArrayList<>(rowNoSet);
        Collections.sort(rowNoListAsc);
        for (int rowNo : rowNoListAsc) {
            if (rowNo % 2 == 0) { //偶数排号，添加到左边（前面）
                rowNoList.addFirst(rowNo);
            } else {
                rowNoList.addLast(rowNo);
            }
        }
        if (oddOrEven.equals(CWPDomain.ROW_SEQ_ODD_EVEN)) {
            Collections.reverse(rowNoList);
        }
        return rowNoList;
    }

    public VMSlot getPairVMSlot(VMSlot vmSlot) {
        VMBay vmBay = vesselData.getVMBayByBayId(vmSlot.getBayId());
        Integer bayNoPair = getPairBayNoByVMBay(vmBay);
        if (bayNoPair != null) {
            VMPosition vmPosition = new VMPosition(bayNoPair, vmSlot.getVmPosition().getRowNo(), vmSlot.getVmPosition().getTierNo());
            return vesselData.getVMSlotByVLocation(vmPosition.getVLocation());
        } else {
            return null;
        }
    }

    private Integer getPairBayNoByVMBay(VMBay vmBay) {
        List<VMBay> vmBayList = getVMBayListByHatchId(vmBay.getHatchId());
        for (VMBay vmBay1 : vmBayList) {
            if (!vmBay.getBayNo().equals(vmBay1.getBayNo())) {
                return vmBay1.getBayNo();
            }
        }
        return null;
    }

    public List<Integer> getAllVMBayNoListByHatchId(Long hatchId) {
        List<Integer> allBayNoList = new ArrayList<>();
        List<Integer> bayNoList = getVMBayNoListByHatchId(hatchId);
        allBayNoList.addAll(bayNoList);
        if (bayNoList.size() == 2) {
            Integer bayNoD = (bayNoList.get(0) + bayNoList.get(1)) / 2;
            allBayNoList.add(bayNoD);
        }
        Collections.sort(allBayNoList);
        return allBayNoList;
    }

    public Double getVMBayHatchPosition(Integer bayNo, Long hatchId) {
        VMHatch vmHatch = vesselData.getVMHatchByHatchId(hatchId);
        List<Integer> bayNoList = getAllVMBayNoListByHatchId(hatchId);
        Double hatchPo = null;
        if (bayNoList.size() == 1) { //只有一个倍位
            if (bayNo.equals(bayNoList.get(0))) {
                hatchPo = CalculateUtil.add(vmHatch.getHatchPosition(), vmHatch.getHatchLength() / 2);
            }
        } else if (bayNoList.size() == 3) { //否则有三个作业倍位
            if (bayNo.equals(bayNoList.get(0))) {
                hatchPo = CalculateUtil.add(vmHatch.getHatchPosition(), vmHatch.getHatchLength() / 4);
            } else if (bayNo.equals(bayNoList.get(1))) {
                hatchPo = CalculateUtil.add(vmHatch.getHatchPosition(), vmHatch.getHatchLength() / 2);
            } else if (bayNo.equals(bayNoList.get(2))) {
                hatchPo = CalculateUtil.add(vmHatch.getHatchPosition(), vmHatch.getHatchLength() * 3 / 4);
            }
        }
        return hatchPo;
    }
}
