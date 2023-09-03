package com.example.drivenmap.feat_map.domain.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.UserModel
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getUserDataFromFireStore(collection:String, document:String): Flow<ResponseState<UserModel>>
    fun setUserDataToFireStore(collection:String, document:String, dataToBeSet:UserModel): Flow<ResponseState<Boolean>>
}