package com.shuxuan.shuxuanwu_comp304lab4_ex1.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocationRepositoryImpl : LocationRepository {

    private val locationFlow = MutableStateFlow<LatLng>(LatLng(0.0, 0.0))

    override fun getLocationFlow(): Flow<LatLng> = locationFlow

    override suspend fun saveLocation(location: LatLng) {
        locationFlow.value = location
    }
}
