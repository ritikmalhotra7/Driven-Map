package com.example.drivenmap.feat_map.data.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore
) : MapRepository {
    override fun getUserDataFromFireStore(
        collection: String,
        document: String
    ): Flow<ResponseState<UserModel>> = flow {
        emit(ResponseState.Loading())
        try {
            val data = fireStore.collection(collection).document(document).get().await()
            val user = data.toObject(UserModel::class.java)
            if (data.exists() && user != null) {
                emit(ResponseState.Success(user))
            } else {
                emit(ResponseState.Error("Cannot find User!"))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e.toString()))
        }
    }

    override fun setUserDataToFireStore(
        collection: String,
        document: String,
        dataToBeSet: UserModel
    ): Flow<ResponseState<Boolean>> = flow {
        emit(ResponseState.Loading())
        val data = fireStore.collection(collection).document(document).set(dataToBeSet)
        if (data.isSuccessful) {
            emit(ResponseState.Success(true))
        } else {
            emit(ResponseState.Success(false))
        }
    }
}