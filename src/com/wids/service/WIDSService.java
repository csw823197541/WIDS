package com.wids.service;

import com.wids.manager.data.AllData;
import com.wids.manager.data.VesselData;
import com.wids.manager.data.WIData;
import com.wids.manager.method.GenerateResultMethod;
import com.wids.model.log.Logger;
import com.wids.service.process.WIDSProcess;

/**
 * Created by csw on 2017/10/11.
 * Description:
 */
public class WIDSService {

    private WIDSService() {
    }

    public static void processWIDS(AllData allData) {
        for (WIData wiData : allData.getAllWIData()) {
            Long berthId = wiData.getVmSchedule().getBerthId();
            VesselData vesselData = allData.getVesselDataByVesselCode(wiData.getVmSchedule().getVesselCode());
            Logger logger = wiData.getLogger();
            logger.logInfo("调用发箱服务算法，对船舶(berthId: " + berthId + ")进行发送指令。");
            try {
                WIDSProcess widsProcess = new WIDSProcess(wiData, vesselData, allData);
                widsProcess.processWIDS();
            } catch (Exception e) {
                logger.logError("对船舶(berthId: " + berthId + ")进行发送指令时发生异常！");
                e.printStackTrace();
            }
            logger.logInfo("船舶(berthId: " + berthId + ")指令发送结束。");
        }
    }

    //test
    public static void adjustWorkBlock(AllData allData) {
        for (WIData wiData : allData.getAllWIData()) {
            Long berthId = wiData.getVmSchedule().getBerthId();
            VesselData vesselData = allData.getVesselDataByVesselCode(wiData.getVmSchedule().getVesselCode());
            Logger logger = wiData.getLogger();
            logger.logInfo("对船舶(berthId: " + berthId + ")的作业块块进行调整。");
            try {
                WIDSProcess widsProcess = new WIDSProcess(wiData, vesselData, allData);
                widsProcess.adjustWorkBlock();
            } catch (Exception e) {
                logger.logError("对船舶(berthId: " + berthId + ")进行作业块调整时发生异常！");
                e.printStackTrace();
            }
            logger.logInfo("船舶(berthId: " + berthId + ")的作业块调整结束。");
        }
    }

    //test
    public static void validateCwp(AllData allData) {
        for (WIData wiData : allData.getAllWIData()) {
            Long berthId = wiData.getVmSchedule().getBerthId();
            VesselData vesselData = allData.getVesselDataByVesselCode(wiData.getVmSchedule().getVesselCode());
            Logger logger = wiData.getLogger();
            logger.logInfo("对船舶(berthId: " + berthId + ")进行验证CWP是否需要重排调整。");
            try {
                WIDSProcess widsProcess = new WIDSProcess(wiData, vesselData, allData);
                widsProcess.validateCwp();
            } catch (Exception e) {
                logger.logError("对船舶(berthId: " + berthId + ")进行验证CWP是否需要重排调整时发生异常！");
                e.printStackTrace();
            }
            logger.logInfo("船舶(berthId: " + berthId + ")进行验证CWP是否需要重排调整结束。");
        }
    }
}
