import sqlite3
from datetime import datetime
from config import DATABASE_NAME


def get_connection():
    connection = sqlite3.connect(
        DATABASE_NAME
    )
    connection.row_factory = sqlite3.Row
    return connection


def initialize_database():
    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS inspections (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            damage_type TEXT NOT NULL,
            severity TEXT NOT NULL,
            confidence REAL NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            recommendation TEXT,
            image_path TEXT,
            timestamp TEXT NOT NULL
        )
        """
    )
    connection.commit()
    connection.close()


def insert_inspection(
    damage_type,
    severity,
    confidence,
    latitude,
    longitude,
    recommendation,
    image_path
):
    connection = get_connection()
    cursor = connection.cursor()
    timestamp = datetime.now().isoformat()
    cursor.execute(
        """
        INSERT INTO inspections
        (
            damage_type,
            severity,
            confidence,
            latitude,
            longitude,
            recommendation,
            image_path,
            timestamp
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            damage_type,
            severity,
            confidence,
            latitude,
            longitude,
            recommendation,
            image_path,
            timestamp
        )
    )
    inspection_id = cursor.lastrowid
    connection.commit()
    connection.close()
    return inspection_id


def get_summary():
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute(
        "SELECT COUNT(*) FROM inspections"
    )
    total_inspections = cursor.fetchone()[0]

    cursor.execute(
        """
        SELECT COUNT(*)
        FROM inspections
        WHERE damage_type != 'No Damage'
        """
    )
    total_damages = cursor.fetchone()[0]

    cursor.execute(
        """
        SELECT COUNT(*)
        FROM inspections
        WHERE LOWER(damage_type) LIKE '%pothole%'
        """
    )
    potholes = cursor.fetchone()[0]

    cursor.execute(
        """
        SELECT COUNT(*)
        FROM inspections
        WHERE LOWER(damage_type) LIKE '%crack%'
        """
    )
    cracks = cursor.fetchone()[0]

    cursor.execute(
        """
        SELECT COUNT(*)
        FROM inspections
        WHERE UPPER(severity) = 'HIGH'
        """
    )
    high_severity = cursor.fetchone()[0]

    connection.close()

    return {
        "total_inspections": total_inspections,
        "total_damages": total_damages,
        "potholes": potholes,
        "cracks": cracks,
        "high_severity": high_severity
    }


def get_all_inspections():
    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute(
        """
        SELECT *
        FROM inspections
        ORDER BY id DESC
        """
    )
    rows = cursor.fetchall()
    connection.close()
    return [dict(row) for row in rows]
