package com.example.drivenmap.feat_map.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenmap.feat_core.utils.ResponseState
import com.example.drivenmap.feat_core.utils.logd
import com.example.drivenmap.feat_map.data.dto.GroupDto
import com.example.drivenmap.feat_map.data.dto.LocationDto
import com.example.drivenmap.feat_map.data.dto.UserDto
import com.example.drivenmap.feat_map.domain.usecases.CancelGroupUsecase
import com.example.drivenmap.feat_map.domain.usecases.CreateGroupUsecase
import com.example.drivenmap.feat_map.domain.usecases.GetGroupUsecase
import com.example.drivenmap.feat_map.domain.usecases.GetUserUsecase
import com.example.drivenmap.feat_map.domain.usecases.UpdateLocationUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UIStates{
    IS_IN_LOCATION_SHARING,NORMAL
}
@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val createGroupUsecase:CreateGroupUsecase,
    private val getUserUsecase: GetUserUsecase,
    private val getGroupUsecase: GetGroupUsecase,
    private val updateLocationUsecase: UpdateLocationUsecase,
    private val cancelGroupUsecase: CancelGroupUsecase

) : ViewModel() {

    val uiState=MutableStateFlow<UIStates?>(null)

    private val _mCurrentUserState = MutableStateFlow<UserDto?>(null)
    val mCurrentUserState = _mCurrentUserState.asStateFlow()

    private val _mGroupState = MutableSharedFlow<GroupDto?>()
    val mGroupState = _mGroupState.asSharedFlow()

    private val _errorState = MutableSharedFlow<String>()
    val errorState = _errorState.asSharedFlow()

    fun createGroup(hostId:String,userEmails:List<String>) = viewModelScope.launch(Dispatchers.IO){
        createGroupUsecase(hostId,userEmails)
    }

    fun cancelGroup(groupId:String) = viewModelScope.launch(Dispatchers.IO){
        cancelGroupUsecase.invoke(groupId)
    }

    fun getGroup(groupId:String) = viewModelScope.launch(Dispatchers.IO){
        getGroupUsecase(groupId).collectLatest{state->
            state.logd("state")
            when(state){
                is ResponseState.Success->{
                    _mGroupState.emit(state.data)
                }
                is ResponseState.Error->{
                    _errorState.emit(state.message?:"Something went wrong!")
                }
            }
        }
    }
    fun updateLocation(groupId: String,userId:String,location:LocationDto) = viewModelScope.launch(Dispatchers.IO){
        updateLocationUsecase(groupId, userId, location)
    }

    fun getUser(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        getUserUsecase(userId).collectLatest{state->
            state.logd("state")
            when(state){
                is ResponseState.Success->{
                    _mCurrentUserState.update{
                        state.data
                    }
                    if(!state.data?.groupId.isNullOrBlank()){
                        uiState.update { UIStates.IS_IN_LOCATION_SHARING }
                    }else{
                        uiState.update { UIStates.NORMAL }
                    }
                }
                is ResponseState.Error->{
                    _errorState.emit(state.message?:"Something went wrong!")
                }
            }
        }
    }
}















