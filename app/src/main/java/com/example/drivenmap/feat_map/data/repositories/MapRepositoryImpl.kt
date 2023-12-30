package com.example.drivenmap.feat_map.data.repositories

import android.util.Log
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_map.data.mappers.toAddedUser
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore
) : MapRepository {
    override fun getUser(
        collection: String,
        document: String
    ): Flow<ResponseState<UserModel>> = callbackFlow {
        trySend(ResponseState.Loading())
        try {
            fireStore.collection(collection).document(document)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        trySend(ResponseState.Success(snapshot.toObject(UserModel::class.java)!!))
                    } else {
                        trySend(ResponseState.Error(error?.message ?: "Cannot Find Data"))
                    }
                }
        } catch (e: Exception) {
            trySend(ResponseState.Error(e.toString()))
        }
        awaitClose()
    }

    override fun addMembersAndStartSession(
        collection: String,
        hostId:String,
        addedMembersIds: List<String>
    ): Flow<ResponseState<Boolean>> = flow {
        emit(ResponseState.Loading())
        val mCollection = fireStore.collection(collection)
        try {
            mCollection.whereIn("id", addedMembersIds).snapshots().take(1).collect {
                it.documents.forEach { documentSnapshot ->
                    val dataToBeUpdated = mutableMapOf(
                        Pair(
                            "addedMembers",
                            it.documents.filterNot { it.get("id") == documentSnapshot.id }
                                .map { it.toObject(UserModel::class.java)?.toAddedUser() }
                        ), Pair("active", true)
                    )
                    if(documentSnapshot.id == hostId){
                        dataToBeUpdated["host"] = true
                    }
                    mCollection.document(documentSnapshot.id).update(dataToBeUpdated).await()
                }
            }
            emit(ResponseState.Success(true))

        } catch (e: Exception) {
            emit(ResponseState.Error(e.toString()))
        }
    }

    override fun locationUpdate(
        collection: String,
        document: String,
        location: Location
    ): Flow<ResponseState<Boolean>> = callbackFlow {
        trySend(ResponseState.Loading())
        val mCollection = fireStore.collection(collection)
        try {
            mCollection.document(document).update("currentLocation", location)
                .addOnSuccessListener {
                    trySend(ResponseState.Success(true))
                }
        } catch (e: Exception) {
            trySend(ResponseState.Error(e.toString()))
        }
        awaitClose()
    }

    override fun locationUpdateInOthers(
        collection: String,
        membersAdded: List<String>,
        currentId:String,
        location:Location
    ): Flow<ResponseState<Boolean>> = flow{
        val mCollection = fireStore.collection(collection)
        emit(ResponseState.Loading())
        try {
            val addedMembers = arrayListOf<AddedUser>()
            mCollection.whereIn("id", membersAdded).snapshots().take(1).collectLatest {
                it.documents.forEach { documentSnapshot ->
                    val addedUser = documentSnapshot.toObject(UserModel::class.java)!!.toAddedUser()
                    addedMembers.add(addedUser)
                    Log.d("taget-location-document","${addedUser.currentLocation}, ${addedUser.id}")
                }
                val dataToBeUpdated = mapOf(
                    Pair("addedMembers",addedMembers),
                )
                mCollection.document(currentId).update(dataToBeUpdated).await()
                Log.d("taget-location-others","${addedMembers.map { it.currentLocation?.latitude }},${addedMembers.map { it.id }}" )
            }
            emit(ResponseState.Success(true))
        } catch (e: Exception) {
            emit(ResponseState.Error(e.toString()))
        }
    }

    override fun stopSession(
        collection: String,
        addedMembersIds: List<String>
    ): Flow<ResponseState<Boolean>> =
        flow {
            val mCollection = fireStore.collection(collection)
            emit(ResponseState.Loading())
            try {
                mCollection.whereIn("id", addedMembersIds).snapshots().take(1).collectLatest {
                    it.documents.forEach { documentSnapshot ->
                        val dataToBeUpdated = mapOf(
                            Pair(
                                "addedMembers", arrayListOf<AddedUser>()
                            ), Pair("active", false),Pair("host",false)
                        )
                        mCollection.document(documentSnapshot.id).update(dataToBeUpdated).await()
                    }
                }
                emit(ResponseState.Success(true))
            } catch (e: Exception) {
                emit(ResponseState.Error(e.toString()))
            }
        }


}