import os
from dotenv import load_dotenv

load_dotenv()

GROQ_API_KEY = os.getenv("GROQ_API_KEY")

GROQ_MODEL = os.getenv(
    "GROQ_MODEL",
    "qwen/qwen3.8-27b"
)

FLASK_HOST = os.getenv(
    "FLASK_HOST",
    "0.0.0.0"
)

FLASK_PORT = int(
    os.getenv(
        "FLASK_PORT",
        "5000"
    )
)

DATABASE_NAME = "road_inspections.db"

UPLOAD_FOLDER = "uploads"

REPORT_FOLDER = "reports"
