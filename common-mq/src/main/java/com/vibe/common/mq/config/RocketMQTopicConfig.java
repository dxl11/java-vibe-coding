package com.vibe.common.mq.config;

/**
 * RocketMQ Topic 和 Tag 配置类
 * 定义业务 Topic 和 Tag 常量
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class RocketMQTopicConfig {
    
    // ==================== Topic 定义 ====================
    
    /**
     * 订单 Topic
     */
    public static final String TOPIC_ORDER = "order-topic";
    
    /**
     * 库存 Topic
     */
    public static final String TOPIC_INVENTORY = "inventory-topic";
    
    /**
     * 支付 Topic
     */
    public static final String TOPIC_PAYMENT = "payment-topic";
    
    /**
     * 用户 Topic
     */
    public static final String TOPIC_USER = "user-topic";
    
    /**
     * 商品 Topic
     */
    public static final String TOPIC_PRODUCT = "product-topic";
    
    /**
     * 优惠券 Topic
     */
    public static final String TOPIC_COUPON = "coupon-topic";
    
    // ==================== Tag 定义 ====================
    
    /**
     * 订单创建 Tag
     */
    public static final String TAG_ORDER_CREATE = "order-create";
    
    /**
     * 订单支付 Tag
     */
    public static final String TAG_ORDER_PAY = "order-pay";
    
    /**
     * 订单取消 Tag
     */
    public static final String TAG_ORDER_CANCEL = "order-cancel";
    
    /**
     * 订单完成 Tag
     */
    public static final String TAG_ORDER_COMPLETE = "order-complete";
    
    /**
     * 库存扣减 Tag
     */
    public static final String TAG_INVENTORY_DEDUCT = "inventory-deduct";
    
    /**
     * 库存回滚 Tag
     */
    public static final String TAG_INVENTORY_ROLLBACK = "inventory-rollback";
    
    /**
     * 库存补货 Tag
     */
    public static final String TAG_INVENTORY_REPLENISH = "inventory-replenish";
    
    /**
     * 支付成功 Tag
     */
    public static final String TAG_PAYMENT_SUCCESS = "payment-success";
    
    /**
     * 支付失败 Tag
     */
    public static final String TAG_PAYMENT_FAIL = "payment-fail";
    
    /**
     * 用户注册 Tag
     */
    public static final String TAG_USER_REGISTER = "user-register";
    
    /**
     * 用户登录 Tag
     */
    public static final String TAG_USER_LOGIN = "user-login";
    
    /**
     * 商品上架 Tag
     */
    public static final String TAG_PRODUCT_ONLINE = "product-online";
    
    /**
     * 商品下架 Tag
     */
    public static final String TAG_PRODUCT_OFFLINE = "product-offline";
    
    /**
     * 优惠券发放 Tag
     */
    public static final String TAG_COUPON_ISSUE = "coupon-issue";
    
    /**
     * 优惠券使用 Tag
     */
    public static final String TAG_COUPON_USE = "coupon-use";
    
    // ==================== 消费者组定义 ====================
    
    /**
     * 订单消费者组
     */
    public static final String CONSUMER_GROUP_ORDER = "order-consumer-group";
    
    /**
     * 库存消费者组
     */
    public static final String CONSUMER_GROUP_INVENTORY = "inventory-consumer-group";
    
    /**
     * 支付消费者组
     */
    public static final String CONSUMER_GROUP_PAYMENT = "payment-consumer-group";
    
    /**
     * 用户消费者组
     */
    public static final String CONSUMER_GROUP_USER = "user-consumer-group";
    
    /**
     * 商品消费者组
     */
    public static final String CONSUMER_GROUP_PRODUCT = "product-consumer-group";
    
    /**
     * 优惠券消费者组
     */
    public static final String CONSUMER_GROUP_COUPON = "coupon-consumer-group";
    
    /**
     * 私有构造函数，防止实例化
     */
    private RocketMQTopicConfig() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
