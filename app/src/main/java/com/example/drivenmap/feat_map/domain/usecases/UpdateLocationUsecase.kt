package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_map.data.dto.LocationDto
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import javax.inject.Inject

class UpdateLocationUsecase @Inject constructor(private val repo: MapRepository) {
    suspend operator fun invoke(groupId: String, userId: String, location: LocationDto) =
        repo.updateUserLocationInGroup(groupId, userId, location)
}