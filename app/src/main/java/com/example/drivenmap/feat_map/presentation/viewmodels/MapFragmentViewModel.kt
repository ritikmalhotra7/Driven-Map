package com.example.drivenmap.feat_map.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.usecases.GetUserData
import com.example.drivenmap.feat_map.domain.usecases.SetUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.type.DateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val setUserDataUc: SetUserData,
    private val getUserDataUc: GetUserData,
    @ApplicationContext ctx: Context,
    private val mAuth:FirebaseAuth
) : ViewModel() {

    val mapScreenState = MutableStateFlow(MapScreenState())

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
        data class StopTheRunningSession(val userWhoStartedThisSession: UserModel) : MapScreenEvents()
    }

    private fun onEvent(mapEvent: MapScreenEvents) {
        when (mapEvent) {
            is MapScreenEvents.OnRefresh -> {
                updateUIAccToUserData(mapEvent.document)
            }

            is MapScreenEvents.StartSession -> {
                startSession(mapEvent.userWhoStartedThisSession)
            }

            is MapScreenEvents.StopTheRunningSession -> {
                stopTheSession(mapEvent.userWhoStartedThisSession)
            }
        }
    }

    private fun startSession(user: UserModel) {
        setUserData(user.id.toString(),user.copy(isActive = true, activeTimeStarted = LocalDateTime.now()))
        updateUIAccToUserData(user.id.toString())
    }
    private fun stopTheSession(user:UserModel){
        setUserData(user.id.toString(),user.copy(isActive = false, activeTimeStarted = null))
        updateUIAccToUserData(user.id.toString())
    }

    private fun setUserData(
        documentName: String,
        data: UserModel
    ) = viewModelScope.launch(Dispatchers.IO) {
        setUserDataUc(USER_COLLECTION_NAME, documentName, data).collectLatest { userResponseState ->
            when (userResponseState) {
                is ResponseState.Success -> {
                    mapScreenState.emit(
                        mapScreenState.value.copy(
                            isLoading = false,
                            containsError = null
                        )
                    )
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

    private fun updateUIAccToUserData(
        documentName: String,
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















