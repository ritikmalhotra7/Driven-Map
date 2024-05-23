package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import javax.inject.Inject

class CancelGroupUsecase @Inject constructor(private val repo:MapRepository) {
    suspend operator fun invoke(groupId:String) = repo.cancelGroup(groupId)
}