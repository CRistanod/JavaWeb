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
@ApiModel(description = "特卖抢购受理结果")
public class FlashSalePurchaseVO implements Serializable {

    public static final Integer ACCEPTED = 0;
    public static final Integer SUCCESS = 1;
    public static final Integer FAILED = 2;

    @ApiModelProperty("请求ID")
    private String requestId;

    @ApiModelProperty("受理状态：0处理中 1成功 2失败")
    private Integer status;

    @ApiModelProperty("提示信息")
    private String message;
}
