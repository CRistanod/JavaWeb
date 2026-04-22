package com.sky.config;

import com.sky.constant.FlashSaleRabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 限时特卖模块的 RabbitMQ 拓扑配置。
 * 仅在 async-enabled=true 时启用，避免本地未启动 MQ 时影响服务启动。
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(value = "sky.flash-sale.async-enabled", havingValue = "true")
public class RabbitMQConfiguration {

    /**
     * 抢购成功后发送“创建特卖订单”消息的交换机。
     */
    @Bean
    public DirectExchange flashSaleOrderExchange() {
        return new DirectExchange(FlashSaleRabbitConstant.ORDER_EXCHANGE, true, false);
    }

    /**
     * 创建特卖订单队列。
     */
    @Bean
    public Queue flashSaleOrderQueue() {
        return QueueBuilder.durable(FlashSaleRabbitConstant.ORDER_QUEUE).build();
    }

    /**
     * 绑定创建订单交换机和队列。
     */
    @Bean
    public Binding flashSaleOrderBinding() {
        return BindingBuilder.bind(flashSaleOrderQueue())
                .to(flashSaleOrderExchange())
                .with(FlashSaleRabbitConstant.ORDER_ROUTING_KEY);
    }

    /**
     * 超时消息交换机。
     * 订单创建成功后，会发送一个带 TTL 的消息到这里。
     */
    @Bean
    public DirectExchange flashSaleTimeoutExchange() {
        return new DirectExchange(FlashSaleRabbitConstant.TIMEOUT_EXCHANGE, true, false);
    }

    /**
     * 死信交换机。
     * TTL 到期的消息会从超时队列转入这里。
     */
    @Bean
    public DirectExchange flashSaleDeadExchange() {
        return new DirectExchange(FlashSaleRabbitConstant.DEAD_EXCHANGE, true, false);
    }

    /**
     * 超时队列。
     * 自身不直接消费，主要用来承载延迟效果。
     */
    @Bean
    public Queue flashSaleTimeoutQueue() {
        return QueueBuilder.durable(FlashSaleRabbitConstant.TIMEOUT_QUEUE)
                .deadLetterExchange(FlashSaleRabbitConstant.DEAD_EXCHANGE)
                .deadLetterRoutingKey(FlashSaleRabbitConstant.DEAD_ROUTING_KEY)
                .build();
    }

    /**
     * 死信队列。
     * 最终由消费者执行“订单是否超时未支付”的检查。
     */
    @Bean
    public Queue flashSaleDeadQueue() {
        return QueueBuilder.durable(FlashSaleRabbitConstant.DEAD_QUEUE).build();
    }

    /**
     * 绑定超时交换机和超时队列。
     */
    @Bean
    public Binding flashSaleTimeoutBinding() {
        return BindingBuilder.bind(flashSaleTimeoutQueue())
                .to(flashSaleTimeoutExchange())
                .with(FlashSaleRabbitConstant.TIMEOUT_ROUTING_KEY);
    }

    /**
     * 绑定死信交换机和死信队列。
     */
    @Bean
    public Binding flashSaleDeadBinding() {
        return BindingBuilder.bind(flashSaleDeadQueue())
                .to(flashSaleDeadExchange())
                .with(FlashSaleRabbitConstant.DEAD_ROUTING_KEY);
    }
}
