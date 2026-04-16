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
@ApiModel(description = "Agent内部接口-分类销售汇总项")
public class OpsCategorySalesItemVO implements Serializable {

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("销量")
    private Integer salesVolume;

    @ApiModelProperty("营业额，单位：元")
    private Double revenue;

    @ApiModelProperty("营业额贡献占比，0.25 表示 25%")
    private Double revenueContributionRate;
}
