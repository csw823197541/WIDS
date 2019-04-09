package com.wids.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by csw on 2017/4/5 11:02.
 * Explain: 验证传入算法的数据是否合理
 */
public class ValidatorUtil {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNull(Object object) {
        boolean isNull = object == null;
        if (!isNull && object instanceof String) {
            isNull = "".equals(((String) object).trim());
        }
        return isNull;
    }

}
