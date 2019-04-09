package com.wids.utils;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class StringUtil {

    public static String getKey(Long id, String str) {
        return id.toString() + "@" + str;
    }

    public static String getKey(String id, String str) {
        return id + "@" + str;
    }

    public static String getKey(Integer id, String str) {
        return id.toString() + "@" + str;
    }

    public static boolean notBlank(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    public static String getKey(Integer id1, Integer id2) {
        return id1.toString() + "@" + id2.toString();
    }
}
