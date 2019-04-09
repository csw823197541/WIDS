package com.wids.model.vessel;


/**
 * Created by csw on 2017/4/20 13:55.
 * Explain: 舱盖板信息
 */
public class VMHatchCover {

    private String vesselCode;//船舶代码
    private Long hatchCoverId;//舱盖板ID
    private String hatchCoverNo;//舱盖板编号
    private Integer openSeq;//开舱顺序 （暂不考虑）
    private String hatchFromRowNo;//舱上开始排
    private String hatchToRowNo; //舱上结束排
    private Long bayIdFrom;//开始倍位
    private Long bayIdTo;//结束倍位
    private String deckFromRowNo;//甲板上开始排
    private String deckToRowNo;// 甲板上结束排
    private Long leftCoverFather;// 左边父甲板编号
    private Long rightCoverFather; // 右边父甲板编号
    private Long frontCoverFather; // 前边父甲板编号
    private Long behindCoverFather; // 后边父甲板编号

    public VMHatchCover(String vesselCode, Long hatchCoverId) {
        this.vesselCode = vesselCode;
        this.hatchCoverId = hatchCoverId;
    }

    public String getVesselCode() {
        return vesselCode;
    }

    public Long getHatchCoverId() {
        return hatchCoverId;
    }

    public String getHatchCoverNo() {
        return hatchCoverNo;
    }

    public void setHatchCoverNo(String hatchCoverNo) {
        this.hatchCoverNo = hatchCoverNo;
    }

    public Integer getOpenSeq() {
        return openSeq;
    }

    public void setOpenSeq(Integer openSeq) {
        this.openSeq = openSeq;
    }

    public String getHatchFromRowNo() {
        return hatchFromRowNo;
    }

    public void setHatchFromRowNo(String hatchFromRowNo) {
        this.hatchFromRowNo = hatchFromRowNo;
    }

    public String getHatchToRowNo() {
        return hatchToRowNo;
    }

    public void setHatchToRowNo(String hatchToRowNo) {
        this.hatchToRowNo = hatchToRowNo;
    }

    public Long getBayIdFrom() {
        return bayIdFrom;
    }

    public void setBayIdFrom(Long bayIdFrom) {
        this.bayIdFrom = bayIdFrom;
    }

    public Long getBayIdTo() {
        return bayIdTo;
    }

    public void setBayIdTo(Long bayIdTo) {
        this.bayIdTo = bayIdTo;
    }

    public String getDeckFromRowNo() {
        return deckFromRowNo;
    }

    public void setDeckFromRowNo(String deckFromRowNo) {
        this.deckFromRowNo = deckFromRowNo;
    }

    public String getDeckToRowNo() {
        return deckToRowNo;
    }

    public void setDeckToRowNo(String deckToRowNo) {
        this.deckToRowNo = deckToRowNo;
    }

    public Long getLeftCoverFather() {
        return leftCoverFather;
    }

    public void setLeftCoverFather(Long leftCoverFather) {
        this.leftCoverFather = leftCoverFather;
    }

    public Long getRightCoverFather() {
        return rightCoverFather;
    }

    public void setRightCoverFather(Long rightCoverFather) {
        this.rightCoverFather = rightCoverFather;
    }

    public Long getFrontCoverFather() {
        return frontCoverFather;
    }

    public void setFrontCoverFather(Long frontCoverFather) {
        this.frontCoverFather = frontCoverFather;
    }

    public Long getBehindCoverFather() {
        return behindCoverFather;
    }

    public void setBehindCoverFather(Long behindCoverFather) {
        this.behindCoverFather = behindCoverFather;
    }
}
