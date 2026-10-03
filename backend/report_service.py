import os
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from config import REPORT_FOLDER


def generate_report(
    inspection,
    filename
):
    os.makedirs(
        REPORT_FOLDER,
        exist_ok=True
    )

    report_path = os.path.join(
        REPORT_FOLDER,
        filename
    )

    pdf = canvas.Canvas(
        report_path,
        pagesize=A4
    )

    width, height = A4
    y = height - 60

    pdf.setFont(
        "Helvetica-Bold",
        20
    )
    pdf.drawString(
        50,
        y,
        "AI Road Inspection Report"
    )

    y -= 50
    pdf.setFont(
        "Helvetica",
        12
    )

    fields = [
        (
            "Inspection ID",
            inspection["id"]
        ),
        (
            "Damage Type",
            inspection["damage_type"]
        ),
        (
            "Severity",
            inspection["severity"]
        ),
        (
            "Confidence",
            f'{inspection["confidence"] * 100:.2f}%'
        ),
        (
            "Latitude",
            inspection["latitude"]
        ),
        (
            "Longitude",
            inspection["longitude"]
        ),
        (
            "Timestamp",
            inspection["timestamp"]
        ),
        (
            "Recommendation",
            inspection["recommendation"]
        )
    ]

    for label, value in fields:
        pdf.setFont(
            "Helvetica-Bold",
            11
        )
        pdf.drawString(
            50,
            y,
            f"{label}:"
        )
        pdf.setFont(
            "Helvetica",
            11
        )
        pdf.drawString(
            180,
            y,
            str(value)
        )
        y -= 30

    pdf.save()
    return report_path
