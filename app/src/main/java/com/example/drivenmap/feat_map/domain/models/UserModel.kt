package com.example.drivenmap.feat_map.domain.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.type.DateTime
import java.time.LocalDateTime
import java.util.Calendar

data class UserModel(
    val id:String? = null,
    val name:String? = null,
    val phoneNumber:String? = null,
    val email:String? = null,
    val isActive:Boolean = false,
    val activeTimeStarted:Calendar? = null,
    val currentLocation: LatLng? = null,
    val addedMembers:List<AddedUser> = listOf(),
    val profilePhoto: Bitmap? = null
)
