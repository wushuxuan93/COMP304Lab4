package com.shuxuan.shuxuanwu_comp304lab4_ex1.geofencing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.GeofenceStatusCodes

class GeofenceHelper(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    fun addGeofence(geofenceId: String, latLng: LatLng, radius: Float) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Toast.makeText(context, "Geofence Added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    val errorMessage = getGeofenceErrorString(e)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getGeofenceErrorString(e: Exception): String {
        return if (e is ApiException) {
            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                    "Geofence service is not available now"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                    "Your app has registered too many 2geofences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                    "You have provided too many PendingIntents to the addGeofences() call"
                else -> e.localizedMessage ?: "Unknown error"
            }
        } else {
            e.localizedMessage ?: "Unknown error"
        }
    }
}


