package com.wids.model.wi;

import com.wids.model.domain.WIDomain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2017/6/22.
 * Description:
 */
public class WICraneMove {

    private String workBayKey; //主键，"bayNo@moveOrder"
    private Integer workBayNo; //作业位置，倍位号

    private Integer rowNo;
    private Integer tierNo;

    private Long moveOrder; //桥机作业顺序号
    private String workflow; //作业工艺
    private String workStatus;
    private Long cntWorkTime; //作业时间，秒s

    private String craneNo; //桥机号
    private String ldFlag; //装卸标志
    private Long hatchId; //舱Id

    private Date workingStartTime; //计划开始时间
    private Date workingEndTime; //计划结束时间

    private String selectedWorkBlock;
    private Long carryOrder;
    private String cwoManualWi;
    private Long sentSeq;

    private Integer valueTemp;
    private WIExchangeValue wiExchangeValue; //交换规则与原因

    private List<WICraneContainer> wiCraneContainerList;

    public WICraneMove() {
        wiCraneContainerList = new ArrayList<>();
        valueTemp = 300;
    }

    public boolean isOverrunCnt() {
        for (WICraneContainer wiCraneContainer : wiCraneContainerList) {
            WIContainer wiContainer = wiCraneContainer.getOriginalContainer();
            if (WIDomain.OVERRUN_O.equals(wiContainer.getOverrunCd()) || WIDomain.OVERRUN_OW.equals(wiContainer.getOverrunCd())) {
                return true;
            }
        }
        return false;
    }

    public String getWorkBayKey() {
        return workBayKey;
    }

    public void setWorkBayKey(String workBayKey) {
        this.workBayKey = workBayKey;
    }

    public Integer getWorkBayNo() {
        return workBayNo;
    }

    public void setWorkBayNo(Integer workBayNo) {
        this.workBayNo = workBayNo;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public Integer getTierNo() {
        return tierNo;
    }

    public void setTierNo(Integer tierNo) {
        this.tierNo = tierNo;
    }

    public Long getMoveOrder() {
        return moveOrder;
    }

    public void setMoveOrder(Long moveOrder) {
        this.moveOrder = moveOrder;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public Long getCntWorkTime() {
        return cntWorkTime;
    }

    public void setCntWorkTime(Long cntWorkTime) {
        this.cntWorkTime = cntWorkTime;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getLdFlag() {
        return ldFlag;
    }

    public void setLdFlag(String ldFlag) {
        this.ldFlag = ldFlag;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public Date getWorkingStartTime() {
        return workingStartTime;
    }

    public void setWorkingStartTime(Date workingStartTime) {
        this.workingStartTime = workingStartTime;
    }

    public Date getWorkingEndTime() {
        return workingEndTime;
    }

    public void setWorkingEndTime(Date workingEndTime) {
        this.workingEndTime = workingEndTime;
    }

    public String getSelectedWorkBlock() {
        return selectedWorkBlock;
    }

    public void setSelectedWorkBlock(String selectedWorkBlock) {
        this.selectedWorkBlock = selectedWorkBlock;
    }

    public Long getCarryOrder() {
        return carryOrder;
    }

    public void setCarryOrder(Long carryOrder) {
        this.carryOrder = carryOrder;
    }

    public String getCwoManualWi() {
        return cwoManualWi;
    }

    public void setCwoManualWi(String cwoManualWi) {
        this.cwoManualWi = cwoManualWi;
    }

    public Long getSentSeq() {
        return sentSeq;
    }

    public void setSentSeq(Long sentSeq) {
        this.sentSeq = sentSeq;
    }

    public List<WICraneContainer> getWiCraneContainerList() {
        return wiCraneContainerList;
    }

    public void setWiCraneContainerList(List<WICraneContainer> wiCraneContainerList) {
        this.wiCraneContainerList = wiCraneContainerList;
    }

    public Integer getValueTemp() {
        return valueTemp;
    }

    public void setValueTemp(Integer valueTemp) {
        this.valueTemp = valueTemp;
    }

    public WIExchangeValue getWiExchangeValue() {
        return wiExchangeValue;
    }

    public void setWiExchangeValue(WIExchangeValue wiExchangeValue) {
        this.wiExchangeValue = wiExchangeValue;
    }
}
