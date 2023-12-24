package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetDataToFireStore @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(collectionName:String, documentName:String, data:Any): Flow<ResponseState<Boolean>> {
        return mapRepository.setDataToFireStore(collectionName, documentName,data)
    }
}