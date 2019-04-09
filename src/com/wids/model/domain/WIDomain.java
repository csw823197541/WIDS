package com.wids.model.domain;

/**
 * Created by csw on 2017/4/27 15:03.
 * Explain:
 */
public final class WIDomain {

    /**
     * 发送，队列中
     */
    public static final String A = "A";
    /**
     * 作业中
     */
    public static final String W = "W";
    /**
     * 完成
     */
    public static final String C = "C";
    /**
     * 完成
     */
    public static final String RC = "RC";
    /**
     * 未发送
     */
    public static final String Y = "Y";
    /**
     * 未发送，提交状态
     */
    public static final String S = "S";
    /**
     * 未发送
     */
    public static final String P = "P";
    /**
     * 退卸或退装
     */
    public static final String R = "R";

    public static final String SELF = "SELF";

    public static final String YES = "Y";

    public static final String NO = "N";

    /**
     * 作业块状态
     */
    /**
     * 计划
     */
    public static final String Pre = "1";
    /**
     * 提交作业
     */
    public static final String SubmitToWork = "3";
    /**
     * 发箱A
     */
    public static final String SendToWork = "4";
    /**
     * 作业W
     */
    public static final String Work = "5";
    /**
     * 完工
     */
    public static final String Finish = "9";
    /**
     * 取消作业
     */
    public static final String Cancel = "D";

    public static final String LEFT = "LEFT";

    public static final String RIGHT = "RIGHT";
}
