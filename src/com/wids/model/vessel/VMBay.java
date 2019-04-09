package com.wids.model.vessel;


import com.wids.utils.StringUtil;

/**
 * Created by csw on 2017/4/20 11:38.
 * Explain: 倍位作业信息(一般分三个倍位置作业)
 */
public class VMBay {

    private Long bayId; //倍Id
    private Integer bayNo; //倍位号
    private String aboveOrBelow; //甲板、舱内
    private Long hatchId;

    public VMBay(Long bayId, Integer bayNo, String aboveOrBelow, Long hatchId) {
        this.bayId = bayId;
        this.bayNo = bayNo;
        this.aboveOrBelow = aboveOrBelow;
        this.hatchId = hatchId;
    }

    public Long getBayId() {
        return bayId;
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public String getBayKey() {
        return StringUtil.getKey(bayNo, aboveOrBelow);
    }

    public String getAboveOrBelow() {
        return aboveOrBelow;
    }

    public Long getHatchId() {
        return hatchId;
    }

}
