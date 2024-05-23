package com.example.drivenmap.feat_map.domain.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.data.dto.GroupDto
import com.example.drivenmap.feat_map.data.dto.LocationDto
import com.example.drivenmap.feat_map.data.dto.UserDto
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    suspend fun createGroup(createdBy: String, users: List<String>)
    suspend fun getGroup(groupId: String): Flow<ResponseState<GroupDto>>
    suspend fun getUser(userId: String): Flow<ResponseState<UserDto>>
    suspend fun updateUserLocationInGroup(
        groupId: String,
        userId: String,
        location: LocationDto
    )
    suspend fun cancelGroup(groupId: String)
}