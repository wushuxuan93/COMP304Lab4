package com.shuxuan.shuxuanwu_comp304lab4_ex1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.shuxuan.shuxuanwu_comp304lab4_ex1.viewmodel.LocationViewModel
import com.shuxuan.shuxuanwu_comp304lab4_ex1.geofencing.GeofenceHelper
import androidx.compose.runtime.collectAsState
import com.shuxuan.shuxuanwu_comp304lab4_ex1.ui.screens.MainContent
import com.shuxuan.shuxuanwu_comp304lab4_ex1.ui.theme.ShuxuanWu_COMP304Lab4_Ex1Theme
import com.shuxuan.shuxuanwu_comp304lab4_ex1.worker.LocationHelper


class MainActivity : ComponentActivity() {
    // ViewModel for location management
    private val viewModel: LocationViewModel by viewModels()

    // Permission launcher to request foreground location permission
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Foreground location permission granted")
            // Permission has already been granted, proceed with accessing location
            onLocationPermissionGranted()
        } else {
            Log.d("MainActivity", "Foreground location permission not granted")
            Toast.makeText(this, "Foreground location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher to request background location permission
    private val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Background location permission granted")
            // Proceed with background location tasks
            onBackgroundLocationPermissionGranted()
        } else {
            Log.d("MainActivity", "Background location permission not granted")
            Toast.makeText(
                this,
                "Background location permission not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var geofenceHelper: GeofenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize GeofenceHelper
        geofenceHelper = GeofenceHelper(this)

        // Initial permission check
        checkLocationPermissions()

        setContent {
            ShuxuanWu_COMP304Lab4_Ex1Theme {
                // Collecting state from ViewModel
                val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsState()
                val userLocation by viewModel.userLocation.collectAsState()
                val showBackgroundRationale by viewModel.showBackgroundPermissionRationale.collectAsState()

                // Main UI
                MainContent(
                    userLocation = userLocation,
                    locationPermissionGranted = locationPermissionGranted,
                    showBackgroundPermissionRationale = showBackgroundRationale,
                    onRequestBackgroundPermission = {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        viewModel.hideBackgroundPermissionRationale() // Hide dialog after requesting
                    },
                    viewModel = viewModel
                )

                // Side-effect to add geofence when permission is granted
                LaunchedEffect(locationPermissionGranted, userLocation) {
                    if (locationPermissionGranted) {
                        viewModel.addGeofenceIfNeeded(userLocation)
                    }
                }
            }
        }
    }

    // Function to check and request foreground location permissions
    private fun checkLocationPermissions() {
        val permissionFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permissionFineLocation)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            permissionLauncher.launch(permissionFineLocation)
        } else {
            // Permission has already been granted, proceed with accessing location
            onLocationPermissionGranted()
        }
    }

    private fun onLocationPermissionGranted() {
        viewModel.setLocationPermissionGranted(true)
        viewModel.startLocationUpdates()

        // Setup location-related features
        setupLocationFeatures()

        // Schedule location updates using the helper function
        LocationHelper.scheduleLocationUpdates(this)

        // After handling foreground permission, check for background permission
        checkBackgroundLocationPermission()
    }

    // Function to check and request background location permission
    @SuppressLint("ObsoleteSdkInt")
    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Trigger the rationale dialog via ViewModel
                viewModel.triggerBackgroundPermissionRationale()
            } else {
                // Background permission already granted
                onBackgroundLocationPermissionGranted()
            }
        } else {
            // Background permission not required for versions below Android Q
            onBackgroundLocationPermissionGranted()
        }
    }

    private fun onBackgroundLocationPermissionGranted() {
        // Proceed with background location tasks, e.g., geofencing
        Log.d("MainActivity", "Background location permission is granted.")
        setupBackgroundLocationFeatures()
    }

    // Setup location-related features
    private fun setupLocationFeatures() {
        viewModel.startLocationUpdates()
    }

    // Setup background location-related features
    private fun setupBackgroundLocationFeatures() {
        viewModel.userLocation.value.let { userLocation ->
            viewModel.addGeofenceIfNeeded(userLocation)
        }
    }

//    // Function to add a geofence using GeofenceHelper
//    private fun addGeofence(latLng: LatLng, radius: Float) {
//        // Check if permission is granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            // Generate unique ID for geofence
//            val geofenceId = UUID.randomUUID().toString()
//
//            // Use GeofenceHelper to add a geofence
//            geofenceHelper.addGeofence(geofenceId, latLng, radius)
//        } else {
//            Toast.makeText(this, "Location permission not granted for geofencing", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
    }
}

