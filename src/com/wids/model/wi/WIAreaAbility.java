package com.wids.model.wi;

import com.wids.service.method.PublicMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2018/1/19.
 * Description:
 */
public class WIAreaAbility {

    private String areaNo;
    private Integer areaEfficiency; //箱区能力
    private Integer allTaskNum; //总共指令数目
    private Integer curHourTaskNum; //当前一个小时需要发送的指令数目
    private Integer curHour20TaskNum; //当前一个小时需要发送的20尺箱子数目
    private Integer curMeanTaskNum; //当前一个小时所有箱区平均出箱能力
    private Integer curHourSentNum; //当前一个小时发送的指令数目
    private Integer curHourExchangeNum; //当前一个小时交换了的指令数目

    private List<WIContainer> wiContainerList;
    private Long curWorkTime;
    private List<WIContainer> sentCntList; //该箱区队列中、正在作业的指令
    private List<WIContainer> unSendCntList; //该箱区剩余的指令

    private Integer sentWiNumTemp;

    public WIAreaAbility(String areaNo) {
        this.areaNo = areaNo;
        areaEfficiency = 16;
        allTaskNum = 0;
        curHourTaskNum = 0;
        curHour20TaskNum = 0;
        curMeanTaskNum = 0;
        curHourSentNum = 0;
        curHourExchangeNum = 0;
        wiContainerList = new ArrayList<>();
        sentCntList = new ArrayList<>();
        unSendCntList = new ArrayList<>();
    }

    public int getSentWiNumBy15Mis(long curAreaWorkTime, Long sendIntervalTime) {
        int num = 0;
        long st;
        PublicMethod.sortWIContainerByWorkingStartTime(sentCntList);
        for (WIContainer wiContainer : sentCntList) {
            st = wiContainer.getWorkingStartTime().getTime();
            if (curAreaWorkTime - st > sendIntervalTime) {
                break;
            } else {
                num++;
            }
        }
        sentWiNumTemp = num;
        return num;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public int getAreaEfficiency() {
        return areaEfficiency;
    }

    public void setAreaEfficiency(int areaEfficiency) {
        this.areaEfficiency = areaEfficiency;
    }

    public Integer getAllTaskNum() {
        return allTaskNum;
    }

    public void setAllTaskNum(int allTaskNum) {
        this.allTaskNum = allTaskNum;
    }

    public int getCurHourTaskNum() {
        return curHourTaskNum;
    }

    public void setCurHourTaskNum(int curHourTaskNum) {
        this.curHourTaskNum = curHourTaskNum;
    }

    public Integer getCurHour20TaskNum() {
        return curHour20TaskNum;
    }

    public void setCurHour20TaskNum(Integer curHour20TaskNum) {
        this.curHour20TaskNum = curHour20TaskNum;
    }

    public int getCurMeanTaskNum() {
        return curMeanTaskNum;
    }

    public void setCurMeanTaskNum(int curMeanTaskNum) {
        this.curMeanTaskNum = curMeanTaskNum;
    }

    public Integer getCurHourSentNum() {
        return curHourSentNum;
    }

    public void setCurHourSentNum(int curHourSentNum) {
        this.curHourSentNum = curHourSentNum;
    }

    public int getCurHourExchangeNum() {
        return curHourExchangeNum;
    }

    public void setCurHourExchangeNum(int curHourExchangeNum) {
        this.curHourExchangeNum = curHourExchangeNum;
    }

    public List<WIContainer> getWiContainerList() {
        return wiContainerList;
    }

    public void setWiContainerList(List<WIContainer> wiContainerList) {
        this.wiContainerList = wiContainerList;
    }

    public List<WIContainer> getSentCntList() {
        return sentCntList;
    }

    public void setSentCntList(List<WIContainer> sentCntList) {
        this.sentCntList = sentCntList;
    }

    public List<WIContainer> getUnSendCntList() {
        return unSendCntList;
    }

    public void setUnSendCntList(List<WIContainer> unSendCntList) {
        this.unSendCntList = unSendCntList;
    }

    public Long getCurWorkTime() {
        return curWorkTime;
    }

    public void setCurWorkTime(Long curWorkTime) {
        this.curWorkTime = curWorkTime;
    }

    public Integer getSentWiNumTemp() {
        return sentWiNumTemp;
    }

    public void setSentWiNumTemp(Integer sentWiNumTemp) {
        this.sentWiNumTemp = sentWiNumTemp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WIAreaAbility that = (WIAreaAbility) o;

        return areaNo.equals(that.areaNo);
    }

    @Override
    public int hashCode() {
        return areaNo.hashCode();
    }
}
