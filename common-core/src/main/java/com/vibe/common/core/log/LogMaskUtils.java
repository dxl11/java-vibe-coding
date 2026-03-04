package com.vibe.common.core.log;

import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 * 用于敏感信息的脱敏处理
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class LogMaskUtils {
    
    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    
    /**
     * 身份证号正则
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    
    /**
     * 银行卡号正则
     */
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");
    
    /**
     * 邮箱正则
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9_]{1,3})[a-zA-Z0-9_]*@([a-zA-Z0-9]+\\.[a-zA-Z]+)");
    
    /**
     * 私有构造函数，防止实例化
     */
    private LogMaskUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 脱敏手机号
     * 示例：13812345678 -> 138****5678
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return maskCommon(phone, 3, 4);
    }
    
    /**
     * 脱敏身份证号
     * 示例：110101199001011234 -> 110101********1234
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + "******" + idCard.substring(12);
        }
        return maskCommon(idCard, 6, 4);
    }
    
    /**
     * 脱敏银行卡号
     * 示例：6222021234567890123 -> 6222****1234
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return bankCard;
        }
        if (bankCard.length() >= 8) {
            return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
        }
        return "****";
    }
    
    /**
     * 脱敏密码
     * 
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "******";
    }
    
    /**
     * 脱敏邮箱
     * 示例：username@example.com -> u***@example.com
     * 
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            String prefix = email.substring(0, Math.min(1, atIndex));
            String suffix = email.substring(atIndex);
            return prefix + "***" + suffix;
        }
        return maskCommon(email, 1, 0);
    }
    
    /**
     * 脱敏姓名
     * 示例：张三 -> 张*
     * 
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**" + name.charAt(name.length() - 1);
    }
    
    /**
     * 通用脱敏方法
     * 
     * @param value 原始值
     * @param prefixLength 前缀保留长度
     * @param suffixLength 后缀保留长度
     * @return 脱敏后的值
     */
    public static String maskCommon(String value, int prefixLength, int suffixLength) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        if (length <= prefixLength + suffixLength) {
            return "****";
        }
        String prefix = value.substring(0, prefixLength);
        String suffix = suffixLength > 0 ? value.substring(length - suffixLength) : "";
        int maskLength = length - prefixLength - suffixLength;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }
        return prefix + mask + suffix;
    }
    
    /**
     * 脱敏对象（递归处理）
     * 
     * @param obj 对象
     * @return 脱敏后的字符串
     */
    public static String maskObject(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        // 尝试各种脱敏规则
        if (str.matches("\\d{11}")) {
            return maskPhone(str);
        }
        if (str.matches("\\d{15}|\\d{18}")) {
            return maskIdCard(str);
        }
        if (str.matches("\\d{16,19}")) {
            return maskBankCard(str);
        }
        if (str.contains("@")) {
            return maskEmail(str);
        }
        return str;
    }
}
