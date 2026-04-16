from __future__ import annotations

from typing import Any

import httpx

from app.config import settings
from app.models import OpsToolResponse


class OpsApiClient:
    def __init__(self) -> None:
        self._client = httpx.AsyncClient(
            base_url=settings.server_base_url,
            timeout=settings.agent_timeout_seconds,
        )

    async def close(self) -> None:
        await self._client.aclose()

    async def get_sales_overview(
        self,
        start_time: str,
        end_time: str,
        compare_start_time: str | None = None,
        compare_end_time: str | None = None,
    ) -> dict[str, Any]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
        }
        if compare_start_time and compare_end_time:
            params["compareStartTime"] = compare_start_time
            params["compareEndTime"] = compare_end_time
        return await self._get("/internal/ops/sales-overview", params)

    async def get_sales_trend(self, start_time: str, end_time: str, granularity: str) -> list[dict[str, Any]]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
            "granularity": granularity,
        }
        return await self._get("/internal/ops/sales-trend", params)

    async def get_dish_sales_ranking(
        self,
        start_time: str,
        end_time: str,
        order_by: str = "revenue",
        sort: str = "desc",
        limit: int = 10,
    ) -> list[dict[str, Any]]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
            "orderBy": order_by,
            "sort": sort,
            "limit": limit,
        }
        return await self._get("/internal/ops/dish-sales-ranking", params)

    async def get_category_sales_summary(self, start_time: str, end_time: str) -> list[dict[str, Any]]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
        }
        return await self._get("/internal/ops/category-sales-summary", params)

    async def get_order_status_summary(self, start_time: str, end_time: str) -> dict[str, Any]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
        }
        return await self._get("/internal/ops/order-status-summary", params)

    async def get_store_status(self, start_time: str, end_time: str) -> dict[str, Any]:
        params = {
            "startTime": start_time,
            "endTime": end_time,
        }
        return await self._get("/internal/ops/store-status", params)

    async def _get(self, path: str, params: dict[str, Any]) -> Any:
        response = await self._client.get(path, params=params)
        response.raise_for_status()
        payload = OpsToolResponse.model_validate(response.json())
        if payload.code != 1:
            raise RuntimeError(payload.msg or f"Downstream call failed: {path}")
        return payload.data

