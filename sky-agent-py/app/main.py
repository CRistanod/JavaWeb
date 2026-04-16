from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.clients.ops_api import OpsApiClient
from app.models import AnalyzeRequest, AnalyzeResponse
from app.services.ops_agent import OpsAnalysisAgent


ops_api_client = OpsApiClient()
ops_analysis_agent = OpsAnalysisAgent(ops_api_client)


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    await ops_api_client.close()


app = FastAPI(
    title="Sky Agent Service",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/agent/ops/analyze", response_model=AnalyzeResponse)
async def analyze_operations(request: AnalyzeRequest) -> AnalyzeResponse:
    return await ops_analysis_agent.analyze(request)

