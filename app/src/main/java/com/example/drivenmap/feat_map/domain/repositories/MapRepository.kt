package com.example.drivenmap.feat_map.domain.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getUser(collection:String, document:String): Flow<ResponseState<UserModel>>
    fun addMembersAndStartSession(collection: String, addedMembersIds:List<String>):Flow<ResponseState<Boolean>>
    fun locationUpdate(collection: String,document: String,location:Location):Flow<ResponseState<Boolean>>
    fun stopSession(collection: String,addedMembersIds: List<String>):Flow<ResponseState<Boolean>>
}