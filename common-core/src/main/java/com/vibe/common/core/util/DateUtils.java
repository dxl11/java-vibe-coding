package com.vibe.common.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 * 提供日期格式化、解析等常用方法
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class DateUtils {
    
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    
    /**
     * 默认日期时间格式化器
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    
    /**
     * 默认日期格式化器
     */
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = 
            DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    
    /**
     * 默认时间格式化器
     */
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
    
    /**
     * 私有构造函数，防止实例化
     */
    private DateUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 格式化 LocalDateTime 为字符串（默认格式）
     * 
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 格式化 LocalDateTime 为字符串（自定义格式）
     * 
     * @param dateTime 日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
    
    /**
     * 解析字符串为 LocalDateTime（默认格式）
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 解析字符串为 LocalDateTime（自定义格式）
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
    
    /**
     * 获取当前时间
     * 
     * @return 当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
