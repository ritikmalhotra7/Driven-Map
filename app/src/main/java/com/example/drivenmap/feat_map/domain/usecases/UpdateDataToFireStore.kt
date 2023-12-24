package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateDataToFireStore @Inject constructor(private val mapRepository:MapRepository) {
    operator fun invoke(collectionName:String, documentName:String, data:Map<String,Any>): Flow<ResponseState<Boolean>> {
        return mapRepository.updateDataToFireStore(collectionName, documentName,data)
    }
}