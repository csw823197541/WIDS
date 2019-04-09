package com.wids.model.wi;

/**
 * Created by csw on 2018/5/10.
 * Description:
 */
public class WIExchangeValue {

    private Integer code;
    private String desc;

    public WIExchangeValue(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
