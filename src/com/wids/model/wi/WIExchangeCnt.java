package com.wids.model.wi;

/**
 * Created by csw on 2018/1/30.
 * Description:
 */
public class WIExchangeCnt {

    private Long berthId; //靠泊Id
    private Long vpcCntId; //船图箱Id
    private String vLocation; //船箱位信息
    private Long yardContainerId; //交换后新的在场箱Id
    private String originalYLocation;
    private String exchangeYLocation;
    private Integer originalOverCntNum;
    private Integer exchangeOverCntNum;

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getVpcCntId() {
        return vpcCntId;
    }

    public void setVpcCntId(Long vpcCntId) {
        this.vpcCntId = vpcCntId;
    }

    public String getvLocation() {
        return vLocation;
    }

    public void setvLocation(String vLocation) {
        this.vLocation = vLocation;
    }

    public Long getYardContainerId() {
        return yardContainerId;
    }

    public void setYardContainerId(Long yardContainerId) {
        this.yardContainerId = yardContainerId;
    }

    public Integer getOriginalOverCntNum() {
        return originalOverCntNum;
    }

    public void setOriginalOverCntNum(Integer originalOverCntNum) {
        this.originalOverCntNum = originalOverCntNum;
    }

    public Integer getExchangeOverCntNum() {
        return exchangeOverCntNum;
    }

    public void setExchangeOverCntNum(Integer exchangeOverCntNum) {
        this.exchangeOverCntNum = exchangeOverCntNum;
    }

    public String getOriginalYLocation() {
        return originalYLocation;
    }

    public void setOriginalYLocation(String originalYLocation) {
        this.originalYLocation = originalYLocation;
    }

    public String getExchangeYLocation() {
        return exchangeYLocation;
    }

    public void setExchangeYLocation(String exchangeYLocation) {
        this.exchangeYLocation = exchangeYLocation;
    }
}
