package com.sky.controller.internal;

import com.sky.result.Result;
import com.sky.service.OpsAnalyticsService;
import com.sky.vo.OpsCategorySalesItemVO;
import com.sky.vo.OpsDishSalesItemVO;
import com.sky.vo.OpsOrderStatusSummaryVO;
import com.sky.vo.OpsSalesOverviewVO;
import com.sky.vo.OpsSalesTrendPointVO;
import com.sky.vo.OpsStoreStatusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/internal/ops")
@Api(tags = "Agent内部运营分析接口")
@Slf4j
public class OpsAnalyticsController {

    private final OpsAnalyticsService opsAnalyticsService;

    public OpsAnalyticsController(OpsAnalyticsService opsAnalyticsService) {
        this.opsAnalyticsService = opsAnalyticsService;
    }

    @GetMapping("/sales-overview")
    @ApiOperation(
            value = "营业概览",
            notes = "供 agent 获取指定时间范围内的营业额、订单数、客单价、完成率以及可选对比时段指标。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "统计开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "统计结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59"),
            @ApiImplicitParam(name = "compareStartTime", value = "对比开始时间，可选；与 compareEndTime 同时传入才会返回对比指标", dataTypeClass = String.class, paramType = "query", example = "2026-04-07 00:00:00"),
            @ApiImplicitParam(name = "compareEndTime", value = "对比结束时间，可选；与 compareStartTime 同时传入才会返回对比指标", dataTypeClass = String.class, paramType = "query", example = "2026-04-07 23:59:59")
    })
    public Result<OpsSalesOverviewVO> getSalesOverview(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime compareStartTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime compareEndTime) {
        log.info("内部运营分析-营业概览：startTime={}, endTime={}, compareStartTime={}, compareEndTime={}",
                startTime, endTime, compareStartTime, compareEndTime);
        return Result.success(opsAnalyticsService.getSalesOverview(startTime, endTime, compareStartTime, compareEndTime));
    }

    @GetMapping("/sales-trend")
    @ApiOperation(
            value = "营业趋势",
            notes = "供 agent 按小时或按天获取营业趋势。granularity 可传 hour 或 day，默认 day。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "统计开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "统计结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59"),
            @ApiImplicitParam(name = "granularity", value = "统计粒度：hour 按小时，day 按天", dataTypeClass = String.class, paramType = "query", allowableValues = "hour,day", example = "hour")
    })
    public Result<List<OpsSalesTrendPointVO>> getSalesTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "day") String granularity) {
        log.info("内部运营分析-营业趋势：startTime={}, endTime={}, granularity={}", startTime, endTime, granularity);
        return Result.success(opsAnalyticsService.getSalesTrend(startTime, endTime, granularity));
    }

    @GetMapping("/dish-sales-ranking")
    @ApiOperation(
            value = "商品销售排行",
            notes = "供 agent 获取菜品和套餐的销售排行。orderBy 可传 revenue 或 salesVolume；sort 可传 desc 或 asc；limit 最多返回 100 条。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "统计开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "统计结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59"),
            @ApiImplicitParam(name = "orderBy", value = "排序字段：revenue 按营业额，salesVolume 按销量", dataTypeClass = String.class, paramType = "query", allowableValues = "revenue,salesVolume", example = "revenue"),
            @ApiImplicitParam(name = "sort", value = "排序方式：desc 降序，asc 升序", dataTypeClass = String.class, paramType = "query", allowableValues = "desc,asc", example = "desc"),
            @ApiImplicitParam(name = "limit", value = "返回条数，最小 1，最大 100，默认 10", dataTypeClass = Integer.class, paramType = "query", example = "10")
    })
    public Result<List<OpsDishSalesItemVO>> getDishSalesRanking(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "revenue") String orderBy,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("内部运营分析-商品销量排行：startTime={}, endTime={}, orderBy={}, sort={}, limit={}",
                startTime, endTime, orderBy, sort, limit);
        return Result.success(opsAnalyticsService.getDishSalesRanking(startTime, endTime, orderBy, sort, limit));
    }

    @GetMapping("/category-sales-summary")
    @ApiOperation(
            value = "分类销售汇总",
            notes = "供 agent 获取各分类的销量、营业额和营业额贡献占比。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "统计开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "统计结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59")
    })
    public Result<List<OpsCategorySalesItemVO>> getCategorySalesSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("内部运营分析-分类销售汇总：startTime={}, endTime={}", startTime, endTime);
        return Result.success(opsAnalyticsService.getCategorySalesSummary(startTime, endTime));
    }

    @GetMapping("/order-status-summary")
    @ApiOperation(
            value = "订单状态汇总",
            notes = "供 agent 获取指定时间范围内各订单状态数量、完成率、取消率和退款率。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "统计开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "统计结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59")
    })
    public Result<OpsOrderStatusSummaryVO> getOrderStatusSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("内部运营分析-订单状态汇总：startTime={}, endTime={}", startTime, endTime);
        return Result.success(opsAnalyticsService.getOrderStatusSummary(startTime, endTime));
    }

    @GetMapping("/store-status")
    @ApiOperation(
            value = "门店营业状态",
            notes = "供 agent 获取当前门店营业状态，并回显分析时间范围。shopStatus：1 营业中，0 打烊中。时间格式：yyyy-MM-dd HH:mm:ss"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "分析开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 00:00:00"),
            @ApiImplicitParam(name = "endTime", value = "分析结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, dataTypeClass = String.class, paramType = "query", example = "2026-04-08 23:59:59")
    })
    public Result<OpsStoreStatusVO> getStoreStatus(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("内部运营分析-门店状态：startTime={}, endTime={}", startTime, endTime);
        return Result.success(opsAnalyticsService.getStoreStatus(startTime, endTime));
    }
}
