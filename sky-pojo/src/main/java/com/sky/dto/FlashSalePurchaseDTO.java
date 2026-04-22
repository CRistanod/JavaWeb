package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户发起特卖抢购参数")
public class FlashSalePurchaseDTO implements Serializable {

    @ApiModelProperty(value = "活动ID", required = true)
    private Long activityId;

    @ApiModelProperty(value = "地址簿ID", required = true)
    private Long addressBookId;

    @ApiModelProperty(value = "请求幂等ID，客户端每次点击生成唯一值", required = true)
    private String requestId;

    @ApiModelProperty("备注")
    private String remark;
}
