package com.example.drivenmap.feat_map.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.data.mappers.toAddedUser
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.usecases.GetDataFromFireStore
import com.example.drivenmap.feat_map.domain.usecases.SetDataToFireStore
import com.example.drivenmap.feat_map.domain.usecases.UpdateDataToFireStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val setDataUc: SetDataToFireStore,
    private val getDataUc: GetDataFromFireStore,
    private val updateDataUc: UpdateDataToFireStore,
    private val mAuth: FirebaseAuth
) : ViewModel() {

    val mapScreenState = MutableStateFlow(MapScreenState())

    init {
        mAuth.currentUser?.let { currentUser ->
            onEvent(mapEvent = MapScreenEvents.OnRefresh(currentUser.uid))
        }
    }

    data class MapScreenState(
        val isSessionActive: Boolean? = null,
        val currentUser: UserModel? = null,
        //these 2 props are for every function to use
        val isLoading: Boolean? = null,
        val containsError: String? = null
    )

    sealed class MapScreenEvents {
        data class OnRefresh(val document: String) : MapScreenEvents()
        object StartSession : MapScreenEvents()
        data class AddMemberToSession(val addedMember: String) :
            MapScreenEvents()

        data class UpdateCurrentLocation(val user: UserModel, val currentLocation: Location) :
            MapScreenEvents()

        data class StopTheRunningSession(val userWhoStartedThisSession: UserModel) :
            MapScreenEvents()
    }

    fun onEvent(mapEvent: MapScreenEvents) {
        when (mapEvent) {
            is MapScreenEvents.OnRefresh -> {
                getData(
                    USER_COLLECTION_NAME,
                    mapEvent.document,
                    onSuccess = { user ->
                        viewModelScope.launch {
                            Log.d("taget", user.toString())
                            mapScreenState.emit(
                                mapScreenState.value.copy(
                                    currentUser = user as UserModel,
                                    isLoading = false,
                                    containsError = null,
                                    isSessionActive = user.isActive
                                )
                            )
                        }
                    }
                )
            }

            is MapScreenEvents.StartSession -> {
                //start a session is not working look into it at 12 dec 2023
                startSession()
                onEvent(MapScreenEvents.OnRefresh(mAuth.currentUser!!.uid))
            }

            is MapScreenEvents.AddMemberToSession -> {
                onEvent(MapScreenEvents.OnRefresh(mAuth.currentUser!!.uid))
                memberAdded(mapEvent.addedMember)
            }

            is MapScreenEvents.UpdateCurrentLocation -> {
            }

            is MapScreenEvents.StopTheRunningSession -> {

            }
        }
    }

    //1.updating data of current user to be active
    private fun startSession() = viewModelScope.launch(Dispatchers.IO) {
        updateData(
            collectionName = USER_COLLECTION_NAME,
            documentName = mAuth.currentUser!!.uid,
            dataToBeSaved = mapOf(
                Pair(
                    "isActive",
                    true
                )
            )
        ) {}
    }

    //1.get data of the member we want to add
    //2.update users data to have current user as there added member
    //3.update currentUser data to have added member in there data
    private fun memberAdded(id: String) = viewModelScope.launch(Dispatchers.IO) {
        getData(
            collectionName = USER_COLLECTION_NAME,
            documentName = id,
            onSuccess = { data ->
                val userData = data as UserModel
                if (!userData.isActive) {
                    val currentUser = mapScreenState.value.currentUser
                    viewModelScope.launch(Dispatchers.IO) {
                        val addedMembers: ArrayList<AddedUser> =
                            if (currentUser!!.addedMembers.contains(userData.toAddedUser())) currentUser.addedMembers else currentUser.addedMembers.apply {
                                add(userData.toAddedUser())
                            }
                        updateData(
                            collectionName = USER_COLLECTION_NAME,
                            documentName = userData.id.toString(),
                            dataToBeSaved = mapOf(
                                Pair(
                                    UserModel::class.java.getDeclaredField("addedMembers").name,
                                    userData.addedMembers.apply {  if(!contains(currentUser.toAddedUser())) add(currentUser.toAddedUser()) }),
                                Pair(UserModel::class.java.getDeclaredField("isActive").name, true)
                            ),
                            onSuccess = {
                                updateData(
                                    collectionName = USER_COLLECTION_NAME,
                                    documentName = currentUser.id.toString(),
                                    dataToBeSaved = mapOf(
                                        Pair(
                                            "addedMembers",
                                            currentUser.addedMembers.apply { if(!contains(userData.toAddedUser())) add(userData.toAddedUser()) })
                                    ),
                                    onSuccess = {
                                        viewModelScope.launch {
                                            mapScreenState.emit(
                                                mapScreenState.value.copy(
                                                    currentUser = currentUser.copy(addedMembers = addedMembers),
                                                    isLoading = false,
                                                    containsError = null,
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        )

                    }
                } else {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = false,
                                containsError = "User is in another Session",
                            )
                        )
                    }
                }
            }
        )
    }

    private fun setData(
        collectionName: String,
        documentName: String,
        dataToBeSaved: Any,
        onSuccess: ((Boolean?) -> Unit)?
    ) = viewModelScope.launch(Dispatchers.IO) {
        setDataUc(collectionName, documentName, dataToBeSaved).collectLatest { data ->
            when (data) {
                is ResponseState.Success -> {
                    onSuccess?.let { it(data.data) }
                }

                is ResponseState.Error -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = false,
                                containsError = data.message
                            )
                        )
                        Log.d("taget-error",data.message.toString())
                    }
                }

                is ResponseState.Loading -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = true,
                                containsError = null
                            )
                        )
                    }
                }
            }
        }
    }

    private fun updateData(
        collectionName: String,
        documentName: String,
        dataToBeSaved: Map<String, Any>,
        onSuccess: ((Boolean?) -> Unit)?
    ) = viewModelScope.launch(Dispatchers.IO) {
        updateDataUc(collectionName, documentName, dataToBeSaved).collectLatest { data ->
            when (data) {
                is ResponseState.Success -> {
                    onSuccess?.let { it(data.data) }
                }

                is ResponseState.Error -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = false,
                                containsError = data.message
                            )
                        )
                        Log.d("taget-error",data.message.toString())
                    }
                }

                is ResponseState.Loading -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = true,
                                containsError = null
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getData(
        collectionName: String,
        documentName: String,
        onSuccess: ((Any?) -> Unit)?
    ) = viewModelScope.launch(Dispatchers.IO) {
        getDataUc(collectionName, documentName).collectLatest { data ->
            when (data) {
                is ResponseState.Success -> {
                    Log.d("taget", data.toString())
                    onSuccess?.let { it(data.data) }
                }

                is ResponseState.Error -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = false,
                                containsError = data.message
                            )
                        )
                    }
                }

                is ResponseState.Loading -> {
                    viewModelScope.launch {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                isLoading = true,
                                containsError = null
                            )
                        )
                    }
                }
            }
        }
    }
}















