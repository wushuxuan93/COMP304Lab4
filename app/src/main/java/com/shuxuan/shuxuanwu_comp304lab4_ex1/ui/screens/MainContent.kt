package com.shuxuan.shuxuanwu_comp304lab4_ex1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.shuxuan.shuxuanwu_comp304lab4_ex1.viewmodel.LocationViewModel

@Composable
fun MainContent(
    userLocation: LatLng,
    locationPermissionGranted: Boolean,
    showBackgroundPermissionRationale: Boolean,
    onRequestBackgroundPermission: () -> Unit,
    viewModel: LocationViewModel // Added viewModel parameter
) {
    // Capture the context outside the lambda
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            userLocation = userLocation,
            locationPermissionGranted = locationPermissionGranted,
            viewModel = viewModel // Pass viewModel to MapScreen
        )

        // Show Background Permission Rationale Dialog
        if (showBackgroundPermissionRationale) {
            BackgroundPermissionDialog(
                onGrant = onRequestBackgroundPermission,
                onDeny = {
                    Toast.makeText(context, "Background location permission denied", Toast.LENGTH_SHORT).show()
                    viewModel.hideBackgroundPermissionRationale()
                }
            )
        }
    }
}

@Composable
fun BackgroundPermissionDialog(
    onGrant: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Optionally handle dismiss */ },
        title = { Text(text = "Background Location Permission") },
        text = { Text(text = "This app needs background location access to provide geofencing and background location updates.") },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Deny")
            }
        }
    )
}
