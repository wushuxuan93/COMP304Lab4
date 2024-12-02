package com.shuxuan.shuxuanwu_comp304lab4_ex1.viewmodel

import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.shuxuan.shuxuanwu_comp304lab4_ex1.BuildConfig
import com.shuxuan.shuxuanwu_comp304lab4_ex1.api.RetrofitInstance
import com.shuxuan.shuxuanwu_comp304lab4_ex1.data.DirectionsResponse
import com.shuxuan.shuxuanwu_comp304lab4_ex1.data.Route
import com.shuxuan.shuxuanwu_comp304lab4_ex1.geofencing.GeofenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import retrofit2.HttpException
import java.io.IOException


class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Default location to Toronto (in case user location is not available)
    private val _userLocation = MutableStateFlow(LatLng(43.6532, -79.3832))
    val userLocation: StateFlow<LatLng> = _userLocation

    // Tracking permission status
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    // Handling background permission rationale
    private val _showBackgroundPermissionRationale = MutableStateFlow(false)
    val showBackgroundPermissionRationale: StateFlow<Boolean> = _showBackgroundPermissionRationale

    // Callback to receive location updates
    private var locationCallback: LocationCallback? = null

    // StateFlow to hold the route points
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints

    private var geofenceAdded = false // Track if geofence is already added
    private var geofenceHelper: GeofenceHelper =
        GeofenceHelper(getApplication()) // Initialize GeofenceHelper

    // Function to start receiving location updates
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 seconds interval
        ).build()

        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    location?.let {
                        _userLocation.value = LatLng(it.latitude, it.longitude)
                    }
                }
            }
        }

        try {
            // Request location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationViewModel", "Permission issue: ${e.message}")
        }
    }

    // Function to stop receiving location updates
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    // Update permission state
    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    // Trigger background permission rationale
    fun triggerBackgroundPermissionRationale() {
        _showBackgroundPermissionRationale.value = true
    }

    // Hide background permission rationale
    fun hideBackgroundPermissionRationale() {
        _showBackgroundPermissionRationale.value = false
    }

    // Function to add geofence if not already added
    fun addGeofenceIfNeeded(userLocation: LatLng) {
        if (!geofenceAdded) {
            geofenceHelper.addGeofence(UUID.randomUUID().toString(), userLocation, 100f)
            geofenceAdded = true
        }
    }
    private val apiKey = "AIzaSyA4EW_vZEAux0ZF1JUebEcbbaIw_Q1JfV4"
    // Route planning logic
    fun getRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"
                val response = RetrofitInstance.api.getDirections(
                    origin = originStr,
                    destination = destinationStr,
                    //apiKey = BuildConfig.DIRECTIONS_API_KEY
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val directionsResponse: DirectionsResponse? = response.body()
                    Log.d("API_RESPONSE", "Response: $directionsResponse")
                    if (directionsResponse != null) {
                        when (directionsResponse.status) {
                            "OK" -> {
                                if (directionsResponse.routes.isNotEmpty()) {
                                    val route = directionsResponse.routes[0]
                                    val polyline = route.overviewPolyline?.points
                                    if (polyline != null) {
                                        val decodedPath = decodePolyline(polyline)
                                        _routePoints.value = decodedPath
                                        Log.d("LocationViewModel", "Route fetched and decoded successfully.")
                                    } else {
                                        Log.e("LocationViewModel", "overviewPolyline is null.")
                                        // Alternative: Reconstruct route from step polylines
                                        val reconstructedPath = reconstructRouteFromSteps(route)
                                        if (reconstructedPath.isNotEmpty()) {
                                            _routePoints.value = reconstructedPath
                                            Log.d("LocationViewModel", "Route reconstructed from steps successfully.")
                                        } else {
                                            Log.e("LocationViewModel", "Failed to reconstruct route from steps.")
                                        }
                                    }
                                } else {
                                    Log.e("LocationViewModel", "No routes found.")
                                }
                            }
                            else -> {
                                Log.e(
                                    "LocationViewModel",
                                    "Directions API Error: ${directionsResponse.status}, Message: ${directionsResponse.errorMessage}"
                                )
                            }
                        }
                    } else {
                        Log.e("LocationViewModel", "Directions API response body is null.")
                    }
                } else {
                    Log.e("LocationViewModel", "Directions API HTTP Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                Log.e("LocationViewModel", "Network Error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                Log.e("LocationViewModel", "HTTP Exception: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Unexpected Error: ${e.localizedMessage}")
            }
        }
    }

    // Function to decode polyline points
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }

    // Function to reconstruct route from step polylines
    private fun reconstructRouteFromSteps(route: Route): List<LatLng> {
        val path = mutableListOf<LatLng>()
        route.legs.forEach { leg ->
            leg.steps.forEach { step ->
                val stepPolyline = step.polyline.points
                val decodedStep = decodePolyline(stepPolyline)
                path.addAll(decodedStep)
            }
        }
        return path
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates() // Clean up to prevent memory leaks
    }
}