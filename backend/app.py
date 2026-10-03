import os
import uuid
from flask import (
    Flask,
    jsonify,
    request,
    send_file
)
from flask_cors import CORS

from config import (
    FLASK_HOST,
    FLASK_PORT,
    UPLOAD_FOLDER
)
from database import (
    initialize_database,
    insert_inspection,
    get_summary,
    get_all_inspections,
    get_connection
)
from damage_detector import detect_damage
from report_service import generate_report

app = Flask(__name__)
CORS(app)

os.makedirs(
    UPLOAD_FOLDER,
    exist_ok=True
)

initialize_database()


@app.route(
    "/",
    methods=["GET"]
)
def home():
    return jsonify({
        "application":
            "AI-Based Road Management and Road Damage Detection System",
        "status":
            "running",
        "message":
            "Flask backend is working."
    })


@app.route(
    "/health",
    methods=["GET"]
)
def health():
    return jsonify({
        "status":
            "healthy",
        "service":
            "AI Road Inspection Backend"
    })


@app.route(
    "/api/inspect",
    methods=["POST"]
)
def inspect():
    try:
        if "image" not in request.files:
            return jsonify({
                "success": False,
                "message":
                    "Image file is required."
            }), 400

        image = request.files["image"]
        latitude = request.form.get("latitude")
        longitude = request.form.get("longitude")

        if latitude is None or longitude is None:
            return jsonify({
                "success": False,
                "message":
                    "GPS coordinates are required."
            }), 400

        try:
            latitude = float(latitude)
            longitude = float(longitude)
        except ValueError:
            return jsonify({
                "success": False,
                "message":
                    "Invalid GPS coordinates."
            }), 400

        if image.filename == "":
            return jsonify({
                "success": False,
                "message":
                    "Invalid image."
            }), 400

        extension = ".jpg"
        filename = (
            str(uuid.uuid4())
            + extension
        )

        image_path = os.path.join(
            UPLOAD_FOLDER,
            filename
        )

        image.save(image_path)

        # AI analysis
        result = detect_damage(image_path)

        inspection_id = insert_inspection(
            damage_type=result["damage_type"],
            severity=result["severity"],
            confidence=result["confidence"],
            latitude=latitude,
            longitude=longitude,
            recommendation=result["recommendation"],
            image_path=image_path
        )

        connection = get_connection()
        cursor = connection.cursor()
        cursor.execute(
            """
            SELECT *
            FROM inspections
            WHERE id = ?
            """,
            (inspection_id,)
        )
        row = cursor.fetchone()
        connection.close()

        inspection = dict(row)

        return jsonify({
            "success": True,
            "message":
                "Road inspection completed.",
            "inspection":
                inspection
        })

    except Exception as e:
        return jsonify({
            "success": False,
            "message":
                str(e)
        }), 500


@app.route(
    "/api/summary",
    methods=["GET"]
)
def summary():
    try:
        data = get_summary()
        return jsonify({
            "success": True,
            **data
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500


@app.route(
    "/api/inspections",
    methods=["GET"]
)
def inspections():
    try:
        data = get_all_inspections()
        return jsonify({
            "success": True,
            "inspections": data
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500


@app.route(
    "/api/report/<int:inspection_id>",
    methods=["GET"]
)
def report(inspection_id):
    try:
        connection = get_connection()
        cursor = connection.cursor()
        cursor.execute(
            """
            SELECT *
            FROM inspections
            WHERE id = ?
            """,
            (inspection_id,)
        )
        row = cursor.fetchone()
        connection.close()

        if row is None:
            return jsonify({
                "success": False,
                "message":
                    "Inspection not found."
            }), 404

        inspection = dict(row)
        filename = (
            f"inspection_"
            f"{inspection_id}.pdf"
        )
        report_path = generate_report(
            inspection,
            filename
        )

        return send_file(
            report_path,
            as_attachment=True,
            download_name=filename
        )

    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500


if __name__ == "__main__":
    print(
        "\n===================================="
    )
    print(
        "AI ROAD INSPECTION BACKEND"
    )
    print(
        "===================================="
    )
    print(
        f"Server running on "
        f"http://{FLASK_HOST}:{FLASK_PORT}"
    )
    app.run(
        host=FLASK_HOST,
        port=FLASK_PORT,
        debug=True
    )
