package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特卖抢购记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleOrder implements Serializable {

    public static final Integer CREATED = 1;
    public static final Integer PAID = 2;
    public static final Integer CANCELLED = 3;
    public static final Integer FAILED = 4;

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long activityId;

    private Long userId;

    private Long orderId;

    private String requestId;

    private Integer status;

    private String failReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
