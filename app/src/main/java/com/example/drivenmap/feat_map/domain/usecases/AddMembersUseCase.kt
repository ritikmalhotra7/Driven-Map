package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddMembersUseCase @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(
        collectionName: String,
        hostId:String,
        addedMembersIds: List<String>
    ): Flow<ResponseState<Boolean>> {
        return mapRepository.addMembersAndStartSession(collectionName,hostId, addedMembersIds)
    }
}