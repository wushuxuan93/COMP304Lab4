package com.shuxuan.shuxuanwu_comp304lab4_ex1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.shuxuan.shuxuanwu_comp304lab4_ex1.viewmodel.LocationViewModel

@Composable
fun MapScreen(
    userLocation: LatLng,
    locationPermissionGranted: Boolean,
    viewModel: LocationViewModel
) {
    val context = LocalContext.current

    // Camera position state to control the camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    // Collect the route points from the ViewModel
    val routePoints by viewModel.routePoints.collectAsState()

    // List to hold markers
    val markers = remember { mutableStateListOf<LatLng>() }

    // Observe changes to routePoints to update the map
    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            // Calculate bounds to include all route points
            val boundsBuilder = LatLngBounds.Builder()
            routePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()

            // Adjust the camera to include the entire route with padding
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )

            Toast.makeText(context, "Route Plotted", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = locationPermissionGranted,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionGranted
            ),
            onMapClick = { latLng ->
                markers.add(latLng)
            }
        ) {
            // Draw markers on the map
            markers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker),
                    title = "Marker",
                    snippet = "Lat: ${marker.latitude}, Lng: ${marker.longitude}"
                )
            }

            // Draw the polyline for the route
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 10f,
                    jointType = JointType.ROUND,
                    endCap = RoundCap(),
                    startCap = RoundCap()
                )
            }
        }

        // Floating Action Buttons for Route Planning
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart)
        ) {
            FloatingActionButton(
                onClick = {
                    if (markers.size >= 2) {
                        val origin = markers[markers.size - 2]
                        val destination = markers.last()
                        viewModel.getRoute(origin, destination)
                    } else {
                        Toast.makeText(context, "Add at least two markers", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = "Plot Route",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            FloatingActionButton(
                onClick = {
                    if (markers.isNotEmpty()) {
                        markers.removeAt(markers.lastIndex)
                        Toast.makeText(context, "Last Marker Removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No markers to remove", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Marker",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

