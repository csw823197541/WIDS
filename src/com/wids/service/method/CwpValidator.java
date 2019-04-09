package com.wids.service.method;

import com.wids.manager.data.WIData;
import com.wids.model.domain.WIDefaultValue;
import com.wids.model.wi.WICrane;
import com.wids.model.wi.WIWorkBlock;

import java.util.List;

/**
 * Created by csw on 2017/11/15.
 * Description:
 */
public class CwpValidator {

    public static void validateKeyRoadAndSchedule(WIData wiData) {
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        for (WICrane wiCrane : wiCraneList) {
            List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
            if (wiWorkBlockList.size() > 0) {
                PublicMethod.sortWIWorkBlockByCraneSeq(wiWorkBlockList);
                WIWorkBlock wiWorkBlock = wiWorkBlockList.get(wiWorkBlockList.size() - 1);
                long planEndTime = wiData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000;
                long estimateEndTime = wiWorkBlock.getEstimateEndTime().getTime() / 1000;
                long exceedTime = estimateEndTime - planEndTime;
                if (exceedTime > WIDefaultValue.exceedTime) {
                    wiCrane.setNeedReDoCwp(Boolean.TRUE);
                    wiCrane.setReDoCwpReason("桥机(craneNo: " + wiCrane.getCraneNo() + ")完成倍位(bayNo: " + wiWorkBlock.getBayNo() + ")的作业，会超过船期(" + exceedTime / 60 + ")分钟！\n");
                }
            }
        }
    }
}
