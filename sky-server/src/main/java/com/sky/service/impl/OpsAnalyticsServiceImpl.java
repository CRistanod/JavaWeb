package com.sky.service.impl;

import com.sky.controller.admin.ShopController;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OpsAnalyticsService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OpsCategorySalesItemVO;
import com.sky.vo.OpsDishSalesItemVO;
import com.sky.vo.OpsOrderStatusSummaryVO;
import com.sky.vo.OpsSalesOverviewVO;
import com.sky.vo.OpsSalesTrendPointVO;
import com.sky.vo.OpsStoreStatusVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OpsAnalyticsServiceImpl implements OpsAnalyticsService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    private final OrderMapper orderMapper;
    private final WorkspaceService workspaceService;
    private final RedisTemplate redisTemplate;

    public OpsAnalyticsServiceImpl(OrderMapper orderMapper, WorkspaceService workspaceService, RedisTemplate redisTemplate) {
        this.orderMapper = orderMapper;
        this.workspaceService = workspaceService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OpsSalesOverviewVO getSalesOverview(LocalDateTime startTime, LocalDateTime endTime,
                                               LocalDateTime compareStartTime, LocalDateTime compareEndTime) {
        BusinessDataVO current = workspaceService.getBusinessData(startTime, endTime);
        Integer totalOrderCount = countOrders(startTime, endTime, null, null);
        Integer cancelledOrderCount = countOrders(startTime, endTime, Orders.CANCELLED, null);
        Integer refundOrderCount = countOrders(startTime, endTime, null, Orders.REFUND);

        Double compareRevenue = null;
        Integer compareValidOrderCount = null;
        Double compareAvgOrderAmount = null;
        Double revenueChangeRate = null;
        Double validOrderCountChangeRate = null;
        Double avgOrderAmountChangeRate = null;
        if (compareStartTime != null && compareEndTime != null) {
            BusinessDataVO compare = workspaceService.getBusinessData(compareStartTime, compareEndTime);
            compareRevenue = compare.getTurnover();
            compareValidOrderCount = compare.getValidOrderCount();
            compareAvgOrderAmount = compare.getUnitPrice();
            revenueChangeRate = calculateChangeRate(current.getTurnover(), compareRevenue);
            validOrderCountChangeRate = calculateChangeRate(current.getValidOrderCount().doubleValue(), compareValidOrderCount.doubleValue());
            avgOrderAmountChangeRate = calculateChangeRate(current.getUnitPrice(), compareAvgOrderAmount);
        }

        return OpsSalesOverviewVO.builder()
                .startTime(startTime)
                .endTime(endTime)
                .revenue(current.getTurnover())
                .totalOrderCount(totalOrderCount)
                .validOrderCount(current.getValidOrderCount())
                .cancelledOrderCount(cancelledOrderCount)
                .refundOrderCount(refundOrderCount)
                .orderCompletionRate(current.getOrderCompletionRate())
                .avgOrderAmount(current.getUnitPrice())
                .compareRevenue(compareRevenue)
                .revenueChangeRate(revenueChangeRate)
                .compareValidOrderCount(compareValidOrderCount)
                .validOrderCountChangeRate(validOrderCountChangeRate)
                .compareAvgOrderAmount(compareAvgOrderAmount)
                .avgOrderAmountChangeRate(avgOrderAmountChangeRate)
                .build();
    }

    @Override
    public List<OpsSalesTrendPointVO> getSalesTrend(LocalDateTime startTime, LocalDateTime endTime, String granularity) {
        String normalizedGranularity = granularity == null ? "day" : granularity.toLowerCase(Locale.ROOT);
        List<OpsSalesTrendPointVO> trend = new ArrayList<>();
        if ("hour".equals(normalizedGranularity)) {
            LocalDateTime cursor = startTime.withMinute(0).withSecond(0).withNano(0);
            while (!cursor.isAfter(endTime)) {
                LocalDateTime bucketEnd = cursor.plusHours(1);
                trend.add(buildTrendPoint(cursor, bucketEnd, HOUR_FORMATTER));
                cursor = bucketEnd;
            }
            return trend;
        }

        LocalDate cursorDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        while (!cursorDate.isAfter(endDate)) {
            LocalDateTime bucketStart = LocalDateTime.of(cursorDate, LocalTime.MIN);
            LocalDateTime bucketEnd = LocalDateTime.of(cursorDate, LocalTime.MAX);
            if (bucketStart.isBefore(startTime)) {
                bucketStart = startTime;
            }
            if (bucketEnd.isAfter(endTime)) {
                bucketEnd = endTime;
            }
            trend.add(buildTrendPoint(bucketStart, bucketEnd, DAY_FORMATTER));
            cursorDate = cursorDate.plusDays(1);
        }
        return trend;
    }

    @Override
    public List<OpsDishSalesItemVO> getDishSalesRanking(LocalDateTime startTime, LocalDateTime endTime,
                                                        String orderBy, String sort, Integer limit) {
        String normalizedOrderBy = "salesVolume".equalsIgnoreCase(orderBy) ? "salesVolume" : "revenue";
        String normalizedSort = "asc".equalsIgnoreCase(sort) ? "asc" : "desc";
        int normalizedLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 100);
        String orderByClause = "salesVolume".equals(normalizedOrderBy) ? "salesVolume" : "revenue";
        return orderMapper.getDishSalesRanking(startTime, endTime, orderByClause, normalizedSort, normalizedLimit);
    }

    @Override
    public List<OpsCategorySalesItemVO> getCategorySalesSummary(LocalDateTime startTime, LocalDateTime endTime) {
        List<OpsCategorySalesItemVO> items = orderMapper.getCategorySalesSummary(startTime, endTime);
        double totalRevenue = items.stream().map(OpsCategorySalesItemVO::getRevenue).filter(v -> v != null).mapToDouble(Double::doubleValue).sum();
        for (OpsCategorySalesItemVO item : items) {
            double revenue = item.getRevenue() == null ? 0.0 : item.getRevenue();
            item.setRevenueContributionRate(totalRevenue == 0 ? 0.0 : revenue / totalRevenue);
        }
        return items;
    }

    @Override
    public OpsOrderStatusSummaryVO getOrderStatusSummary(LocalDateTime startTime, LocalDateTime endTime) {
        Integer totalOrderCount = countOrders(startTime, endTime, null, null);
        Integer pendingPaymentOrderCount = countOrders(startTime, endTime, Orders.PENDING_PAYMENT, null);
        Integer toBeConfirmedOrderCount = countOrders(startTime, endTime, Orders.TO_BE_CONFIRMED, null);
        Integer confirmedOrderCount = countOrders(startTime, endTime, Orders.CONFIRMED, null);
        Integer deliveryInProgressOrderCount = countOrders(startTime, endTime, Orders.DELIVERY_IN_PROGRESS, null);
        Integer completedOrderCount = countOrders(startTime, endTime, Orders.COMPLETED, null);
        Integer cancelledOrderCount = countOrders(startTime, endTime, Orders.CANCELLED, null);
        Integer refundOrderCount = countOrders(startTime, endTime, null, Orders.REFUND);

        return OpsOrderStatusSummaryVO.builder()
                .totalOrderCount(totalOrderCount)
                .pendingPaymentOrderCount(pendingPaymentOrderCount)
                .toBeConfirmedOrderCount(toBeConfirmedOrderCount)
                .confirmedOrderCount(confirmedOrderCount)
                .deliveryInProgressOrderCount(deliveryInProgressOrderCount)
                .completedOrderCount(completedOrderCount)
                .cancelledOrderCount(cancelledOrderCount)
                .refundOrderCount(refundOrderCount)
                .completionRate(calculateRate(completedOrderCount, totalOrderCount))
                .cancellationRate(calculateRate(cancelledOrderCount, totalOrderCount))
                .refundRate(calculateRate(refundOrderCount, totalOrderCount))
                .build();
    }

    @Override
    public OpsStoreStatusVO getStoreStatus(LocalDateTime startTime, LocalDateTime endTime) {
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(ShopController.KEY);
        if (shopStatus == null) {
            shopStatus = 0;
        }
        return OpsStoreStatusVO.builder()
                .startTime(startTime)
                .endTime(endTime)
                .shopStatus(shopStatus)
                .shopStatusName(shopStatus == 1 ? "营业中" : "打烊中")
                .build();
    }

    private OpsSalesTrendPointVO buildTrendPoint(LocalDateTime bucketStart, LocalDateTime bucketEnd, DateTimeFormatter formatter) {
        Map<String, Object> queryMap = buildOrderQueryMap(bucketStart, bucketEnd, Orders.COMPLETED, null);
        Double revenue = orderMapper.sumByMap(queryMap);
        Integer validOrderCount = orderMapper.countByMap(queryMap);
        Integer orderCount = countOrders(bucketStart, bucketEnd, null, null);
        return OpsSalesTrendPointVO.builder()
                .bucket(bucketStart.format(formatter))
                .revenue(revenue == null ? 0.0 : revenue)
                .orderCount(orderCount)
                .validOrderCount(validOrderCount)
                .build();
    }

    private Integer countOrders(LocalDateTime startTime, LocalDateTime endTime, Integer status, Integer payStatus) {
        return orderMapper.countByMap(buildOrderQueryMap(startTime, endTime, status, payStatus));
    }

    private Map<String, Object> buildOrderQueryMap(LocalDateTime startTime, LocalDateTime endTime, Integer status, Integer payStatus) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("begin", startTime);
        queryMap.put("end", endTime);
        queryMap.put("status", status);
        queryMap.put("payStatus", payStatus);
        return queryMap;
    }

    private Double calculateRate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0 || numerator == null) {
            return 0.0;
        }
        return numerator.doubleValue() / denominator;
    }

    private Double calculateChangeRate(Double currentValue, Double compareValue) {
        if (compareValue == null || compareValue == 0) {
            return currentValue == null || currentValue == 0 ? 0.0 : 1.0;
        }
        return (currentValue - compareValue) / compareValue;
    }
}
