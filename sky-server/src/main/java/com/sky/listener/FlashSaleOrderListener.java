package com.sky.listener;

import com.sky.constant.FlashSaleRabbitConstant;
import com.sky.message.FlashSaleOrderMessage;
import com.sky.service.FlashSaleOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 特卖建单消息监听器。
 * 抢购资格通过后，只要异步模式开启，就由这里消费消息并进入正式建单流程。
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "sky.flash-sale.async-enabled", havingValue = "true")
public class FlashSaleOrderListener {

    @Autowired
    private FlashSaleOrderService flashSaleOrderService;

    /**
     * 消费“创建特卖订单”消息。
     * 当前方法本身只做转发，幂等、建单、异常回滚都下沉到 service。
     */
    @RabbitListener(queues = FlashSaleRabbitConstant.ORDER_QUEUE)
    public void consumeCreateOrder(FlashSaleOrderMessage message) {
        log.info("收到特卖下单消息：{}", message);
        flashSaleOrderService.createFlashSaleOrder(message);
    }
}
