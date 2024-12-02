package com.shuxuan.shuxuanwu_comp304lab4_ex1.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationFlow(): Flow<LatLng>
    suspend fun saveLocation(location: LatLng)
}
