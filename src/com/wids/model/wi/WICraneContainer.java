package com.wids.model.wi;

import java.util.Date;

/**
 * Created by csw on 2017/4/13 11:56.
 * Explain: 指令对象
 */
public class WICraneContainer {

    private Long vpcCntId; //唯一编号
    private String cntKey; //唯一标记
    private Integer bayNo; //作业位置，倍位号
    private Long moveOrder; //桥机作业顺序号
    private String workflow; //作业工艺
    private String workStatus; //指令状态:发送A（队列中）; 完成C,RC; 作业中W; 未发送Y,S,P; 退卸或退装R
    private Long cntWorkTime; //作业时间，秒s

    private String canRecycleFlag; //指令是否可以回收标记，Y或者为空表示可以回收；N表示不可以回收，应该继续执行下去

    private String craneNo; //桥机号
    private String ldFlag; //装卸标志

    private Long berthId; //靠泊ID
    private Long voyageId; //航次Id，进口航次或出口航次
    private Long hatchId; //舱Id
    private String vLocation; //计划放箱位置, 船箱位：倍排层

    private Date workingStartTime; //计划开始时间
    private Date workingEndTime; //计划结束时间

    private Date planStartTime; //计划开始时间
    private Date planEndTime; //计划结束时间

    private String manualFlag; //人工设置（人工指定该位置Move顺序，该Move之前的所有Move由人工决定，CWP算法排之后的顺序。）
    private Long carryOrder; //装车次序
    private String cwoManualWi; //人工锁定的船箱位，发箱时不能作业的箱子，CWP计划排到最后面
    private String cwoManualLocation; //发箱服务箱子交换规则中，指定不允许交换的船箱位

    private String selectedCraneNo; //发送给哪部桥机作业

    private Long sentSeq; //发送出去的指令作业顺序
    private Integer originalOverCntNum; //初始翻箱量
    private Integer exchangeOverCntNum; //交换后的翻箱量
    private Integer overCntNum;

    private WIContainer originalContainer; //初始的箱子
    private WIContainer originalContainerTemp;
    private WIContainer exchangeContainer; //交换后的箱子
    private WIContainer exchangeContainerTemp; //交换后的箱子

    private String exchangeReason; //指令交换原因
    private Date yardWorkingStartTime; //箱区计划作业开始时间
    private Date yardWorkingEndTime; //箱区计划作业结束时间

    public WICraneContainer(Long vpcCntId, String cntKey, Integer bayNo, Long moveOrder, String workflow, String workStatus, Long cntWorkTime) {
        this.vpcCntId = vpcCntId;
        this.cntKey = cntKey;
        this.moveOrder = moveOrder;
        this.workflow = workflow;
        this.workStatus = workStatus;
        this.cntWorkTime = cntWorkTime;
        this.bayNo = bayNo;
    }

    public Integer getTierNo() {
        if (this.vLocation != null) {
            return Integer.valueOf(vLocation.substring(4, vLocation.length()));
        }
        return null;
    }

    public Integer getRowNo() {
        if (this.vLocation != null) {//0,1,2,3,4,5
            return Integer.valueOf(vLocation.substring(2, 4));
        }
        return null;
    }

    public boolean isDoubleWorkflow(WICraneContainer wiCraneContainer) {
        if (wiCraneContainer != null) {
            if (wiCraneContainer.getHatchId().equals(hatchId) && !wiCraneContainer.getCntKey().equals(cntKey)) {
                if (wiCraneContainer.getWorkingStartTime() != null && wiCraneContainer.getWorkingEndTime() != null) {
                    return (wiCraneContainer.getWorkflow().equals(workflow) && wiCraneContainer.getMoveOrder().equals(moveOrder))
                            && (wiCraneContainer.getWorkingStartTime().equals(workingStartTime) && wiCraneContainer.getWorkingEndTime().equals(workingEndTime));
                } else {
                    return (wiCraneContainer.getWorkflow().equals(workflow) && wiCraneContainer.getMoveOrder().equals(moveOrder));
                }
            }
        }
        return false;
    }

