package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 特卖活动
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleActivity implements Serializable {

    public static final Integer DISABLED = 0;
    public static final Integer ENABLED = 1;

    private static final long serialVersionUID = 1L;

    private Long id;

    private String activityName;

    private Long dishId;

    private BigDecimal salePrice;

    private Integer stock;

    private Integer availableStock;

    private Integer limitPerUser;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
