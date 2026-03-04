package com.vibe.common.core.util;

import java.util.Collection;

/**
 * 字符串工具类
 * 提供字符串处理的常用方法
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class StringUtils {
    
    /**
     * 私有构造函数，防止实例化
     */
    private StringUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 判断字符串是否为空
     * 
     * @param str 字符串
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     * 
     * @param str 字符串
     * @return true-不为空，false-为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否为空白（包括空格、制表符等）
     * 
     * @param str 字符串
     * @return true-为空白，false-不为空白
     */
    public static boolean isBlank(String str) {
        if (isEmpty(str)) {
            return true;
        }
        return str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空白
     * 
     * @param str 字符串
     * @return true-不为空白，false-为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 判断集合是否为空
     * 
     * @param collection 集合
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 判断集合是否不为空
     * 
     * @param collection 集合
     * @return true-不为空，false-为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}
