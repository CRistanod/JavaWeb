package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "管理端特卖活动新增/编辑参数")
public class FlashSaleActivityDTO implements Serializable {

    @ApiModelProperty("活动ID，新增时不传")
    private Long id;

    @ApiModelProperty(value = "活动名称", required = true)
    private String activityName;

    @ApiModelProperty(value = "关联菜品ID", required = true)
    private Long dishId;

    @ApiModelProperty(value = "特卖价，单位：元", required = true)
    private BigDecimal salePrice;

    @ApiModelProperty(value = "活动总库存", required = true)
    private Integer stock;

    @ApiModelProperty(value = "单用户限购次数", required = true, example = "1")
    private Integer limitPerUser;

    @ApiModelProperty(value = "活动开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "活动结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("活动状态：0禁用 1启用，新增时可不传，默认禁用")
    private Integer status;
}
