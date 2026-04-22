package com.sky.listener;

import com.sky.constant.FlashSaleRabbitConstant;
import com.sky.message.FlashSaleTimeoutMessage;
import com.sky.service.FlashSaleOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 特卖超时检查消息监听器。
 * 负责消费死信队列中的消息，判断订单是否超时未支付。
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "sky.flash-sale.async-enabled", havingValue = "true")
public class FlashSaleTimeoutListener {

    @Autowired
    private FlashSaleOrderService flashSaleOrderService;

    /**
     * 消费超时检查消息。
     * service 中会再次校验订单状态，而不是收到消息就直接取消。
     */
    @RabbitListener(queues = FlashSaleRabbitConstant.DEAD_QUEUE)
    public void consumeTimeout(FlashSaleTimeoutMessage message) {
        log.info("收到特卖超时检查消息：{}", message);
        flashSaleOrderService.handleTimeout(message.getFlashSaleOrderId());
    }
}
