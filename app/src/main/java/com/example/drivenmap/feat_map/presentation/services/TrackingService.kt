package com.example.drivenmap.feat_map.presentation.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import com.example.drivenmap.feat_map.utils.Utils
import com.example.drivenmap.feat_map.utils.Utils.CURRENT_LOCATION
import com.example.drivenmap.feat_map.utils.Utils.CURRENT_LOCATION_LATITUDE
import com.example.drivenmap.feat_map.utils.Utils.CURRENT_LOCATION_LONGITUDE
import com.example.drivenmap.feat_map.utils.Utils.FASTEST_LOCATION_INTERVAL
import com.example.drivenmap.feat_map.utils.Utils.LOCATION_UPDATES
import com.example.drivenmap.feat_map.utils.Utils.LOCATION_UPDATE_INTERVAL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class TrackingService: Service() {
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onBind(p0: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Utils.hasLocationPermissions(this)) {
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_LOCATION_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        return START_STICKY
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val location = p0.lastLocation
            val latLng = LatLng(location.latitude, location.longitude)
            val intent = Intent(LOCATION_UPDATES).apply {
                putExtra(CURRENT_LOCATION_LATITUDE,latLng.latitude)
                putExtra(CURRENT_LOCATION_LONGITUDE,latLng.longitude)
            }
            sendBroadcast(intent)
        }
    }
}