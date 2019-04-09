package com.wids.model.wi;

import java.util.Date;

/**
 * Created by csw on 2017/4/13 15:13.
 * Explain:
 */
public class WIAreaTask {

    private Long berthId; //靠泊ID
    private String vpcCntrId; //唯一编号
    private String craneNo; //桥机号
    private Long cwpwkmovenum; //桥机作业顺序号
    private String lduld; //装卸标志
    private String yardContainerId; //箱Id号
    private String cszCsizecd; //箱尺寸
    private String yLocation; //计划提箱位置, 场箱位：倍.排.层
    private String vLocation; //计划放箱位置, 船箱位：倍.排.层
    private Date workingStartTime; //计划开始时间
    private Date workingEndTime; //计划结束时间
    private String moveKind; //作业类型（DSCH，LOAD，SHIFTIN，SHIFTOUT（转堆））
    private String workFlow; //作业工艺
    private Long voyId; //航次Id，进口航次或出口航次
    private String workStatus; //指令状态:发送A; 完成C,RC; 作业中W; 未发送Y,S,P; 退卸或退装R
    private String workIsExchangeLabel; //指令是否可以互相交换标识，标识相同的指令即满足交换规则

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public String getVpcCntrId() {
        return vpcCntrId;
    }

    public void setVpcCntrId(String vpcCntrId) {
        this.vpcCntrId = vpcCntrId;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public Long getCwpwkmovenum() {
        return cwpwkmovenum;
    }

    public void setCwpwkmovenum(Long cwpwkmovenum) {
        this.cwpwkmovenum = cwpwkmovenum;
    }

    public String getLduld() {
        return lduld;
    }

    public void setLduld(String lduld) {
        this.lduld = lduld;
    }

    public String getYardContainerId() {
        return yardContainerId;
    }

    public void setYardContainerId(String yardContainerId) {
        this.yardContainerId = yardContainerId;
    }

    public String getCszCsizecd() {
        return cszCsizecd;
    }

    public void setCszCsizecd(String cszCsizecd) {
        this.cszCsizecd = cszCsizecd;
    }

    public String getyLocation() {
        return yLocation;
    }

    public void setyLocation(String yLocation) {
        this.yLocation = yLocation;
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

    public String getMoveKind() {
        return moveKind;
    }

    public void setMoveKind(String moveKind) {
        this.moveKind = moveKind;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    public Long getVoyId() {
        return voyId;
    }

    public void setVoyId(Long voyId) {
        this.voyId = voyId;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getWorkIsExchangeLabel() {
        return workIsExchangeLabel;
    }

    public void setWorkIsExchangeLabel(String workIsExchangeLabel) {
        this.workIsExchangeLabel = workIsExchangeLabel;
    }
}
