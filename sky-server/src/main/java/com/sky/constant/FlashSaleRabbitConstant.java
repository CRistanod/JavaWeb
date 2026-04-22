package com.sky.constant;

/**
 * 限时特卖模块使用到的 RabbitMQ 常量。
 * 建单链路和超时检查链路都在这里统一定义。
 */
public final class FlashSaleRabbitConstant {

    private FlashSaleRabbitConstant() {
    }

    /** 创建订单交换机 */
    public static final String ORDER_EXCHANGE = "flashsale.order.exchange";
    /** 创建订单队列 */
    public static final String ORDER_QUEUE = "flashsale.order.queue";
    /** 创建订单路由键 */
    public static final String ORDER_ROUTING_KEY = "flashsale.order.create";

    /** 超时交换机 */
    public static final String TIMEOUT_EXCHANGE = "flashsale.timeout.exchange";
    /** 超时队列 */
    public static final String TIMEOUT_QUEUE = "flashsale.timeout.queue";
    /** 超时路由键 */
    public static final String TIMEOUT_ROUTING_KEY = "flashsale.order.timeout";

    /** 死信交换机 */
    public static final String DEAD_EXCHANGE = "flashsale.dead.exchange";
    /** 死信队列 */
    public static final String DEAD_QUEUE = "flashsale.dead.queue";
    /** 死信路由键 */
    public static final String DEAD_ROUTING_KEY = "flashsale.order.dead";
}
