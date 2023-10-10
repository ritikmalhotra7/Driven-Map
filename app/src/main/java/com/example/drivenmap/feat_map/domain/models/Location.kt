package com.example.drivenmap.feat_map.domain.models

import java.io.Serializable

data class Location(
    val latitude:Double? = null,
    val longitude:Double? = null,
    val name:String? = null
):Serializable
