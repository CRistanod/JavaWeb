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
@ApiModel(description = "Agent内部接口-商品销售排行项")
public class OpsDishSalesItemVO implements Serializable {

    @ApiModelProperty("商品类型：dish 菜品，setmeal 套餐")
    private String goodsType;

    @ApiModelProperty("商品ID")
    private Long itemId;

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("所属分类名称")
    private String categoryName;

    @ApiModelProperty("销量")
    private Integer salesVolume;

    @ApiModelProperty("营业额，单位：元")
    private Double revenue;
}
