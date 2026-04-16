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
@ApiModel(description = "Agent内部接口-门店营业状态")
public class OpsStoreStatusVO implements Serializable {

    @ApiModelProperty("分析开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("分析结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("门店状态：1 营业中，0 打烊中")
    private Integer shopStatus;

    @ApiModelProperty("门店状态名称")
    private String shopStatusName;
}
