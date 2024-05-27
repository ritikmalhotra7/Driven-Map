package com.example.drivenmap.feat_map.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import coil.transform.CircleCropTransformation
import com.example.drivenmap.databinding.FragmentAddedMemberBottomSheetBinding
import com.example.drivenmap.feat_core.utils.logd
import com.example.drivenmap.feat_map.presentation.viewmodels.MapFragmentViewModel
import com.example.drivenmap.feat_map.utils.Constants.ADDED_MEMBER_DETAILS_ID_KEY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddedMemberBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddedMemberBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel:MapFragmentViewModel by viewModels()

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddedMemberBottomSheetBinding.inflate(inflater)
        userId = requireArguments().getString(ADDED_MEMBER_DETAILS_ID_KEY,"")
        userId.logd("userID")
        viewModel.getUser(userId)
        setViews()
        return binding.root
    }

    private fun setViews() {
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    launch{
                        viewModel.mUserState.collectLatest {
                            it?.let{user->
                                user.logd("user")
                                viewModel.getGroup(user.groupId?:"")

                                tvName.text = user.userName
                                tvEmail.text = user.email
                                tvPhoneNumber.text = user.phoneNumber
                                ivProfile.load(user.profilePhoto){
                                    transformations(CircleCropTransformation())
                                }
                            }
                        }
                    }
                    launch{
                        viewModel.mGroupState.collectLatest {
                            (it?:"").logd("groupNull")
                            it?.let{group->
                                group.logd("group")
                                group.users.firstOrNull { it.id == userId }?.let{
                                    tvLatitude.text = it.location?.lattitude.toString()
                                    tvLongitude.text = it.location?.longitude.toString()
                                    tvPlaceName.text = it.location?.placeName.toString()
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}