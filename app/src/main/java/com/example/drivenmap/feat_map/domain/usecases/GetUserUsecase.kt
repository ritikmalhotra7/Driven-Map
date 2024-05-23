package com.example.drivenmap.feat_map.domain.usecases

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.data.dto.UserDto
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUsecase @Inject constructor(private val repo:MapRepository) {
    suspend operator fun invoke(userId:String): Flow<ResponseState<UserDto>> = repo.getUser(userId)
}