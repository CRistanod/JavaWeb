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
@ApiModel(description = "特卖抢购异步结果")
public class FlashSaleResultVO implements Serializable {

    @ApiModelProperty("请求ID")
    private String requestId;

    @ApiModelProperty("处理状态：0处理中 1成功 2失败")
    private Integer status;

    @ApiModelProperty("提示信息")
    private String message;

    @ApiModelProperty("活动ID")
    private Long activityId;

    @ApiModelProperty("特卖记录ID")
    private Long flashSaleOrderId;

    @ApiModelProperty("正式订单ID")
    private Long orderId;

    @ApiModelProperty("正式订单号")
    private String orderNumber;
}
