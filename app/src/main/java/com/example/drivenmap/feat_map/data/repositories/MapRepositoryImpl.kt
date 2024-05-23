package com.example.drivenmap.feat_map.data.repositories

import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.logd
import com.example.drivenmap.feat_map.data.dto.GroupDto
import com.example.drivenmap.feat_map.data.dto.LocationDto
import com.example.drivenmap.feat_map.data.dto.UserDto
import com.example.drivenmap.feat_map.data.dto.UserXDto
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore
) : MapRepository {


    /*override fun getUser(
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
        }*/
    override suspend fun createGroup(
        createdBy: String,
        users: List<String>
    ) {
        val mCollection = fireStore.collection("GROUP")
        val mCollectionUser = fireStore.collection("USERS")
        var userDtos = listOf<UserDto?>()
        userDtos = mCollectionUser.whereIn("email", users).get().await()
            .documents.map { it.toObject(UserDto::class.java) }
        userDtos.logd("userIds")
        mCollection.document(createdBy).set(
            GroupDto(
                createdBy,
                userDtos.map { UserXDto(it?.id ?: "", it?.email ?: "", it?.profilePhoto ?: "") },
                Calendar.getInstance().time.toString(),
                createdBy
            )
        ).await()
        userDtos.forEach {
            it?.let {
                updateUserWithGroupData(it, createdBy)
            }
        }
    }

    override suspend fun getGroup(groupId: String): Flow<ResponseState<GroupDto>> = callbackFlow {
        if(groupId.isNullOrBlank()){
            awaitClose()
            return@callbackFlow
        }
        fireStore.collection("GROUP").document(groupId).get().addOnSuccessListener { value ->
            if (!value.exists()) {
                trySend(ResponseState.Error("Something went wrong!"))
            } else {
                value?.let {
                    value.data?.logd("value-group")
                    trySend(ResponseState.Success(value.toObject(GroupDto::class.java)))
                } ?: {
                    trySend(ResponseState.Error("Data doesn't exist!"))
                }
            }
        }
        awaitClose()
    }

    override suspend fun getUser(userId: String): Flow<ResponseState<UserDto>> = callbackFlow {
        fireStore.collection("USERS").document(userId).get().addOnSuccessListener { value ->
            if (!value.exists()) {
                trySend(ResponseState.Error("Something went wrong!"))
            } else {
                value?.let {
                    trySend(ResponseState.Success(value.toObject(UserDto::class.java)))
                } ?: {
                    trySend(ResponseState.Error("Data doesn't exist!"))
                }
            }
        }
        awaitClose()
    }

    override suspend fun updateUserLocationInGroup(
        groupId: String,
        userId: String,
        location: LocationDto
    ) {
        val mCollection = fireStore.collection("GROUP")
        val group = mCollection.document(groupId).get().await().toObject(GroupDto::class.java)
        group?.logd("group")
        group?.users?.let {
            it.logd("user-location")
            mCollection.document(groupId)
                .update("users", FieldValue.arrayRemove(it.firstOrNull { it.id == userId })).await()
            mCollection.document(groupId).update(
                "users",
                FieldValue.arrayUnion(it.firstOrNull { it.id == userId }?.copy(location = location))
            ).await()
        }
    }

    override suspend fun cancelGroup(groupId: String) {
        val mCollection = fireStore.collection("GROUP")
        val mCollectionUsers = fireStore.collection("USERS")
        val group = mCollection.document(groupId).get().await().toObject(GroupDto::class.java)
        val users = group?.users
        mCollectionUsers.whereIn("id", users?.map { it.id } ?: listOf()).get()
            .await().documents.forEach {
                val user = it.toObject(UserDto::class.java)
                val data = mapOf(
                    "groupId" to ""
                )
                mCollectionUsers.document(user?.id?:"").update(data)
        }
    }

    private suspend fun updateUserWithGroupData(
        user: UserDto,
        groupId: String,
    ) {
        val mCollection = fireStore.collection("USERS")
        val data = mapOf(
            "groupId" to groupId,
        )
        mCollection.document(user.id).update(data).await()
    }
}