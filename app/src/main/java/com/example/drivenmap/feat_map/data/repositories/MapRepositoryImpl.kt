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
    override fun getDataFromFireStore(
        collection: String,
        document: String
    ): Flow<ResponseState<Any>> = flow {
        emit(ResponseState.Loading())
        try {
            val data = fireStore.collection(collection).document(document).get().await()
            if (data.exists() && data != null) {
                emit(ResponseState.Success(data.toObject(UserModel::class.java) as Any))
            } else {
                emit(ResponseState.Error("Cannot find Data!"))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e.toString()))
        }
    }

    override fun setDataToFireStore(
        collection: String,
        document: String,
        dataToBeSet: Any
    ): Flow<ResponseState<Boolean>> = flow {
        emit(ResponseState.Loading())
        val data = fireStore.collection(collection).document(document).set(dataToBeSet)
        if (data.isSuccessful) {
            emit(ResponseState.Success(true))
        } else {
            emit(ResponseState.Error(data.exception.toString()))
        }
    }

    override fun updateDataToFireStore(
        collection: String,
        document: String,
        dataToBeSet: Map<String,Any>
    ): Flow<ResponseState<Boolean>> = flow{
        emit(ResponseState.Loading())
        val task = fireStore.collection(collection).document(document).update(dataToBeSet)
        try{
            task.await()
            if(task.isSuccessful) emit(ResponseState.Success(true))
            else emit(ResponseState.Error(task.exception.toString()))
        }catch (e:Exception){
            emit(ResponseState.Error(e.toString()))
        }
    }
}