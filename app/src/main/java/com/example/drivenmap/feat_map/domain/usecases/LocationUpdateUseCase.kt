package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationUpdateUseCase  @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(collectionName:String, documentName:String,location: Location): Flow<ResponseState<Boolean>> {
        return mapRepository.locationUpdate(collectionName, documentName,location)
    }
}