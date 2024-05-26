package com.example.drivenmap.feat_core.utils

import android.util.Log

object Utils {
    const val MAP_ZOOM = 18f
    const val REQUEST_CODE_FORE_LOCATION_PERMISSION = 100
    const val REQUEST_CODE_BACK_LOCATION_PERMISSION = 100
    const val LOCATION_UPDATE_INTERVAL = 15000L
    const val FASTEST_LOCATION_INTERVAL = 10000L
    const val TIMER_UPDATE_INTERVAL = 50L
    const val LOCATION_UPDATES = "LOCATION_UPDATES"
    const val CURRENT_LOCATION = "CURRENT_LOCATION"
    const val CURRENT_LOCATION_LATITUDE = "CURRENT_LOCATION_LATITUDE"
    const val CURRENT_LOCATION_LONGITUDE = "CURRENT_LOCATION_LONGITUDE"
    const val USER_COLLECTION_NAME = "USERS"

}

fun Any.logd(tag:String = ""){
    Log.d("Taget: $tag", this.toString())
}