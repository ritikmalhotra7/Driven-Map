package com.example.drivenmap.feat_map.domain.models

import android.graphics.Bitmap
import com.google.type.DateTime
import com.google.type.LatLng

data class AddedUser(
    val id:String? = null,
    val name:String? = null,
    val phoneNumber:String? = null,
    val email:String? = null,
    val isActive:Boolean = false,
    val activeTimeStarted: DateTime? = null,
    val currentLocation: LatLng? = null,
    val profilePhoto:Bitmap? = null,
    val distanceAway:String? = null
){
    override fun toString(): String {
        return "AddedUser(id=$id, name=$name, phoneNumber=$phoneNumber, email=$email, isActive=$isActive, activeTimeStarted=$activeTimeStarted, currentLocation=$currentLocation, profilePhoto=$profilePhoto, distanceAway=$distanceAway)"
    }
}
