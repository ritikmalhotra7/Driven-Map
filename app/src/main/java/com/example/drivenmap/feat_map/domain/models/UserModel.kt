package com.example.drivenmap.feat_map.domain.models

import com.google.type.DateTime
import com.google.type.LatLng

data class UserModel(
    val id:String? = null,
    val name:String? = null,
    val phoneNumber:String? = null,
    val email:String? = null,
    val isActive:Boolean = false,
    val activeTimeStarted:DateTime? = null,
    val currentLocation:LatLng? = null,
    val addedMembers:List<AddedUser> = listOf()
)
