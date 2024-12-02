package com.shuxuan.shuxuanwu_comp304lab4_ex1.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
object LocationHelper {
    fun scheduleLocationUpdates(context: Context) {
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LocationUpdateWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            locationWorkRequest
        )
    }
}


