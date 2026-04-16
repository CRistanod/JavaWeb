package com.sky.service;

import com.sky.vo.OpsCategorySalesItemVO;
import com.sky.vo.OpsDishSalesItemVO;
import com.sky.vo.OpsOrderStatusSummaryVO;
import com.sky.vo.OpsSalesOverviewVO;
import com.sky.vo.OpsSalesTrendPointVO;
import com.sky.vo.OpsStoreStatusVO;

import java.time.LocalDateTime;
import java.util.List;

public interface OpsAnalyticsService {

    OpsSalesOverviewVO getSalesOverview(LocalDateTime startTime, LocalDateTime endTime,
                                        LocalDateTime compareStartTime, LocalDateTime compareEndTime);

    List<OpsSalesTrendPointVO> getSalesTrend(LocalDateTime startTime, LocalDateTime endTime, String granularity);

    List<OpsDishSalesItemVO> getDishSalesRanking(LocalDateTime startTime, LocalDateTime endTime,
                                                 String orderBy, String sort, Integer limit);

    List<OpsCategorySalesItemVO> getCategorySalesSummary(LocalDateTime startTime, LocalDateTime endTime);

    OpsOrderStatusSummaryVO getOrderStatusSummary(LocalDateTime startTime, LocalDateTime endTime);

    OpsStoreStatusVO getStoreStatus(LocalDateTime startTime, LocalDateTime endTime);
}
