package com.shuxuan.shuxuanwu_comp304lab4_ex1.data

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("status") val status: String,
    @SerializedName("error_message") val errorMessage: String? = null
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline?,
    @SerializedName("legs") val legs: List<Leg>
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)

data class Leg(
    @SerializedName("steps") val steps: List<Step>,
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_address") val endAddress: String,
    @SerializedName("end_location") val endLocation: LatLngResponse,
    @SerializedName("start_address") val startAddress: String,
    @SerializedName("start_location") val startLocation: LatLngResponse
)

data class Step(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_location") val endLocation: LatLngResponse,
    @SerializedName("start_location") val startLocation: LatLngResponse,
    @SerializedName("polyline") val polyline: OverviewPolyline,
    @SerializedName("html_instructions") val htmlInstructions: String,
    @SerializedName("travel_mode") val travelMode: String
)

data class Distance(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Duration(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class LatLngResponse(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
