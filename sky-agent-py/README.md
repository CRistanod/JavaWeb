# sky-agent-py

Python agent service for merchant operations analysis.

## Run

```bash
uv sync
uv run uvicorn app.main:app --reload --port 8001
```

## Environment

- `SKY_SERVER_BASE_URL`: Java service base URL, default `http://127.0.0.1:8080`
- `SKY_AGENT_TIMEOUT_SECONDS`: HTTP timeout to Java service, default `10`

## Main API

`POST /agent/ops/analyze`

Example request:

```json
{
  "traceId": "demo-trace-001",
  "storeId": 1,
  "userId": 1,
  "query": "为什么今天营业额比昨天低？",
  "timeRange": {
    "startTime": "2026-04-08 00:00:00",
    "endTime": "2026-04-08 23:59:59"
  },
  "compareRange": {
    "startTime": "2026-04-07 00:00:00",
    "endTime": "2026-04-07 23:59:59"
  },
  "context": {
    "role": "merchant_admin",
    "timezone": "Asia/Shanghai"
  }
}
```

