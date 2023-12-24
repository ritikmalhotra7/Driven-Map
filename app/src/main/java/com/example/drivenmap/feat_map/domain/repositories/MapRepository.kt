package com.example.drivenmap.feat_map.domain.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.UserModel
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getDataFromFireStore(collection:String, document:String): Flow<ResponseState<Any>>
    fun setDataToFireStore(collection:String, document:String, dataToBeSet:Any): Flow<ResponseState<Boolean>>

    fun updateDataToFireStore(collection:String, document:String, dataToBeSet:Map<String,Any>):Flow<ResponseState<Boolean>>
}