package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Agent内部接口-订单状态汇总")
public class OpsOrderStatusSummaryVO implements Serializable {

    @ApiModelProperty("订单总数")
    private Integer totalOrderCount;

    @ApiModelProperty("待付款订单数")
    private Integer pendingPaymentOrderCount;

    @ApiModelProperty("待接单订单数")
    private Integer toBeConfirmedOrderCount;

    @ApiModelProperty("已接单订单数")
    private Integer confirmedOrderCount;

    @ApiModelProperty("派送中订单数")
    private Integer deliveryInProgressOrderCount;

    @ApiModelProperty("已完成订单数")
    private Integer completedOrderCount;

    @ApiModelProperty("已取消订单数")
    private Integer cancelledOrderCount;

    @ApiModelProperty("退款订单数")
    private Integer refundOrderCount;

    @ApiModelProperty("订单完成率，0.8 表示 80%")
    private Double completionRate;

    @ApiModelProperty("订单取消率，0.1 表示 10%")
    private Double cancellationRate;

    @ApiModelProperty("订单退款率，0.05 表示 5%")
    private Double refundRate;
}
