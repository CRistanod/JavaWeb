from __future__ import annotations

from datetime import datetime
from typing import Any

from pydantic import BaseModel, Field


class TimeRange(BaseModel):
    startTime: datetime
    endTime: datetime


class AnalyzeRequest(BaseModel):
    traceId: str
    storeId: int | None = None
    userId: int | None = None
    query: str
    timeRange: TimeRange
    compareRange: TimeRange | None = None
    context: dict[str, Any] = Field(default_factory=dict)


class AnalyzeResponse(BaseModel):
    traceId: str
    summary: str
    analysis: list[str]
    suggestions: list[str]
    evidence: dict[str, Any]
    toolCalls: list[str]


class OpsToolResponse(BaseModel):
    code: int
    msg: str | None = None
    data: Any = None

