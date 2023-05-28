package com.example.bookbuddy.models

data class Response(
    val bbox: List<Double>,
    val features: List<Feature>,
    val metadata: Metadata,
    val type: String
)

data class Feature(
    val bbox: List<Double>,
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)

data class Metadata(
    val attribution: String,
    val engine: Engine,
    val query: Query,
    val service: String,
    val timestamp: Long
)

data class Engine(
    val build_date: String,
    val graph_date: String,
    val version: String
)

data class Geometry(
    val coordinates: List<List<Double>>,
    val type: String
)

data class Properties(
    val segments: List<Segment>,
    val summary: Summary,
    val way_points: List<Int>
)

data class Query(
    val coordinates: List<List<Double>>,
    val format: String,
    val profile: String
)

data class Segment(
    val distance: Double,
    val duration: Double,
    val steps: List<Step>
)

data class Step(
    val distance: Double,
    val duration: Double,
    val instruction: String,
    val name: String,
    val type: Int,
    val way_points: List<Int>
)

data class Summary(
    val distance: Double,
    val duration: Double
)

data class CleanResponse(
    val coordinates: List<List<Double>>,
    val distance: Double,
    val duration: Double
)