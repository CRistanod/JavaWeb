package com.sky.service;

import com.sky.message.FlashSaleOrderMessage;

public interface FlashSaleOrderService {

    void createFlashSaleOrder(FlashSaleOrderMessage message);

    void handleTimeout(Long flashSaleOrderId);

    void markPaidByOrderId(Long orderId);

    void handleOrderCancellation(Long orderId, String reason);
}
