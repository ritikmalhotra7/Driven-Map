package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StopSessionUseCase @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(collection: String, addedMembersIds: List<String>): Flow<ResponseState<Boolean>> {
        return mapRepository.stopSession(collection, addedMembersIds)
    }
}