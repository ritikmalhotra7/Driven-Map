package com.example.drivenmap.feat_map.data.dto

data class GroupDto(
    val id:String="",
    val users:List<UserXDto> = listOf(),
    val createdAt:String= "",
    val createdBy:String = ""
)
