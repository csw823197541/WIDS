package com.wids.model.wi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2017/4/13 15:09.
 * Explain: 每部桥机的开始作业时间
 */
public class WICrane {

    private String craneNo; //桥机编号
    private String workStatus; //桥机作业状态  1：计划、4：作业/开工、5：暂停、9：完工
    private Date workStartTime; //桥机计划作业开始时间
    private Integer craneSeq; //桥机位置的顺序号，用于给桥机排先后顺序

    private Date actualWorkST;
    private Long sentWiWorkTime; //已发送状态指令的作业耗时，毫秒ms
    private Long remainWiWorkTime;

    private List<WIWorkBlock> wiWorkBlockList; //桥机作业块信息
    private Long wiWorkTime;

    private Boolean needReDoCwp;
    private String reDoCwpReason;

    private long curExchangeTime;

    public WICrane(String craneNo, String workStatus) {
        this.craneNo = craneNo;
        this.workStatus = workStatus;
        sentWiWorkTime = 0L;
        wiWorkTime = 0L;
        wiWorkBlockList = new ArrayList<>();
        needReDoCwp = Boolean.FALSE;
    }

    public boolean craneCanWork() {//计划和开工状态的指令都可以发
        return "4".equals(workStatus) || "1".equals(workStatus);
    }

    public String getCraneNo() {
        return craneNo;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public Date getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(Date workStartTime) {
        this.workStartTime = workStartTime;
    }

    public Integer getCraneSeq() {
        return craneSeq;
    }

    public void setCraneSeq(Integer craneSeq) {
        this.craneSeq = craneSeq;
    }

    public Date getActualWorkST() {
        return actualWorkST;
    }

    public void setActualWorkST(Date actualWorkST) {
        this.actualWorkST = actualWorkST;
    }

    public Long getSentWiWorkTime() {
        return sentWiWorkTime;
    }

    public void setSentWiWorkTime(Long sentWiWorkTime) {
        this.sentWiWorkTime = sentWiWorkTime;
    }

    public Long getRemainWiWorkTime() {
        return remainWiWorkTime;
    }

    public void setRemainWiWorkTime(Long remainWiWorkTime) {
        this.remainWiWorkTime = remainWiWorkTime;
    }

    public List<WIWorkBlock> getWiWorkBlockList() {
        return wiWorkBlockList;
    }

    public void setWiWorkBlockList(List<WIWorkBlock> wiWorkBlockList) {
        this.wiWorkBlockList = wiWorkBlockList;
    }

    public long getWiWorkTime() {
        return wiWorkTime;
    }

    public void setWiWorkTime(long wiWorkTime) {
        this.wiWorkTime = wiWorkTime;
    }

    public Boolean getNeedReDoCwp() {
        return needReDoCwp;
    }

    public void setNeedReDoCwp(Boolean needReDoCwp) {
        this.needReDoCwp = needReDoCwp;
    }

    public String getReDoCwpReason() {
        return reDoCwpReason;
    }

    public void setReDoCwpReason(String reDoCwpReason) {
        this.reDoCwpReason = reDoCwpReason;
    }

    public long getCurExchangeTime() {
        return curExchangeTime;
    }

    public void setCurExchangeTime(long curExchangeTime) {
        this.curExchangeTime = curExchangeTime;
    }
}
