package com.wids.model.vessel;


/**
 * Created by csw on 2017/4/20 11:38.
 * Explain: 舱信息
 */
public class VMHatch {

    private Long hatchId;
    private Double hatchPosition;
    private Double hatchLength;

    public VMHatch(Long hatchId) {
        this.hatchId = hatchId;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public Double getHatchPosition() {
        return hatchPosition;
    }

    public void setHatchPosition(Double hatchPosition) {
        this.hatchPosition = hatchPosition;
    }

    public Double getHatchLength() {
        return hatchLength;
    }

    public void setHatchLength(Double hatchLength) {
        this.hatchLength = hatchLength;
    }
}
