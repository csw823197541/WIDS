package com.wids.model.wi;

/**
 * Created by csw on 2017/4/13 22:12.
 * Explain: 指令相关的配置参数
 */
public class WIConfiguration {

    private Long berthId; //靠泊Id
    private Long intervalTime;//获取某时间段的指令
    private Double craneSafeSpan; //桥机安全距离
    private Boolean crossBridge; //过驾驶台是否起大梁
    private Boolean crossChimney; //过烟囱是否起大梁
    private Long crossBarTime;//桥机起大梁移动时间，15分钟
    private String deckRowExchange; //允许甲板上同槽交换，Y表示允许，N或null表示不允许
    private String deckBayExchange; //允许甲板上同舱（槽与槽）交换，Y表示允许，N或null表示不允许
    private String hatchRowExchange; //允许舱下同槽交换，Y表示允许，N或null表示不允许
    private String hatchBayExchange; //允许舱下同舱（槽与槽）交换，Y表示允许，N或null表示不允许
    private String deckAndHatchExchange; //允许甲板上/下间交换，Y表示允许，N或null表示不允许
    private String allVesselExchange; //允许整船（舱与舱）交换，Y表示允许，N或null表示不允许
    private String emptyCntExchange; //允许空箱整船交换，如果为N或者null，依照上诉规则交换
    private String moveOrderExchange; //是否允许交换作业顺序
    private Long deckWeightDifference; //甲板上箱子交换重量差参数
    private Long hatchWeightDifference; //舱下箱子交换重量差参数
    private Long hatchSideWeightDifference; //甲板上里外档两跟槽重量差参数
    private Integer deckTierNum; //甲板上发箱层高限制
    private Integer hatchTierNum; //舱内发箱层高限制
    private Integer sendContainerNum; // 任意多少分钟发送多少个箱子，发箱数目
    private Long sendIntervalTime; // 任意多少分钟发送多少个箱子，时间参数

    public Integer getSendContainerNum() {
        return sendContainerNum;
    }

    public void setSendContainerNum(Integer sendContainerNum) {
        this.sendContainerNum = sendContainerNum;
    }

    public Long getSendIntervalTime() {
        return sendIntervalTime;
    }

    public void setSendIntervalTime(Long sendIntervalTime) {
        this.sendIntervalTime = sendIntervalTime;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public Double getCraneSafeSpan() {
        return craneSafeSpan;
    }

    public void setCraneSafeSpan(Double craneSafeSpan) {
        this.craneSafeSpan = craneSafeSpan;
    }

    public Long getDeckWeightDifference() {
        return deckWeightDifference;
    }

    public void setDeckWeightDifference(Long deckWeightDifference) {
        this.deckWeightDifference = deckWeightDifference;
    }

    public Long getHatchWeightDifference() {
        return hatchWeightDifference;
    }

    public void setHatchWeightDifference(Long hatchWeightDifference) {
        this.hatchWeightDifference = hatchWeightDifference;
    }

    public Long getHatchSideWeightDifference() {
        return hatchSideWeightDifference;
    }

    public void setHatchSideWeightDifference(Long hatchSideWeightDifference) {
        this.hatchSideWeightDifference = hatchSideWeightDifference;
    }

    public Boolean getCrossBridge() {
        return crossBridge;
    }

    public void setCrossBridge(Boolean crossBridge) {
        this.crossBridge = crossBridge;
    }

    public Boolean getCrossChimney() {
        return crossChimney;
    }

    public void setCrossChimney(Boolean crossChimney) {
        this.crossChimney = crossChimney;
    }

    public Long getCrossBarTime() {
        return crossBarTime;
    }

    public void setCrossBarTime(Long crossBarTime) {
        this.crossBarTime = crossBarTime;
    }

    public String getDeckRowExchange() {
        return deckRowExchange;
    }

    public void setDeckRowExchange(String deckRowExchange) {
        this.deckRowExchange = deckRowExchange;
    }

    public String getDeckBayExchange() {
        return deckBayExchange;
    }

    public void setDeckBayExchange(String deckBayExchange) {
        this.deckBayExchange = deckBayExchange;
    }

    public String getHatchRowExchange() {
        return hatchRowExchange;
    }

    public void setHatchRowExchange(String hatchRowExchange) {
        this.hatchRowExchange = hatchRowExchange;
    }

    public String getHatchBayExchange() {
        return hatchBayExchange;
    }

    public void setHatchBayExchange(String hatchBayExchange) {
        this.hatchBayExchange = hatchBayExchange;
    }

    public String getDeckAndHatchExchange() {
        return deckAndHatchExchange;
    }

    public void setDeckAndHatchExchange(String deckAndHatchExchange) {
        this.deckAndHatchExchange = deckAndHatchExchange;
    }

    public String getAllVesselExchange() {
        return allVesselExchange;
    }

    public void setAllVesselExchange(String allVesselExchange) {
        this.allVesselExchange = allVesselExchange;
    }

    public String getEmptyCntExchange() {
        return emptyCntExchange;
    }

    public void setEmptyCntExchange(String emptyCntExchange) {
        this.emptyCntExchange = emptyCntExchange;
    }

    public String getMoveOrderExchange() {
        return moveOrderExchange;
    }

    public void setMoveOrderExchange(String moveOrderExchange) {
        this.moveOrderExchange = moveOrderExchange;
    }

    public Integer getDeckTierNum() {
        return deckTierNum;
    }

    public void setDeckTierNum(Integer deckTierNum) {
        this.deckTierNum = deckTierNum;
    }

    public Integer getHatchTierNum() {
        return hatchTierNum;
    }

    public void setHatchTierNum(Integer hatchTierNum) {
        this.hatchTierNum = hatchTierNum;
    }
}
