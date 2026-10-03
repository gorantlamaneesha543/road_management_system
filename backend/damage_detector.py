from groq_service import analyze_road_image


def detect_damage(image_path):
    result = analyze_road_image(
        image_path
    )
    return {
        "damage_type":
            result["damage_type"],
        "severity":
            result["severity"],
        "confidence":
            result["confidence"],
        "recommendation":
            result["recommendation"]
    }
