from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any

from app.clients.ops_api import OpsApiClient
from app.models import AnalyzeRequest, AnalyzeResponse


DATETIME_FORMAT = "%Y-%m-%d %H:%M:%S"


@dataclass
class IntentPlan:
    need_trend: bool
    need_categories: bool
    need_ranking: bool
    need_order_status: bool
    need_store_status: bool
    ranking_sort: str = "desc"


class OpsAnalysisAgent:
    def __init__(self, ops_api_client: OpsApiClient) -> None:
        self.ops_api_client = ops_api_client

    async def analyze(self, request: AnalyzeRequest) -> AnalyzeResponse:
        plan = self._build_plan(request.query)
        start_time = self._format_datetime(request.timeRange.startTime)
        end_time = self._format_datetime(request.timeRange.endTime)
        compare_start_time = None
        compare_end_time = None
        if request.compareRange:
            compare_start_time = self._format_datetime(request.compareRange.startTime)
            compare_end_time = self._format_datetime(request.compareRange.endTime)

        tool_calls: list[str] = []
        evidence: dict[str, Any] = {}

        sales_overview = await self.ops_api_client.get_sales_overview(
            start_time,
            end_time,
            compare_start_time,
            compare_end_time,
        )
        tool_calls.append("sales-overview")
        evidence["salesOverview"] = sales_overview

        trend = None
        if plan.need_trend:
            trend = await self.ops_api_client.get_sales_trend(start_time, end_time, self._trend_granularity(request))
            tool_calls.append("sales-trend")
            evidence["salesTrend"] = trend

        categories = None
        if plan.need_categories:
            categories = await self.ops_api_client.get_category_sales_summary(start_time, end_time)
            tool_calls.append("category-sales-summary")
            evidence["categorySalesSummary"] = categories

        ranking = None
        if plan.need_ranking:
            ranking = await self.ops_api_client.get_dish_sales_ranking(
                start_time,
                end_time,
                order_by="revenue",
                sort=plan.ranking_sort,
                limit=10,
            )
            tool_calls.append("dish-sales-ranking")
            evidence["dishSalesRanking"] = ranking

        order_status = None
        if plan.need_order_status:
            order_status = await self.ops_api_client.get_order_status_summary(start_time, end_time)
            tool_calls.append("order-status-summary")
            evidence["orderStatusSummary"] = order_status

        store_status = None
        if plan.need_store_status:
            store_status = await self.ops_api_client.get_store_status(start_time, end_time)
            tool_calls.append("store-status")
            evidence["storeStatus"] = store_status

        summary = self._build_summary(request.query, sales_overview)
        analysis = self._build_analysis(sales_overview, trend, categories, ranking, order_status, store_status)
        suggestions = self._build_suggestions(sales_overview, trend, categories, ranking, order_status, store_status)

        return AnalyzeResponse(
            traceId=request.traceId,
            summary=summary,
            analysis=analysis,
            suggestions=suggestions,
            evidence=evidence,
            toolCalls=tool_calls,
        )

    def _build_plan(self, query: str) -> IntentPlan:
        normalized = query.lower()
        need_trend = any(keyword in query for keyword in ["时段", "趋势", "波动", "峰", "小时", "什么时候"])
        need_categories = any(keyword in query for keyword in ["分类", "品类", "套餐", "菜品结构"])
        need_ranking = any(keyword in query for keyword in ["菜", "商品", "热销", "滞销", "卖得", "拖累"])
        need_order_status = any(keyword in query for keyword in ["退款", "取消", "完成率", "异常订单", "订单状态"])
        need_store_status = any(keyword in query for keyword in ["营业状态", "打烊", "暂停接单", "关店"])

        if any(keyword in query for keyword in ["为什么", "原因", "下降", "降低", "少了"]):
            need_trend = True
            need_categories = True
            need_order_status = True
            need_ranking = True

        ranking_sort = "asc" if any(keyword in query for keyword in ["滞销", "最差", "拖累"]) else "desc"
        if "overview" in normalized:
            need_trend = True

        return IntentPlan(
            need_trend=need_trend,
            need_categories=need_categories,
            need_ranking=need_ranking,
            need_order_status=need_order_status,
            need_store_status=need_store_status,
            ranking_sort=ranking_sort,
        )

    def _trend_granularity(self, request: AnalyzeRequest) -> str:
        delta = request.timeRange.endTime - request.timeRange.startTime
        return "hour" if delta.days <= 2 else "day"

    def _build_summary(self, query: str, sales_overview: dict[str, Any]) -> str:
        revenue = sales_overview.get("revenue", 0.0)
        compare_revenue = sales_overview.get("compareRevenue")
        change_rate = sales_overview.get("revenueChangeRate")
        valid_order_count = sales_overview.get("validOrderCount", 0)

        if compare_revenue is not None and change_rate is not None:
            direction = "上升" if change_rate >= 0 else "下降"
            return (
                f"当前时间范围营业额为 {revenue:.2f} 元，较对比时段{direction} {abs(change_rate) * 100:.1f}%，"
                f"有效订单数为 {valid_order_count}。"
            )
        return f"当前时间范围营业额为 {revenue:.2f} 元，有效订单数为 {valid_order_count}。"

    def _build_analysis(
        self,
        sales_overview: dict[str, Any],
        trend: list[dict[str, Any]] | None,
        categories: list[dict[str, Any]] | None,
        ranking: list[dict[str, Any]] | None,
        order_status: dict[str, Any] | None,
        store_status: dict[str, Any] | None,
    ) -> list[str]:
        analysis: list[str] = []

        revenue = sales_overview.get("revenue", 0.0)
        avg_order_amount = sales_overview.get("avgOrderAmount", 0.0)
        analysis.append(f"本时段营业额 {revenue:.2f} 元，平均客单价 {avg_order_amount:.2f} 元。")

        change_rate = sales_overview.get("revenueChangeRate")
        if change_rate is not None:
            direction = "增长" if change_rate >= 0 else "下降"
            analysis.append(f"营业额较对比时段{direction} {abs(change_rate) * 100:.1f}%。")

        if trend:
            weakest_bucket = min(trend, key=lambda item: item.get("revenue", 0.0))
            strongest_bucket = max(trend, key=lambda item: item.get("revenue", 0.0))
            analysis.append(
                f"时段波动上，{strongest_bucket['bucket']} 表现最好，营业额 {strongest_bucket.get('revenue', 0.0):.2f} 元；"
                f"{weakest_bucket['bucket']} 表现最弱，营业额 {weakest_bucket.get('revenue', 0.0):.2f} 元。"
            )

        if categories:
            top_category = categories[0]
            analysis.append(
                f"分类贡献最高的是 {top_category.get('categoryName') or '未分类'}，"
                f"贡献营业额 {top_category.get('revenue', 0.0):.2f} 元，占比 "
                f"{top_category.get('revenueContributionRate', 0.0) * 100:.1f}%。"
            )

        if ranking:
            first_item = ranking[0]
            analysis.append(
                f"商品层面，{first_item.get('name')} 表现最突出，销量 {first_item.get('salesVolume', 0)}，"
                f"营业额贡献 {first_item.get('revenue', 0.0):.2f} 元。"
            )

        if order_status:
            cancellation_rate = order_status.get("cancellationRate", 0.0)
            refund_rate = order_status.get("refundRate", 0.0)
            analysis.append(
                f"订单状态方面，取消率 {cancellation_rate * 100:.1f}%，退款率 {refund_rate * 100:.1f}%。"
            )

        if store_status:
            analysis.append(f"当前门店状态为{store_status.get('shopStatusName', '未知')}。")

        return analysis

    def _build_suggestions(
        self,
        sales_overview: dict[str, Any],
        trend: list[dict[str, Any]] | None,
        categories: list[dict[str, Any]] | None,
        ranking: list[dict[str, Any]] | None,
        order_status: dict[str, Any] | None,
        store_status: dict[str, Any] | None,
    ) -> list[str]:
        suggestions: list[str] = []

        revenue_change_rate = sales_overview.get("revenueChangeRate")
        if revenue_change_rate is not None and revenue_change_rate < 0:
            suggestions.append("营业额低于对比时段，建议优先复盘低谷时段曝光、出餐和履约链路。")

        if trend:
            weakest_bucket = min(trend, key=lambda item: item.get("revenue", 0.0))
            suggestions.append(f"建议重点关注 {weakest_bucket['bucket']} 这一低表现时段，可尝试限时套餐或活动引流。")

        if categories:
            weak_categories = sorted(categories, key=lambda item: item.get("revenue", 0.0))[:2]
            weak_names = "、".join(item.get("categoryName") or "未分类" for item in weak_categories)
            suggestions.append(f"可优先复查 {weak_names} 的商品结构、库存和展示策略。")

        if ranking:
            weak_items = sorted(ranking, key=lambda item: item.get("revenue", 0.0))[:3]
            weak_names = "、".join(item.get("name") or "未知商品" for item in weak_items)
            suggestions.append(f"可针对 {weak_names} 做组合售卖、图片优化或临时下架评估。")

        if order_status and order_status.get("refundRate", 0.0) > 0.05:
            suggestions.append("退款率偏高，建议复查近期售后原因和对应商品质量问题。")

        if store_status and store_status.get("shopStatus") != 1:
            suggestions.append("当前门店非营业中，如需分析营业额异常，先确认营业状态是否符合预期。")

        if not suggestions:
            suggestions.append("整体经营较平稳，建议继续关注高峰时段表现和热销商品供给。")

        return suggestions[:3]

    def _format_datetime(self, value: datetime) -> str:
        return value.strftime(DATETIME_FORMAT)

