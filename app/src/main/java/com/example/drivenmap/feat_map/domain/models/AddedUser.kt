package com.example.drivenmap.feat_map.domain.models

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.type.DateTime
import java.time.LocalDateTime
import java.util.Calendar

data class AddedUser(
    val id:String? = null,
    val name:String? = null,
    val currentLocation:Location? = null
){
    override fun toString(): String {
        return "AddedUser(id=$id, name=$name, location=$currentLocation)"
    }
}
