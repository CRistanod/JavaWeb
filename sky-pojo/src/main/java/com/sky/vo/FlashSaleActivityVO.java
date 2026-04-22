package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "特卖活动展示对象")
public class FlashSaleActivityVO implements Serializable {

    @ApiModelProperty("活动ID")
    private Long id;

    @ApiModelProperty("活动名称")
    private String activityName;

    @ApiModelProperty("关联菜品ID")
    private Long dishId;

    @ApiModelProperty("菜品名称")
    private String dishName;

    @ApiModelProperty("菜品图片")
    private String dishImage;

    @ApiModelProperty("菜品原价，单位：元")
    private BigDecimal originalPrice;

    @ApiModelProperty("特卖价，单位：元")
    private BigDecimal salePrice;

    @ApiModelProperty("活动总库存")
    private Integer stock;

    @ApiModelProperty("剩余库存")
    private Integer availableStock;

    @ApiModelProperty("单用户限购次数")
    private Integer limitPerUser;

    @ApiModelProperty("活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty("活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("活动状态：0禁用 1启用")
    private Integer status;
}
