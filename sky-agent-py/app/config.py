from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="SKY_", case_sensitive=False)

    server_base_url: str = "http://127.0.0.1:8080"
    agent_timeout_seconds: float = 10.0


settings = Settings()

