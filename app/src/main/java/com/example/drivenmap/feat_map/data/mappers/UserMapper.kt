package com.example.drivenmap.feat_map.data.mappers

import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.UserModel

fun UserModel.toAddedUser(): AddedUser {
    return AddedUser(
        id,
        name,
        phoneNumber,
        email,
        isActive,
        activeTimeStarted,
        currentLocation,
        profilePhoto,
        distanceAway = null
    )
}