package com.example.drivenmap.feat_map.data.dto

data class UserXDto(
    val id:String="",
    val email:String = "",
    val profilePhoto:String? = null,
    val location:LocationDto? = null
)