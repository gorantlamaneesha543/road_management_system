package com.example.a31mod2case2

data class InspectionResponse(
    val success: Boolean,
    val message: String?,
    val inspection: Inspection?
)

data class Inspection(
    val id: Int,
    val damage_type: String,
    val severity: String,
    val confidence: Double,
    val latitude: Double,
    val longitude: Double,
    val recommendation: String,
    val timestamp: String
)

data class SummaryResponse(
    val success: Boolean,
    val total_inspections: Int,
    val total_damages: Int,
    val potholes: Int,
    val cracks: Int,
    val high_severity: Int
)

data class HealthResponse(
    val status: String,
    val service: String
)
