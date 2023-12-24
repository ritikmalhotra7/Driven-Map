package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDataFromFireStore @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(collectionName:String, documentName:String): Flow<ResponseState<Any>> {
        return mapRepository.getDataFromFireStore(collectionName, documentName)
    }
}