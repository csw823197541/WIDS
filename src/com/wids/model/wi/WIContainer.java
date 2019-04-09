package com.wids.model.wi;

import java.util.Date;

/**
 * Created by csw on 2018/1/31.
 * Description:
 */
public class WIContainer {

    private String yLocation; //计划提箱位置, 场箱位：倍排层
    private String yardContainerId; //箱Id号
    private String workStatus; //堆场箱子当前作业状态:发送A（队列中）; 完成C,RC; 作业中W; 未发送Y,S,P; 退卸或退装R
    private String moveStage;

    private String size; //箱尺寸
    private String type; //箱型
    private String dstPort; //目的港
    private String rfFlag;	//冷藏箱标记
    private String efFlag; //empty or full，箱空重
    private String dgCd; //危险品代码
    private String isHeight; // 是否高箱
    private String cntHeight; //箱子的具体高度
    private String overrunCd; //超限代码
    private Double weightKg; //重量（kg）
    private String cwoManualLocation; //人工指定不能交换的箱子
    private Date workingStartTime; //计划开始时间
    private Date workingEndTime; //计划结束时间

    private Long sentSeq; //发送出去的指令作业顺序
    private Integer overCntNumTemp; //翻箱量（在比较合适交换的箱子时候参考使用）

    private WICraneContainer originalCraneContainer; //原始的船箱位
    private WICraneContainer exchangeCraneContainer; //交换后的船箱位

    public WIContainer(String yLocation) {
        this.yLocation = yLocation;
    }

    public String getAreaNo() {
        if (this.getyLocation() != null) {
            return yLocation.substring(0, 2);
        }
        return null;
    }

    public String getyLocation() {
        return yLocation;
    }

    public void setyLocation(String yLocation) {
        this.yLocation = yLocation;
    }

    public String getYardContainerId() {
        return yardContainerId;
    }

    public void setYardContainerId(String yardContainerId) {
        this.yardContainerId = yardContainerId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    public String getRfFlag() {
        return rfFlag;
    }

    public void setRfFlag(String rfFlag) {
        this.rfFlag = rfFlag;
    }

    public String getEfFlag() {
        return efFlag;
    }

    public void setEfFlag(String efFlag) {
        this.efFlag = efFlag;
    }

    public String getDgCd() {
        return dgCd;
    }

    public void setDgCd(String dgCd) {
        this.dgCd = dgCd;
    }

    public String getIsHeight() {
        return isHeight;
    }

    public void setIsHeight(String isHeight) {
        this.isHeight = isHeight;
    }

    public String getCntHeight() {
        return cntHeight;
    }

    public void setCntHeight(String cntHeight) {
        this.cntHeight = cntHeight;
    }

    public String getOverrunCd() {
        return overrunCd;
    }

    public void setOverrunCd(String overrunCd) {
        this.overrunCd = overrunCd;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public WICraneContainer getOriginalCraneContainer() {
        return originalCraneContainer;
    }

    public void setOriginalCraneContainer(WICraneContainer originalCraneContainer) {
        this.originalCraneContainer = originalCraneContainer;
    }

    public Long getSentSeq() {
        return sentSeq;
    }

    public void setSentSeq(Long sentSeq) {
        this.sentSeq = sentSeq;
    }

    public Integer getOverCntNumTemp() {
        return overCntNumTemp;
    }

    public void setOverCntNumTemp(Integer overCntNumTemp) {
        this.overCntNumTemp = overCntNumTemp;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public WICraneContainer getExchangeCraneContainer() {
        return exchangeCraneContainer;
    }

    public void setExchangeCraneContainer(WICraneContainer exchangeCraneContainer) {
        this.exchangeCraneContainer = exchangeCraneContainer;
    }

    public String getMoveStage() {
        return moveStage;
    }

    public void setMoveStage(String moveStage) {
        this.moveStage = moveStage;
    }

    public String getCwoManualLocation() {
        return cwoManualLocation;
    }

    public void setCwoManualLocation(String cwoManualLocation) {
        this.cwoManualLocation = cwoManualLocation;
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
}
