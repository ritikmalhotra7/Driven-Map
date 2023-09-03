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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drivenmap.R
import com.example.drivenmap.databinding.AddMembersBottomSheetLayoutBinding
import com.example.drivenmap.databinding.FragmentMapBinding
import com.example.drivenmap.feat_core.utils.PermissionManager
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LATITUDE
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LONGITUDE
import com.example.drivenmap.feat_core.utils.Utils.LOCATION_UPDATES
import com.example.drivenmap.feat_core.utils.Utils.MAP_ZOOM
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.data.mappers.toAddedUser
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.presentation.adapters.AddMemberBottomSheetAdapter
import com.example.drivenmap.feat_map.presentation.adapters.AddedMembersAdapter
import com.example.drivenmap.feat_map.presentation.services.TrackingService
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.AndroidEntryPoint
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


    private lateinit var addedMembersAdapter: AddedMembersAdapter
    private lateinit var map: GoogleMap
    private var currentLocation: LatLng? = null
    private lateinit var serviceIntent: Intent
    private lateinit var permissionManager: PermissionManager

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
        binding.fragmentMapMvMain.onCreate(savedInstanceState)
        binding.fragmentMapMvMain.getMapAsync {
            map = it
        }
        if(mAuth.currentUser == null){
            findNavController().popBackStack(R.id.loginFragment,true)
            findNavController().navigate(R.id.loginFragment)
        }
        val foregroundPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("taget", "fine-location")
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
        permissionManager = PermissionManager(this,foregroundPermLauncher,backgroundPermLauncher){
            setViews()
        }
    }

    private fun setViews() {
        addedMembersAdapter = AddedMembersAdapter()
        /*Places.initialize(requireContext().applicationContext, apiKey)
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
            }

            override fun onError(status: Status) {
            }
        })*/

        binding.apply {
            fragmentMapRvAddedMembers.apply {
                adapter = addedMembersAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            updateViews()
            fragmentMapBtStartSession.setOnClickListener {
                openBottomSheetForAddingMembers()
            }
        }
    }

    private fun updateViews() {
        fireStore.collection(USER_COLLECTION_NAME).document(mAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                val user = it!!.toObject(UserModel::class.java)
                Log.d("taget", user.toString())
                Log.d("taget", mAuth.currentUser!!.uid)
                val addedUserList = user?.addedMembers
                updateRecyclerViewAdapter(addedUserList)
            }.addOnFailureListener {
            Log.e("taget", it.toString())
        }
    }

    private fun updateRecyclerViewAdapter(addedUserList: List<AddedUser>?) {
        addedMembersAdapter.setData(
            addedUserList ?: listOf()
        )
        addedMembersAdapter.setClickListener { addedUser ->
            addedUser.currentLocation?.let { currentLocation ->
                map.apply {
                    animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLocation,
                            MAP_ZOOM
                        )
                    )
                }

            }
        }
    }

    private fun openBottomSheetForAddingMembers() {
        val bottomSheet = BottomSheetDialog(requireContext())
        /*bottomSheet.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED*/
        val binding =
            AddMembersBottomSheetLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val addMemberAdapter = AddMemberBottomSheetAdapter()
        val addedMemberList = arrayListOf<AddedUser>()
        binding.apply {
            addMembersBottomSheetLayoutRvMembers.apply {
                adapter = addMemberAdapter
                layoutManager = GridLayoutManager(this.context,2,GridLayoutManager.HORIZONTAL,false)
            }
            addMembersBottomSheetLayoutFabAddMember.setOnClickListener {
                val code = addMembersBottomSheetLayoutTeitCode.text.toString()
                fireStore.collection(USER_COLLECTION_NAME).document(code).get().addOnSuccessListener {
                    val user = it.toObject(UserModel::class.java)
                    addedMemberList.add(user!!.toAddedUser())
                    addMemberAdapter.apply {
                        setData(addedMemberList)
                    }
                    addMembersBottomSheetLayoutTeitCode.setText("")
                }.addOnFailureListener {
                    Log.e("taget-error", it.toString())
                }
            }
            addMembersBottomSheetLayoutTvDone.setOnClickListener {
                fireStore.collection(USER_COLLECTION_NAME).document(mAuth.currentUser!!.uid).get()
                    .addOnSuccessListener {
                        val user = it.toObject<UserModel>()
                        Log.d("taget", user.toString())
                        fireStore.collection(USER_COLLECTION_NAME).document(mAuth.currentUser!!.uid)
                            .set(user!!.copy(addedMembers = addedMemberList)).addOnSuccessListener {
                            Log.d("taget", "in success")
                            updateViews()
                        }
                    }
                bottomSheet.dismiss()
            }
        }
        bottomSheet.setContentView(binding.root)
        if(!bottomSheet.isShowing){
            bottomSheet.show()
        }
    }

    private val updateLocation = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, p1: Intent) {
            val newLocation = LatLng(
                p1.getDoubleExtra(CURRENT_LOCATION_LATITUDE, 0.0),
                p1.getDoubleExtra(CURRENT_LOCATION_LONGITUDE, 0.0)
            )
            map.apply {
                if(currentLocation == null ){
                    animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, MAP_ZOOM))
                }
                isMyLocationEnabled = true
            }
            currentLocation = newLocation
        }
    }
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            permissionManager.showDialogIfLocationIsNotEnable(this@MapFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.fragmentMapMvMain.onResume()
    }
    override fun onStart() {
        super.onStart()
        binding.fragmentMapMvMain.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.fragmentMapMvMain.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.fragmentMapMvMain.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.fragmentMapMvMain.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        binding.fragmentMapMvMain.onDestroy()
        requireActivity().stopService(serviceIntent)
        requireActivity().apply {
            unregisterReceiver(updateLocation)
            unregisterReceiver(locationReceiver)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.fragmentMapMvMain.onSaveInstanceState(outState)
    }
}