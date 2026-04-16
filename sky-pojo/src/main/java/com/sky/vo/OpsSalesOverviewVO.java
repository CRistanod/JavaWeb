package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Agent内部接口-营业概览")
public class OpsSalesOverviewVO implements Serializable {

    @ApiModelProperty("统计开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("统计结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("营业额，单位：元")
    private Double revenue;

    @ApiModelProperty("订单总数")
    private Integer totalOrderCount;

    @ApiModelProperty("有效订单数，通常指已完成订单")
    private Integer validOrderCount;

    @ApiModelProperty("已取消订单数")
    private Integer cancelledOrderCount;

    @ApiModelProperty("退款订单数")
    private Integer refundOrderCount;

    @ApiModelProperty("订单完成率，0.8 表示 80%")
    private Double orderCompletionRate;

    @ApiModelProperty("平均客单价，单位：元")
    private Double avgOrderAmount;

    @ApiModelProperty("对比时段营业额，未传对比时间时为空")
    private Double compareRevenue;

    @ApiModelProperty("营业额变化率，-0.1 表示下降 10%，未传对比时间时为空")
    private Double revenueChangeRate;

    @ApiModelProperty("对比时段有效订单数，未传对比时间时为空")
    private Integer compareValidOrderCount;

    @ApiModelProperty("有效订单数变化率，未传对比时间时为空")
    private Double validOrderCountChangeRate;

    @ApiModelProperty("对比时段平均客单价，单位：元，未传对比时间时为空")
    private Double compareAvgOrderAmount;

    @ApiModelProperty("平均客单价变化率，未传对比时间时为空")
    private Double avgOrderAmountChangeRate;
}
