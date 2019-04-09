package com.wids.model.vessel;


/**
 * Created by csw on 2017/8/14.
 * Description: 一个船箱位对应一个slot
 */
public class VMSlot {

    private VMPosition vmPosition;
    private String aboveOrBelow;
    private String size; //全隔槽信息
    private Long bayId;

    public VMSlot(VMPosition vmPosition, String aboveOrBelow, String size, Long bayId) {
        this.vmPosition = vmPosition;
        this.aboveOrBelow = aboveOrBelow;
        this.size = size;
        this.bayId = bayId;
    }

    public String getVLocation() {
        return vmPosition.getVLocation();
    }

    public VMPosition getVmPosition() {
        return vmPosition;
    }

    public String getAboveOrBelow() {
        return aboveOrBelow;
    }

    public String getSize() {
        return size;
    }

    public Long getBayId() {
        return bayId;
    }

}
