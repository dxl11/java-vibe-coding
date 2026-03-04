package com.vibe.common.core.constant;

/**
 * 通用常量类
 * 定义系统中使用的通用常量
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class CommonConstants {
    
    /**
     * 成功响应码
     */
    public static final Integer SUCCESS_CODE = 200;
    
    /**
     * 失败响应码
     */
    public static final Integer FAIL_CODE = 500;
    
    /**
     * 参数错误响应码
     */
    public static final Integer PARAM_ERROR_CODE = 400;
    
    /**
     * 未授权响应码
     */
    public static final Integer UNAUTHORIZED_CODE = 401;
    
    /**
     * 禁止访问响应码
     */
    public static final Integer FORBIDDEN_CODE = 403;
    
    /**
     * 资源不存在响应码
     */
    public static final Integer NOT_FOUND_CODE = 404;
    
    /**
     * 逻辑删除标记：未删除
     */
    public static final Integer NOT_DELETED = 0;
    
    /**
     * 逻辑删除标记：已删除
     */
    public static final Integer DELETED = 1;
    
    /**
     * 私有构造函数，防止实例化
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
