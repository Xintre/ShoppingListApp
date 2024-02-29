package com.xintre.shoppinglist

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String,
    val error_message: String?
)

data class GeocodingResult(
    val formatted_address: String
)
