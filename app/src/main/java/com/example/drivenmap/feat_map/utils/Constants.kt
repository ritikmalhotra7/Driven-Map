package com.example.drivenmap.feat_map.utils

import android.content.Context
import android.location.Geocoder
import com.example.drivenmap.feat_core.utils.logd
import java.util.Locale

object Constants {
    const val ADD_MEMBER_ID_FRAGMENT_RESULT_KEY = "ADD_MEMBER_ID_FRAGMENT_RESULT_KEY"
    const val ADD_MEMBER_IDs_KEY = "ADD_MEMBER_ID_KEY"

    const val ADDED_MEMBER_DETAILS_ID_KEY = "ADDED_MEMBER_DETAILS_ID_KEY"

    fun getLocationName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        addresses?.logd("addresses")
        val address = addresses?.firstOrNull()
        return address?.getAddressLine(0)?:""
    }
}