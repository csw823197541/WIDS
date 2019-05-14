package com.wids.model.domain;

/**
 * Created by csw on 2017/4/27 14:25.
 * Explain:
 */
public class WIDefaultValue {

    public static String wiVersion = "WI2.0.19.5.14";
    public static boolean outputLogToConsole = false;

    public static Long intervalTime = 30L; //默认发送30分钟的指令
    public static Double craneSafeSpan = 14.0; //桥机安全距离

    public static Long moveTime = 119000L; //换倍时间2分钟，ms

    public static Boolean crossBridge = Boolean.FALSE;
    public static Boolean crossChimney = Boolean.FALSE;
    public static Long crossBarTime = 10 * 60L;

    public static long exceedTime = 10 * 60;
    public static Long deckWeightDifference = 800L;
    public static Long hatchWeightDifference = 1000L;
    public static Long hatchSideWeightDifference = 1000L;

    public static String deckRowExchange = "N";
    public static String deckBayExchange = "N";
    public static String hatchRowExchange = "N";
    public static String hatchBayExchange = "N";
    public static String deckAndHatchExchange = "N";
    public static String allVesselExchange = "N";
    public static String emptyCntExchange = "N";
    public static String moveOrderExchange = "Y";
    public static Integer deckTierNum = 4;
    public static Integer hatchTierNum = 10;

    public static Integer sendContainerNum = 2;
    public static Long sendIntervalTime = 8L;

    public static long sentSeq = 100;
}
