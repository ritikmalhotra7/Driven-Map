package com.example.drivenmap.feat_map.domain.models

data class UserModel(
    val id:String? = null,
    val name:String? = null,
    val phoneNumber:String? = null,
    val email:String? = null,
    val active:Boolean? = null,
    val host:Boolean? = null,
    val currentLocation: Location? = null,
    val addedMembers:ArrayList<AddedUser> = arrayListOf(),
    val profilePhoto: String? = null
)

