package com.sky.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 特卖建单消息体。
 * 抢购通过后，只把建单所需的最小信息发到 MQ，消费端再按这些参数查库建单。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleOrderMessage implements Serializable {

    /** 活动 ID，用于查活动配置与价格 */
    private Long activityId;

    /** 用户 ID，用于订单归属和用户校验 */
    private Long userId;

    /** 收货地址 ID，消费端会再次校验归属 */
    private Long addressBookId;

    /** 请求幂等号，防止重复消费重复建单 */
    private String requestId;

    /** 用户下单备注 */
    private String remark;
}
