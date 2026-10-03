import json
import base64
from groq import Groq
from config import (
    GROQ_API_KEY,
    GROQ_MODEL
)

if not GROQ_API_KEY:
    raise RuntimeError(
        "GROQ_API_KEY is not configured."
    )

client = Groq(
    api_key=GROQ_API_KEY
)

SYSTEM_PROMPT = """
You are an AI road inspection assistant.
Analyze the supplied road image.
Identify whether road damage is visible.
Possible damage types:
1. Pothole
2. Crack
3. Damaged Road
4. Multiple Damages
5. No Damage

Severity must be one of:
LOW
MEDIUM
HIGH
NONE

Return ONLY valid JSON.
Required JSON structure:
{
    "damage_type": "Pothole",
    "severity": "HIGH",
    "confidence": 0.92,
    "recommendation": "Repair the pothole immediately."
}

Rules:
- confidence must be between 0 and 1.
- If no visible damage exists, use:
  damage_type = "No Damage"
  severity = "NONE"
- Do not invent GPS coordinates.
- Do not invent information that is not visible in the image.
- Keep recommendation short and practical.
"""


def analyze_road_image(image_path):
    with open(
        image_path,
        "rb"
    ) as image_file:
        image_bytes = image_file.read()

    encoded_image = base64.b64encode(
        image_bytes
    ).decode("utf-8")

    image_url = (
        "data:image/jpeg;base64,"
        + encoded_image
    )

    response = client.chat.completions.create(
        model=GROQ_MODEL,
        messages=[
            {
                "role": "system",
                "content": SYSTEM_PROMPT
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": (
                            "Inspect this road image "
                            "for road damage."
                        )
                    },
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": image_url
                        }
                    }
                ]
            }
        ],
        temperature=0,
        max_completion_tokens=500
    )

    content = (
        response
        .choices[0]
        .message
        .content
    )

    content = content.strip()

    if content.startswith("```"):
        content = content.replace(
            "```json",
            ""
        )
        content = content.replace(
            "```",
            ""
        )
        content = content.strip()

    result = json.loads(content)
    return normalize_result(result)


def normalize_result(result):
    damage_type = result.get(
        "damage_type",
        "No Damage"
    )
    severity = result.get(
        "severity",
        "NONE"
    )
    confidence = result.get(
        "confidence",
        0.0
    )
    recommendation = result.get(
        "recommendation",
        "No maintenance action required."
    )

    try:
        confidence = float(
            confidence
        )
    except:
        confidence = 0.0

    confidence = max(
        0.0,
        min(
            confidence,
            1.0
        )
    )

    severity = str(
        severity
    ).upper()

    if severity not in [
        "LOW",
        "MEDIUM",
        "HIGH",
        "NONE"
    ]:
        severity = "MEDIUM"

    return {
        "damage_type": str(
            damage_type
        ),
        "severity": severity,
        "confidence": confidence,
        "recommendation": str(
            recommendation
        )
    }
