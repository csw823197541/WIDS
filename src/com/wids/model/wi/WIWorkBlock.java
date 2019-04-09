package com.wids.model.wi;

import com.wids.service.method.PublicMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2017/6/8.
 * Description:
 */
public class WIWorkBlock {

    private String blockKey;//桥机号与作业块桥机序，"craneNo@craneSeq"

    private Long blockId; //作业块Id，标记作业块的唯一性
    private Long berthId; //靠泊ID
    private String craneNo; //桥机ID
    private String bayNo; //倍位No
    private String currentCraneBayNo; //桥机当前所在倍位
    private Long hatchId; //舱ID
    private Double cranePosition;//桥机当前位置
    private Long craneSeq; //作业某个舱所有桥机的作业顺序
    private Long hatchSeq; //某个桥机作业哪些舱的顺序
    private Date workingStartTime; //作业块计划开工时间
    private Date workingEndTime; //作业块计划完工时间
    private String workStatus; //作业块的作业状态
    private Long planAmount; //作业块Move总数
    private Long sentAmount; //已发送的Move数量
    private Long remainAmount; //作业块剩余作业量

    private Date sentWIStartTime; //作业块（包含队列、作业、剩余指令）的开始时间
    private Date estimateStartTime; //根据实际时间估计出来的作业块开始时间，剩余指令开始作业的时间
    private Date estimateEndTime; //根据实际时间估计出来的作业块结束时间
    private Long exCntTime; //作业块交换指令的时间；

    private String deleteFlag; //删除标记，当传入作业块相应倍位m没有待作业的指令时，作业块标记为"Y"，否则为null或者为"N"
    private String blockMessage; //作业块信息，主要是当作业量与实际指令数目不符合的时候，给出提示信息

    private List<WICraneMove> wiCraneMoveList; //该作业块涉及到的所有作业的指令

    public WIWorkBlock(String blockKey) {
        this.blockKey = blockKey;
        wiCraneMoveList = new ArrayList<>();
        exCntTime = 0L;
    }

    public long getBlockWorkTime() {
        long workTime = 0;
        for (WICraneMove wiCraneMove : wiCraneMoveList) {
            workTime += wiCraneMove.getCntWorkTime() * 1000; //ms
        }
        return workTime;
    }

    public long getSentWiWorkTime() {
        long workTime = 0;
        for (WICraneMove wiCraneMove : wiCraneMoveList) {
            if (PublicMethod.isSentStatus(wiCraneMove.getWorkStatus())) {
                workTime += wiCraneMove.getCntWorkTime() * 1000; //ms
            }
        }
        return workTime;
    }

    public long getWaitWiWorkTime() {
        long workTime = 0;
        for (WICraneMove wiCraneMove : wiCraneMoveList) {
            if (PublicMethod.isUnSentStatus(wiCraneMove.getWorkStatus())) {
                workTime += wiCraneMove.getCntWorkTime() * 1000; //ms
            }
        }
        return workTime;
    }

    public String getBlockKey() {
        return blockKey;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getBayNo() {
        return bayNo;
    }

    public void setBayNo(String bayNo) {
        this.bayNo = bayNo;
    }

    public String getCurrentCraneBayNo() {
        return currentCraneBayNo;
    }

    public void setCurrentCraneBayNo(String currentCraneBayNo) {
        this.currentCraneBayNo = currentCraneBayNo;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public Long getPlanAmount() {
        return planAmount;
    }

    public void setPlanAmount(Long planAmount) {
        this.planAmount = planAmount;
    }

    public Double getCranePosition() {
        return cranePosition;
    }

    public void setCranePosition(Double cranePosition) {
        this.cranePosition = cranePosition;
    }

    public Long getCraneSeq() {
        return craneSeq;
    }

    public void setCraneSeq(Long craneSeq) {
        this.craneSeq = craneSeq;
    }

    public Long getHatchSeq() {
        return hatchSeq;
    }

    public void setHatchSeq(Long hatchSeq) {
        this.hatchSeq = hatchSeq;
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

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public Long getSentAmount() {
        return sentAmount;
    }

    public void setSentAmount(Long sentAmount) {
        this.sentAmount = sentAmount;
    }

    public Date getSentWIStartTime() {
        return sentWIStartTime;
    }

    public void setSentWIStartTime(Date sentWIStartTime) {
        this.sentWIStartTime = sentWIStartTime;
    }

    public Date getEstimateStartTime() {
        return estimateStartTime;
    }

    public void setEstimateStartTime(Date estimateStartTime) {
        this.estimateStartTime = estimateStartTime;
    }

    public Date getEstimateEndTime() {
        return estimateEndTime;
    }

    public void setEstimateEndTime(Date estimateEndTime) {
        this.estimateEndTime = estimateEndTime;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Long getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(Long remainAmount) {
        this.remainAmount = remainAmount;
    }

    public String getBlockMessage() {
        return blockMessage;
    }

    public void setBlockMessage(String blockMessage) {
        this.blockMessage = blockMessage;
    }

    public List<WICraneMove> getWiCraneMoveList() {
        return wiCraneMoveList;
    }

    public void setWiCraneMoveList(List<WICraneMove> wiCraneMoveList) {
        this.wiCraneMoveList = wiCraneMoveList;
    }

    public Long getExCntTime() {
        return exCntTime;
    }

    public void setExCntTime(Long exCntTime) {
        this.exCntTime = exCntTime;
    }
}
