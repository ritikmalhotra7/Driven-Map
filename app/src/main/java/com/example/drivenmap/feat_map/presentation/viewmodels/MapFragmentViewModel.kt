package com.example.drivenmap.feat_map.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.usecases.GetUserData
import com.example.drivenmap.feat_map.domain.usecases.SetUserData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val setUserDataUc: SetUserData,
    private val getUserDataUc: GetUserData,
    private val mAuth:FirebaseAuth
) : ViewModel() {

    val mapScreenState = MutableStateFlow(MapScreenState())
    var currentUser = UserModel()

    init {
        mAuth.currentUser?.let{currentUser ->
            onEvent(mapEvent = MapScreenEvents.OnRefresh(currentUser.uid))
        }
    }

    data class MapScreenState(
        val isSessionActive: Boolean? = null,
        val user: UserModel? = null,
        //these 2 props are for every function to use
        val isLoading: Boolean? = null,
        val containsError: String? = null
    )

    sealed class MapScreenEvents {
        data class OnRefresh(val document: String) : MapScreenEvents()
        data class StartSession(val userWhoStartedThisSession: UserModel) : MapScreenEvents()
        data class UpdateAddedMemberList(val user:UserModel, val addedMembers:List<AddedUser>): MapScreenEvents()
        data class UpdateCurrentLocation(val user:UserModel, val currentLocation:Location): MapScreenEvents()
        data class StopTheRunningSession(val userWhoStartedThisSession: UserModel) : MapScreenEvents()
    }

    fun onEvent(mapEvent: MapScreenEvents) {
        when (mapEvent) {
            is MapScreenEvents.OnRefresh -> {
                updateMapScreenStateAccToUserData(mapEvent.document){}
            }

            is MapScreenEvents.StartSession -> {
                startSession(mapEvent.userWhoStartedThisSession)
            }

            is MapScreenEvents.UpdateAddedMemberList -> {
                updateMemberList(mapEvent.user, mapEvent.addedMembers)
            }
            is MapScreenEvents.UpdateCurrentLocation -> {
                updateCurrentLocation(mapEvent.user, mapEvent.currentLocation)
            }
            is MapScreenEvents.StopTheRunningSession -> {
                stopTheSession(mapEvent.userWhoStartedThisSession)
            }
        }
    }
    private fun updateCurrentLocation(user: UserModel, currentLocation: Location) {
        updatingMapScreenStateAfterSettingUserData(user.copy(currentLocation = currentLocation)){
            updateMapScreenStateAccToUserData(user.id.toString()){}
        }
    }

    private fun updateMemberList(user:UserModel, addedMembers: List<AddedUser>) {
        updatingMapScreenStateAfterSettingUserData(user.copy(addedMembers = addedMembers)){
            updateMapScreenStateAccToUserData(user.id.toString()){}
        }
    }

    private fun startSession(user: UserModel) {
        updatingMapScreenStateAfterSettingUserData(user.copy(isActive = true)){
            viewModelScope.launch { mapScreenState.emit(mapScreenState.value.copy(isSessionActive = true))}
            updateMapScreenStateAccToUserData(user.id.toString()){}
        }
    }
    private fun stopTheSession(user:UserModel){
        updatingMapScreenStateAfterSettingUserData(user.copy(isActive = false)){
            viewModelScope.launch { mapScreenState.emit(mapScreenState.value.copy(isSessionActive = false))}
            updateMapScreenStateAccToUserData(user.id.toString()){}
        }
    }

    // function to set
    private fun updatingMapScreenStateAfterSettingUserData(
        data: UserModel,
        onSuccess:(()->Unit)?
    ) = viewModelScope.launch(Dispatchers.IO) {
        setUserDataUc(USER_COLLECTION_NAME, data.id!!, data).collectLatest { userResponseState ->
            when (userResponseState){
                is ResponseState.Success -> {
                    mapScreenState.emit(
                        mapScreenState.value.copy(
                            isLoading = false,
                            containsError = null
                        )
                    )
                    onSuccess?.let{
                        it()
                    }
                }

                is ResponseState.Error -> {
                    mapScreenState.emit(
                        mapScreenState.value.copy(
                            isLoading = false,
                            containsError = userResponseState.message
                        )
                    )
                }

                is ResponseState.Loading -> {
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

    //function to get
    private fun updateMapScreenStateAccToUserData(
        documentName: String,
        onSuccess:(()->Unit)?
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            getUserDataUc(USER_COLLECTION_NAME, documentName).collectLatest { userResponseState ->
                when (userResponseState) {
                    is ResponseState.Success -> {
                        userResponseState.data?.let { user ->
                            mapScreenState.emit(
                                mapScreenState.value.copy(
                                    user = user,
                                    isLoading = false,
                                    containsError = null
                                )
                            )
                            currentUser = user
                            onSuccess?.let{
                                it()
                            }
                        }
                    }

                    is ResponseState.Error -> {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                user = null,
                                isLoading = false,
                                containsError = userResponseState.message
                            )
                        )
                    }

                    is ResponseState.Loading -> {
                        mapScreenState.emit(
                            mapScreenState.value.copy(
                                user = null,
                                isLoading = true,
                                containsError = null
                            )
                        )
                    }
                }
            }
        }
}















