package com.vibe.common.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 * 规范金额计算，防止精度丢失
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class MoneyUtils {
    
    /**
     * 金额精度（小数点后2位）
     */
    private static final int MONEY_SCALE = 2;
    
    /**
     * 金额舍入模式（四舍五入）
     */
    private static final RoundingMode MONEY_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * 私有构造函数，防止实例化
     */
    private MoneyUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 创建金额（规范精度）
     * 
     * @param value 金额值
     * @return 规范后的金额
     */
    public static BigDecimal createMoney(String value) {
        return new BigDecimal(value).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 创建金额（规范精度）
     * 
     * @param value 金额值
     * @return 规范后的金额
     */
    public static BigDecimal createMoney(double value) {
        return BigDecimal.valueOf(value).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 创建金额（规范精度）
     * 
     * @param value 金额值
     * @return 规范后的金额
     */
    public static BigDecimal createMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
        }
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 金额相加
     * 
     * @param a 金额a
     * @param b 金额b
     * @return 相加后的金额（规范精度）
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        BigDecimal result = createMoney(a).add(createMoney(b));
        return result.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 金额相减
     * 
     * @param a 金额a
     * @param b 金额b
     * @return 相减后的金额（规范精度）
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        BigDecimal result = createMoney(a).subtract(createMoney(b));
        return result.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 金额相乘
     * 
     * @param a 金额a
     * @param b 金额b
     * @return 相乘后的金额（规范精度）
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        BigDecimal result = createMoney(a).multiply(createMoney(b));
        return result.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 金额相乘（金额 * 数量）
     * 
     * @param price 单价
     * @param quantity 数量
     * @return 总价（规范精度）
     */
    public static BigDecimal multiply(BigDecimal price, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
        }
        BigDecimal result = createMoney(price).multiply(new BigDecimal(quantity));
        return result.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 金额相除
     * 
     * @param a 金额a
     * @param b 金额b
     * @return 相除后的金额（规范精度）
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        BigDecimal result = createMoney(a).divide(createMoney(b), MONEY_SCALE, MONEY_ROUNDING_MODE);
        return result.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
    
    /**
     * 确保金额不为负数
     * 
     * @param amount 金额
     * @return 如果为负数则返回0，否则返回原值
     */
    public static BigDecimal ensureNonNegative(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
        }
        BigDecimal result = createMoney(amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
        }
        return result;
    }
}
