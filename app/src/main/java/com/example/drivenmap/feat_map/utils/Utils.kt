package com.example.drivenmap.feat_map.utils

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions

object Utils {
    const val MAP_ZOOM = 18f
    const val REQUEST_CODE_LOCATION_PERMISSION = 100
    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val FASTEST_LOCATION_INTERVAL = 1000L
    const val TIMER_UPDATE_INTERVAL = 50L
    const val LOCATION_UPDATES = "LOCATION_UPDATES"
    const val CURRENT_LOCATION = "CURRENT_LOCATION"
    const val CURRENT_LOCATION_LATITUDE = "CURRENT_LOCATION_LATITUDE"
    const val CURRENT_LOCATION_LONGITUDE = "CURRENT_LOCATION_LONGITUDE"

    fun hasLocationPermissions(ctx: Context): Boolean = EasyPermissions.hasPermissions(
        ctx,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}