    public String getCntKey() {
        return cntKey;
    }

    public Long getVpcCntId() {
        return vpcCntId;
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public Long getMoveOrder() {
        return moveOrder;
    }

    public String getWorkflow() {
        return workflow;
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

    public String getLdFlag() {
        return ldFlag;
    }

    public void setLdFlag(String ldFlag) {
        this.ldFlag = ldFlag;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getVoyageId() {
        return voyageId;
    }

    public void setVoyageId(Long voyageId) {
        this.voyageId = voyageId;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getvLocation() {
        return vLocation;
    }

    public void setvLocation(String vLocation) {
        this.vLocation = vLocation;
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

    public String getManualFlag() {
        return manualFlag;
    }

    public void setManualFlag(String manualFlag) {
        this.manualFlag = manualFlag;
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

    public String getSelectedCraneNo() {
        return selectedCraneNo;
    }

    public void setSelectedCraneNo(String selectedCraneNo) {
        this.selectedCraneNo = selectedCraneNo;
    }

    public Long getSentSeq() {
        return sentSeq;
    }

    public void setSentSeq(Long sentSeq) {
        this.sentSeq = sentSeq;
    }

    public Integer getOriginalOverCntNum() {
        return originalOverCntNum;
    }

    public void setOriginalOverCntNum(Integer originalOverCntNum) {
        this.originalOverCntNum = originalOverCntNum;
    }

    public Integer getExchangeOverCntNum() {
        return exchangeOverCntNum;
    }

    public void setExchangeOverCntNum(Integer exchangeOverCntNum) {
        this.exchangeOverCntNum = exchangeOverCntNum;
    }

    public Integer getOverCntNum() {
        return overCntNum;
    }

    public void setOverCntNum(Integer overCntNum) {
        this.overCntNum = overCntNum;
    }

    public WIContainer getOriginalContainer() {
        return originalContainer;
    }

    public void setOriginalContainer(WIContainer originalContainer) {
        this.originalContainer = originalContainer;
    }

    public WIContainer getExchangeContainer() {
        return exchangeContainer;
    }

    public void setExchangeContainer(WIContainer exchangeContainer) {
        this.exchangeContainer = exchangeContainer;
    }

    public String getCwoManualLocation() {
        return cwoManualLocation;
    }

    public void setCwoManualLocation(String cwoManualLocation) {
        this.cwoManualLocation = cwoManualLocation;
    }

    public WIContainer getExchangeContainerTemp() {
        return exchangeContainerTemp;
    }

    public void setExchangeContainerTemp(WIContainer exchangeContainerTemp) {
        this.exchangeContainerTemp = exchangeContainerTemp;
    }

    public WIContainer getOriginalContainerTemp() {
        return originalContainerTemp;
    }

    public void setOriginalContainerTemp(WIContainer originalContainerTemp) {
        this.originalContainerTemp = originalContainerTemp;
    }

    public String getExchangeReason() {
        return exchangeReason;
    }

    public void setExchangeReason(String exchangeReason) {
        this.exchangeReason = exchangeReason;
    }

    public Date getYardWorkingStartTime() {
        return yardWorkingStartTime;
    }

    public void setYardWorkingStartTime(Date yardWorkingStartTime) {
        this.yardWorkingStartTime = yardWorkingStartTime;
    }

    public Date getYardWorkingEndTime() {
        return yardWorkingEndTime;
    }

    public void setYardWorkingEndTime(Date yardWorkingEndTime) {
        this.yardWorkingEndTime = yardWorkingEndTime;
    }

    public String getCanRecycleFlag() {
        return canRecycleFlag;
    }

    public void setCanRecycleFlag(String canRecycleFlag) {
        this.canRecycleFlag = canRecycleFlag;
    }

    public Date getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(Date planStartTime) {
        this.planStartTime = planStartTime;
    }

    public Date getPlanEndTime() {
        return planEndTime;
    }

    public void setPlanEndTime(Date planEndTime) {
        this.planEndTime = planEndTime;
    }
}
