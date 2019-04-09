package com.wids.model.wi;

import java.util.Date;

/**
 * Created by csw on 2017/11/7.
 * Description:
 */
public class WICraneMaintainPlan {

    private String craneNo;//桥吊ID
    private Date MaintainStartTime	;	//维护开始时间
    private Date MaintainEndTime;    //维护结束时间
    private String craneStatus;//桥吊作业状态故障
    private String craneMoveStatus; //是否可以移动

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public Date getMaintainStartTime() {
        return MaintainStartTime;
    }

    public void setMaintainStartTime(Date maintainStartTime) {
        MaintainStartTime = maintainStartTime;
    }

    public Date getMaintainEndTime() {
        return MaintainEndTime;
    }

    public void setMaintainEndTime(Date maintainEndTime) {
        MaintainEndTime = maintainEndTime;
    }

    public String getCraneStatus() {
        return craneStatus;
    }

    public void setCraneStatus(String craneStatus) {
        this.craneStatus = craneStatus;
    }

    public String getCraneMoveStatus() {
        return craneMoveStatus;
    }

    public void setCraneMoveStatus(String craneMoveStatus) {
        this.craneMoveStatus = craneMoveStatus;
    }
}
