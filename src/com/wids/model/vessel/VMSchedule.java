package com.wids.model.vessel;


import java.util.Date;

/**
 * Created by csw on 2017/4/20 11:28.
 * Explain: 船与船期信息
 */
public class VMSchedule {

    private Long berthId;//靠泊ID
    private String vesselCode;//船舶CD
    private String vesselType;//船舶类型，FCS大船，BAR驳船
    private Date planBeginWorkTime;//计划开工时间
    private Date planEndWorkTime;//计划完工时间
    private Long planStartPst;//船头停泊位置
    private Long planEndPst;//船尾停泊位置
    private String planBerthDirect;//靠泊方向,R:正向，L:反向(奇数排号靠近海侧)
    private Boolean sendWorkInstruction;//是否发送该船指令标记

    public VMSchedule(Long berthId, String vesselCode) {
        this.berthId = berthId;
        this.vesselCode = vesselCode;
    }

    public Long getBerthId() {
        return berthId;
    }

    public String getVesselCode() {
        return vesselCode;
    }

    public String getVesselType() {
        return vesselType;
    }

    public void setVesselType(String vesselType) {
        this.vesselType = vesselType;
    }

    public Date getPlanBeginWorkTime() {
        return planBeginWorkTime;
    }

    public void setPlanBeginWorkTime(Date planBeginWorkTime) {
        this.planBeginWorkTime = planBeginWorkTime;
    }

    public Date getPlanEndWorkTime() {
        return planEndWorkTime;
    }

    public void setPlanEndWorkTime(Date planEndWorkTime) {
        this.planEndWorkTime = planEndWorkTime;
    }

    public Long getPlanStartPst() {
        return planStartPst;
    }

    public void setPlanStartPst(Long planStartPst) {
        this.planStartPst = planStartPst;
    }

    public Long getPlanEndPst() {
        return planEndPst;
    }

    public void setPlanEndPst(Long planEndPst) {
        this.planEndPst = planEndPst;
    }

    public String getPlanBerthDirect() {
        return planBerthDirect;
    }

    public void setPlanBerthDirect(String planBerthDirect) {
        this.planBerthDirect = planBerthDirect;
    }

    public Boolean getSendWorkInstruction() {
        return sendWorkInstruction;
    }

    public void setSendWorkInstruction(Boolean sendWorkInstruction) {
        this.sendWorkInstruction = sendWorkInstruction;
    }
}
