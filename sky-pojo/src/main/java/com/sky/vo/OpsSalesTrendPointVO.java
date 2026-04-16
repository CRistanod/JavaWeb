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
@ApiModel(description = "Agent内部接口-营业趋势点")
public class OpsSalesTrendPointVO implements Serializable {

    @ApiModelProperty("趋势时间桶，按小时示例：2026-04-08 10:00，按天示例：2026-04-08")
    private String bucket;

    @ApiModelProperty("当前时间桶营业额，单位：元")
    private Double revenue;

    @ApiModelProperty("当前时间桶订单总数")
    private Integer orderCount;

    @ApiModelProperty("当前时间桶有效订单数，通常指已完成订单")
    private Integer validOrderCount;
}
