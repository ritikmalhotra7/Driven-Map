package com.example.drivenmap.feat_map.data.dto

data class UserDto(
    val id:String = "",
    val userName:String = "",
    val email:String = "",
    val phoneNumber:String? = null,
    val profilePhoto:String? = null,
    val groupId:String? = null
)
