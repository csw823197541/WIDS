package com.wids.manager.method;

import com.shbtos.biz.smart.cwp.pojo.Results.SmartReMessageInfo;
import com.shbtos.biz.smart.cwp.service.*;
import com.wids.manager.data.AllData;

/**
 * Created by csw on 2017/10/11.
 * Description:
 */
public class WIDSMethod {

    private WIDSMethod() {}

    public static void parseData(AllData allData, SmartWiImportData smartWiImportData, SmartWiResults smartWiResults) {
        ParseDataMethod parseDataMethod = new ParseDataMethod(allData);

        parseDataMethod.parseSchedule(smartWiImportData.getSmartScheduleIdInfoList(), smartWiResults.getSmartReMessageInfo());
        parseDataMethod.insertSmartWiResults(smartWiResults);

        parseDataMethod.parseConfiguration(smartWiImportData.getSmartWiConfigurationList());

        parseDataMethod.parseCrane(smartWiImportData.getSmartCraneWorkStartTmInfoList(), smartWiImportData.getSmartCraneBaseInfoList());

        parseDataMethod.parseCraneMaintainPlan(smartWiImportData.getSmartCraneMaintainPlanInfoList());

        parseDataMethod.parseWorkBlock(smartWiImportData.getSmartCwpWorkBlockInfoList());

        parseDataMethod.parseVesselContainer(smartWiImportData.getSmartVesselContainerInfoList());

        allData.getLogger().logInfo("解析船舶结构信息");
        parseVesselData(allData, smartWiImportData);

    }

    private static void parseVesselData(AllData allData, SmartWiImportData smartWiImportData) {
        ParseDataMethod parseDataMethod = new ParseDataMethod(allData);

        parseDataMethod.parseHatchInfo(smartWiImportData.getSmartVpsVslHatchsInfoList());

        parseDataMethod.parseBayInfo(smartWiImportData.getSmartVpsVslBaysInfoList());

        parseDataMethod.parseRowInfo(smartWiImportData.getSmartVpsVslRowsInfoList());

        parseDataMethod.parseLocationInfo(smartWiImportData.getSmartVpsVslLocationsInfoList());

        parseDataMethod.parseVesselMachine(smartWiImportData.getSmartVesselMachinesInfoList());

        parseDataMethod.parseHatchCover(smartWiImportData.getSmartVpsVslHatchcoversInfoList());
    }


    public static void parseAdjustWorkBlockData(AllData allData, SmartWiImportData smartWiImportData, SmartWiResults smartWiResults) {
        ParseDataMethod parseDataMethod = new ParseDataMethod(allData);

        parseDataMethod.parseSchedule(smartWiImportData.getSmartScheduleIdInfoList(), smartWiResults.getSmartReMessageInfo());
        parseDataMethod.insertSmartWiResults(smartWiResults);

        parseDataMethod.parseConfiguration(smartWiImportData.getSmartWiConfigurationList());

        parseDataMethod.parseCrane(smartWiImportData.getSmartCraneWorkStartTmInfoList(), smartWiImportData.getSmartCraneBaseInfoList());

        parseDataMethod.parseCraneMaintainPlan(smartWiImportData.getSmartCraneMaintainPlanInfoList());

        parseDataMethod.parseWorkBlock(smartWiImportData.getSmartCwpWorkBlockInfoList());

        parseDataMethod.parseHatchInfo(smartWiImportData.getSmartVpsVslHatchsInfoList());

        parseDataMethod.parseBayInfo(smartWiImportData.getSmartVpsVslBaysInfoList());

        parseDataMethod.parseVesselMachine(smartWiImportData.getSmartVesselMachinesInfoList());

        parseDataMethod.parseVesselContainer(smartWiImportData.getSmartVesselContainerInfoList());
    }

    public static void parseCwpValidatorData(AllData allData, SmartCwpValidatorImportData smartCwpValidatorImportData, SmartCwpValidatorResults smartCwpValidatorResults) {
        ParseDataMethod parseDataMethod = new ParseDataMethod(allData);

        parseDataMethod.parseSchedule(smartCwpValidatorImportData.getSmartScheduleIdInfoList(), smartCwpValidatorResults.getSmartReMessageInfo());
        parseDataMethod.insertSmartCwpValidatorResults(smartCwpValidatorResults);

        parseDataMethod.parseConfiguration(smartCwpValidatorImportData.getSmartWiConfigurationList());

        parseDataMethod.parseCrane(smartCwpValidatorImportData.getSmartCraneWorkStartTmInfoList(), smartCwpValidatorImportData.getSmartCraneBaseInfoList());

        parseDataMethod.parseCraneMaintainPlan(smartCwpValidatorImportData.getSmartCraneMaintainPlanInfoList());

        parseDataMethod.parseWorkBlock(smartCwpValidatorImportData.getSmartCwpWorkBlockInfoList());

        parseDataMethod.parseHatchInfo(smartCwpValidatorImportData.getSmartVpsVslHatchsInfoList());

        parseDataMethod.parseBayInfo(smartCwpValidatorImportData.getSmartVpsVslBaysInfoList());

        parseDataMethod.parseVesselMachine(smartCwpValidatorImportData.getSmartVesselMachinesInfoList());

        parseDataMethod.parseVesselContainer(smartCwpValidatorImportData.getSmartVesselContainerInfoList());
    }

    public static void saveLogger(AllData allData, SmartReMessageInfo smartReMessageInfo) {
        GenerateResultMethod.saveLogger(allData, smartReMessageInfo);
    }

    public static void saveValidatorLogger(AllData allData, SmartReMessageInfo smartReMessageInfo) {
        GenerateResultMethod.saveValidatorLogger(allData, smartReMessageInfo);
    }
}
