package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "管理端特卖活动分页查询参数")
public class FlashSaleActivityPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;

    @ApiModelProperty("活动名称")
    private String activityName;

    @ApiModelProperty("菜品名称")
    private String dishName;

    @ApiModelProperty("活动状态：0禁用 1启用")
    private Integer status;
}
