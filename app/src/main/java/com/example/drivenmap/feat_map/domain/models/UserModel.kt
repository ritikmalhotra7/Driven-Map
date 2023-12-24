package com.example.drivenmap.feat_map.domain.models

import android.graphics.Bitmap
import android.net.Uri
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
    val isHost:Boolean? = null,
    val currentLocation: Location? = null,
    val addedMembers:ArrayList<AddedUser> = arrayListOf(),
    val profilePhoto: String? = null
)

