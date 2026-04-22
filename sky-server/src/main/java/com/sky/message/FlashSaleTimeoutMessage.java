package com.sky.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 特卖订单超时检查消息体。
 * 这里只传 flashSaleOrderId，后续由服务层继续查正式订单和支付状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleTimeoutMessage implements Serializable {

    /** 需要做超时检查的特卖记录 ID */
    private Long flashSaleOrderId;
}
