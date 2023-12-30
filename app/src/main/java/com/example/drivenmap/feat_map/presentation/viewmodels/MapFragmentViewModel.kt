package com.example.drivenmap.feat_map.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.domain.usecases.AddMembersUseCase
import com.example.drivenmap.feat_map.domain.usecases.GetUserDataUseCase
import com.example.drivenmap.feat_map.domain.usecases.LocationUpdateToOthersUseCase
import com.example.drivenmap.feat_map.domain.usecases.LocationUpdateUseCase
import com.example.drivenmap.feat_map.domain.usecases.StopSessionUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val getUserDataUc: GetUserDataUseCase,
    private val addMembersUc:AddMembersUseCase,
    private val locationUpdateUc: LocationUpdateUseCase,
    private val locationUpdateToOthersUc: LocationUpdateToOthersUseCase,
    private val stopSessionUseCase: StopSessionUseCase,
    private val mAuth: FirebaseAuth
) : ViewModel() {

    private val _userLiveData = MutableLiveData<ResponseState<UserModel>>()
    val userLiveData: LiveData<ResponseState<UserModel>> = _userLiveData

    private val _addedMembersResult = MutableLiveData<ResponseState<Boolean>>()
    val addedMembersResult : LiveData<ResponseState<Boolean>> = _addedMembersResult

    private val _locationUpdateResult = MutableLiveData<ResponseState<Boolean>>()
    val locationUpdateResult : LiveData<ResponseState<Boolean>> = _locationUpdateResult

    private val _locationUpdatesToOthersResult = MutableLiveData<ResponseState<Boolean>>()
    val locationUpdatesToOthersResult : LiveData<ResponseState<Boolean>> = _locationUpdatesToOthersResult

    private val _stoppingSessionResult = MutableLiveData<ResponseState<Boolean>>()
    val stopSessionResult : LiveData<ResponseState<Boolean>> = _stoppingSessionResult

    val currentId = mAuth.currentUser!!.uid
    var isActive = userLiveData.value?.data?.active?:false
    var isHost = userLiveData.value?.data?.host?:false

    init {
        getUser()
    }

    fun getUser() = viewModelScope.launch() {
        getUserDataUc(
            collectionName = USER_COLLECTION_NAME,
            documentName = currentId
        ).collectLatest {
            _userLiveData.postValue(it)
        }

    }
    fun addMembers(addedMemberIds: List<String>) = viewModelScope.launch(Dispatchers.IO) {
        addMembersUc(USER_COLLECTION_NAME,currentId, addedMemberIds).collectLatest {
            _addedMembersResult.postValue(it)
        }
    }

    fun currentLocationUpdate(location:Location) = viewModelScope.launch(Dispatchers.IO) {
        locationUpdateUc(USER_COLLECTION_NAME,currentId,location).collectLatest {
            _locationUpdateResult.postValue(it)
        }
    }
    fun stopSession(membersIds:List<String>) = viewModelScope.launch(Dispatchers.IO) {
        stopSessionUseCase(USER_COLLECTION_NAME,membersIds).collectLatest {
            _stoppingSessionResult.postValue(it)
        }
    }
    fun locationUpdatesToOther(addedMembers:List<String>,location:Location) = viewModelScope.launch(Dispatchers.IO) {
        locationUpdateToOthersUc(USER_COLLECTION_NAME, addedMembers,currentId,location).collectLatest {
            _locationUpdatesToOthersResult.postValue(it)
        }
    }


}















