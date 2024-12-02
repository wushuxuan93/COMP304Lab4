package com.shuxuan.shuxuanwu_comp304lab4_ex1.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "LocationUpdateWorker"
    }

    override suspend fun doWork(): Result {
        //Check if location permissions are granted
        val isFineLocationGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isBackgroundLocationGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Proceed if permissions are granted
        if (isFineLocationGranted && isBackgroundLocationGranted) {
            return fetchAndProcessLocation()
        } else {
            // Permissions are not granted, log and return failure
            Log.e(TAG, "Location permissions are not granted.")
            return Result.failure()
        }
    }

    private suspend fun fetchAndProcessLocation(): Result {
        return try {
            // Initialize FusedLocationProviderClient
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

            // Fetch the last known location using coroutines
            val location = withContext(Dispatchers.IO) {
                try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Missing location permissions")
                    return@withContext null
                }
            }

            // Handle obtained location
            return if (location != null) {
                Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                Result.success()
            } else {
                Log.w(TAG, "Location is null. Retrying...")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred while fetching location: ${e.localizedMessage}")
            e.printStackTrace()
            Result.retry()
        }
    }
}