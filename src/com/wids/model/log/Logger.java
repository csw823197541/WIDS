package com.wids.model.log;


import com.wids.model.domain.WIDefaultValue;
import com.wids.model.domain.WIDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2017/8/14.
 * Description:
 */
public class Logger {

    private static Integer LOG_LEVEL_DEBUG = 0;
    private static Integer LOG_LEVEL_INFO = 1;
    private static Integer LOG_LEVEL_WARN = 2;
    private static Integer LOG_LEVEL_ERROR = 3;

    private String[] logLevelInfo;

    private List<Object[]> logList = new ArrayList<>();

    public Logger() {
        logLevelInfo = new String[]{"[DEBUG]", "[INFO]", "[WARN]", "[ERROR]"};
    }

    private void logLog(Integer level, String log) {
        Object[] logM = {level, log};
        logList.add(logM);
        if (WIDefaultValue.outputLogToConsole) {
            System.out.println(logLevelInfo[level] + log);
        } else {
            if (!logLevelInfo[level].equals("[DEBUG]")) {
                System.out.println(logLevelInfo[level] + log);
            }
        }
    }

    public void logDebug(String log) {
        logLog(LOG_LEVEL_DEBUG, log);
    }

    public void logInfo(String log) {
        logLog(LOG_LEVEL_INFO, log);
    }

    public void logInfo(String info, boolean empty) {
        if (empty) {
            logInfo("算法输入数据中有信息为空(提示)：" + info);
        }
    }

    public void logWarn(String log) {
        logLog(LOG_LEVEL_WARN, log);
    }

    public void logError(String log) {
        logLog(LOG_LEVEL_ERROR, log);
    }

    public void logError(String info, boolean empty) {
        if (empty) {
            logError("算法输入数据必要信息为空(必要)：" + info);
        }
    }

    private String getLog(Integer level) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Object[] log : logList) {
            Integer curLevel = (Integer) log[0];
            if (curLevel >= level) {
                stringBuffer.append(logLevelInfo[curLevel]).append(log[1]);
                stringBuffer.append('\n');
            }
        }
        return new String(stringBuffer);
    }

    public String getDebug() {
        return getLog(LOG_LEVEL_DEBUG);
    }

    public String getInfo() {
        return getLog(LOG_LEVEL_INFO);
    }

    public String getWarn() {
        return getLog(LOG_LEVEL_WARN);
    }

    public String getError() {
        return getLog(LOG_LEVEL_ERROR);
    }

}
