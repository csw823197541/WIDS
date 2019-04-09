package com.wids.manager.data;


import com.wids.model.log.Logger;
import com.wids.model.wi.WICrane;
import com.wids.model.wi.WICraneMaintainPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2017/10/12.
 * Description:
 */
public class AllData {

    private Logger logger;

    private Map<Long, WIData> wiDataMap;
    private Map<String, VesselData> vesselDataMap;

    private Map<String, WICrane> wiCraneMap; //桥机作业计划信息
    private Map<String, WICraneMaintainPlan> wiCraneMaintainPlanMap; //桥机维修计划

    public AllData() {
        logger = new Logger();
        wiDataMap = new HashMap<>();
        vesselDataMap = new HashMap<>();
        wiCraneMap = new HashMap<>();
        wiCraneMaintainPlanMap = new HashMap<>();
    }

    public Logger getLogger() {
        return logger;
    }

    public void addWIData(WIData wiData) {
        wiDataMap.put(wiData.getVmSchedule().getBerthId(), wiData);
    }

    public List<WIData> getAllWIData() {
        return new ArrayList<>(wiDataMap.values());
    }

    public WIData getWIDataByBerthId(Long berthId) {
        return wiDataMap.get(berthId);
    }

    public void addVesselData(VesselData vesselData) {
        vesselDataMap.put(vesselData.getVesselCode(), vesselData);
    }

    public VesselData getVesselDataByVesselCode(String vesselCode) {
        return vesselDataMap.get(vesselCode);
    }

    public void addWICrane(WICrane wiCrane) {
        wiCraneMap.put(wiCrane.getCraneNo(), wiCrane);
    }

    public WICrane getWICraneByCraneNo(String craneNo) {
        return wiCraneMap.get(craneNo);
    }

    public void addWICraneMaintainPlan(WICraneMaintainPlan wiCraneMaintainPlan) {
        wiCraneMaintainPlanMap.put(wiCraneMaintainPlan.getCraneNo(), wiCraneMaintainPlan);
    }

    public List<WICraneMaintainPlan> getAllWICraneMaintainPlanList() {
        return new ArrayList<>(wiCraneMaintainPlanMap.values());
    }

    public WICraneMaintainPlan getWICraneMaintainPlanByCraneNo(String craneNo) {
        return null;
    }

}
