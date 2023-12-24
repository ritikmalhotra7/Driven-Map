package com.example.drivenmap.feat_map.data.mappers

import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.google.firebase.firestore.auth.User

fun UserModel.toAddedUser():AddedUser{
    return AddedUser(this.id, this.name)
}