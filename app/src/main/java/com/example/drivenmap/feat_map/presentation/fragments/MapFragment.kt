package com.example.drivenmap.feat_map.presentation.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drivenmap.MainActivity
import com.example.drivenmap.R
import com.example.drivenmap.databinding.FragmentMapBinding
import com.example.drivenmap.feat_core.utils.PermissionManager
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LATITUDE
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LONGITUDE
import com.example.drivenmap.feat_core.utils.Utils.LOCATION_UPDATES
import com.example.drivenmap.feat_core.utils.Utils.MAP_ZOOM
import com.example.drivenmap.feat_map.data.dto.LocationDto
import com.example.drivenmap.feat_map.data.dto.UserDto
import com.example.drivenmap.feat_map.data.dto.UserXDto
import com.example.drivenmap.feat_map.presentation.adapters.AddedMembersAdapter
import com.example.drivenmap.feat_map.presentation.services.TrackingService
import com.example.drivenmap.feat_map.presentation.viewmodels.MapFragmentViewModel
import com.example.drivenmap.feat_map.presentation.viewmodels.UIStates
import com.example.drivenmap.feat_map.utils.Constants
import com.example.drivenmap.feat_map.utils.Constants.ADDED_MEMBER_DETAILS_ID_KEY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding by lazy {
        _binding!!
    }

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var fireStore: FirebaseFirestore

    private var placeName = ""

    private val viewModel: MapFragmentViewModel by viewModels()

    private lateinit var addedMembersAdapter: AddedMembersAdapter

    private lateinit var map: GoogleMap

    private var currentUser :UserDto? = null

    private lateinit var serviceIntent: Intent
    private lateinit var permissionManager: PermissionManager
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater)
        serviceIntent = Intent(requireActivity().applicationContext, TrackingService::class.java)
        activity?.startService(serviceIntent)
        activity?.registerReceiver(updateLocation, IntentFilter(LOCATION_UPDATES))
        activity?.registerReceiver(
            locationReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mvMain.onCreate(savedInstanceState)
        binding.mvMain.getMapAsync {
            map = it
        }
        if (mAuth.currentUser == null) {
            findNavController().popBackStack(R.id.loginFragment, true)
            findNavController().navigate(R.id.loginFragment)
        }
        val foregroundPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    setViews()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("taget", "coarse")
                }

                else -> {
                    Log.d("taget", "no permissions")
                }
            }
        }
        val backgroundPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("taget", "background-location")
            } else {
                Log.d("taget", "no permissions")
            }
        }
        permissionManager =
            PermissionManager(this, foregroundPermLauncher, backgroundPermLauncher) {
                setViews()
            }
    }

    private fun setViews() {
        addedMembersAdapter = AddedMembersAdapter().apply {
            setClickListener { addedUser ->
                findNavController().navigate(R.id.action_mapFragment_to_addedMemberBottomSheetFragment,Bundle().apply {
                    putString(ADDED_MEMBER_DETAILS_ID_KEY,addedUser.id)
                })
                addedUser.location?.let { currentLocation ->
                    updateMarker(currentLocation)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mCurrentUserState.collectLatest {
                        it?.let { user ->
                            binding.apply {
                                val isInAnotherGroup = !user.groupId.isNullOrEmpty()
                                if (isInAnotherGroup) {
                                    ibAdd.visibility = View.GONE
                                    ibCancel.visibility = View.VISIBLE
                                } else {
                                    ibAdd.visibility = View.VISIBLE
                                    ibCancel.visibility = View.GONE
                                }
                                currentUser = user
                            }
                        }
                    }
                }
                launch {
                    viewModel.mGroupState.collectLatest {
                        it?.let { group ->
                            binding.ibCancel.setOnClickListener {
                                viewModel.cancelGroup(group.id)
                            }
                            updateRecyclerViewAdapter(group.users)
                        }
                    }
                }
            }
        }
        binding.apply {
            tvEmail.text = "\uD83D\uDC4B Hello \n${mAuth.currentUser?.email?:""}"
            setFragmentResultListener(Constants.ADD_MEMBER_ID_FRAGMENT_RESULT_KEY) { k, b ->
                val ids = ArrayList(
                    b.getStringArrayList(Constants.ADD_MEMBER_IDs_KEY)?.toList() ?: listOf<String>()
                )
                val currentUserId = mAuth.currentUser?.uid ?: ""
                viewModel.createGroup(
                    currentUserId,
                    ids.apply { add(mAuth.currentUser?.email ?: "") })
            }
            ibAdd.setOnClickListener {
                findNavController().navigate(R.id.action_mapFragment_to_addMemberDialogFragment)
            }
            rvAddedMembers.apply {
                adapter = addedMembersAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun updateRecyclerViewAdapter(addedUserList: List<UserXDto>?) {
        addedMembersAdapter.setData(
            addedUserList?: listOf()
        )
    }

    private fun updateMarker(
        currentLocation: LocationDto,
    ) {
        val latLng = LatLng(currentLocation.lattitude!!, currentLocation.longitude!!)
        map.apply {
            marker?.remove()
            marker = map.addMarker(MarkerOptions().apply {
                position(latLng)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            })
            animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng,
                    MAP_ZOOM
                )
            )
        }
    }

    private val updateLocation = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, p1: Intent) {
            val newLocation = LatLng(
                p1.getDoubleExtra(CURRENT_LOCATION_LATITUDE, 0.0),
                p1.getDoubleExtra(CURRENT_LOCATION_LONGITUDE, 0.0)
            )
            val userId = mAuth.currentUser?.uid
            viewModel.getCurrentUser(userId ?: "")
            placeName = Constants.getLocationName(
                requireContext(),
                newLocation.latitude,
                newLocation.longitude
            )?:""
            if (currentUser != null && viewModel.uiState.value == UIStates.IS_IN_LOCATION_SHARING){
                viewModel.updateLocation(
                    currentUser?.groupId ?: "",
                    currentUser?.id ?: "",
                    LocationDto(lattitude = newLocation.latitude, newLocation.longitude, placeName)
                )
                viewModel.getGroup(currentUser?.groupId ?: "")
            }else{
            }
            map.apply {
                isMyLocationEnabled = true
            }
        }
    }
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            permissionManager.showDialogIfLocationIsNotEnable(this@MapFragment)
        }
    }

    private fun showProgress() {
        (requireActivity() as MainActivity).showProgressBar()
    }

    private fun hideProgress() {
        (requireActivity() as MainActivity).hideProgressBar()
    }

    override fun onResume() {
        super.onResume()
        binding.mvMain.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mvMain.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mvMain.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mvMain.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mvMain.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        binding.mvMain.onDestroy()
        requireActivity().stopService(serviceIntent)
        requireActivity().apply {
            unregisterReceiver(updateLocation)
            unregisterReceiver(locationReceiver)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mvMain.onSaveInstanceState(outState)
    }
}