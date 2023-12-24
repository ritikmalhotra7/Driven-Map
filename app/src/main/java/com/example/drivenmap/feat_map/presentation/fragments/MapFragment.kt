package com.example.drivenmap.feat_map.presentation.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.drivenmap.MainActivity
import com.example.drivenmap.R
import com.example.drivenmap.databinding.AddMembersBottomSheetGeneratedQrBinding
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
import com.example.drivenmap.feat_map.domain.models.Location
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.example.drivenmap.feat_map.presentation.adapters.AddMemberBottomSheetAdapter
import com.example.drivenmap.feat_map.presentation.adapters.AddedMembersAdapter
import com.example.drivenmap.feat_map.presentation.services.TrackingService
import com.example.drivenmap.feat_map.presentation.viewmodels.MapFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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

    private val viewModel: MapFragmentViewModel by viewModels()

    private lateinit var addedMembersAdapter: AddedMembersAdapter

    private lateinit var map: GoogleMap

    private var currentLocation: Location? = null
    private lateinit var serviceIntent: Intent
    private lateinit var permissionManager: PermissionManager
    private var codeScanner: CodeScanner? = null
    private var marker: Marker? = null
    private var currentUser: UserModel? = null

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
        addedMembersAdapter = AddedMembersAdapter()
        binding.apply {
            fragmentMapRvAddedMembers.apply {
                adapter = addedMembersAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        lifecycleScope.launch {
            viewModel.mapScreenState.collectLatest { state ->
                val isSessionActive = state.isSessionActive
                val user = state.currentUser
                val isLoading = state.isLoading
                val containsError = state.containsError

                isLoading?.let {
                    if (it) {
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }
                containsError?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).apply {
                        animationMode = Snackbar.ANIMATION_MODE_SLIDE
                    }.show()
                }
                isSessionActive?.let {
                    Log.d("taget-active",it.toString())
                    Log.d("taget-active",user.toString())
                    if (it) {
                        binding.fragmentMapBtStartSession.text =
                            getString(R.string.stop_this_session)
                        user?.let {
                            updateRecyclerViewAdapter(user.addedMembers.distinctBy { it.id })
                        }
                    } else {
                        binding.fragmentMapBtStartSession.text = getString(R.string.start_a_session)
                        updateRecyclerViewAdapter(listOf())
                    }
                }
                user?.let {
                    currentUser = user
                    binding.fragmentMapBtStartSession.setOnClickListener {
                        openBottomSheetForAddingMembers()
                    }
                    binding.fragmentMapBtQr.setOnClickListener {
                        openBottomSheetForGeneratedQR()
                    }
                }
            }
        }
    }

    private fun updateRecyclerViewAdapter(addedUserList: List<AddedUser>?) {
        addedMembersAdapter.setData(
            addedUserList ?: listOf()
        )
        addedMembersAdapter.setClickListener { addedUser ->
            addedUser.currentLocation?.let { currentLocation ->
                updateMarker(currentLocation, addedUser)
            }
        }
    }

    private fun updateMarker(
        currentLocation: Location,
        addedUser: AddedUser
    ) {
        val latLng = LatLng(currentLocation.latitude!!, currentLocation.longitude!!)
        map.apply {
            marker?.remove()
            marker = map.addMarker(MarkerOptions().apply {
                position(latLng)
                title(addedUser.name)
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

    private fun openBottomSheetForGeneratedQR() {
        val bottomSheet = BottomSheetDialog(requireContext())
        /*bottomSheet.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED*/
        val binding =
            AddMembersBottomSheetGeneratedQrBinding.inflate(LayoutInflater.from(requireContext()))
        mAuth.currentUser?.uid?.let { uid ->
            val qrgEncoder = QRGEncoder(uid, null, QRGContents.Type.TEXT, 1000).apply {
                colorBlack = Color.WHITE
                colorWhite = Color.BLACK
            }
            binding.addMembersBottomSheetGeneratedQrIvQr.load(qrgEncoder.bitmap) {
                transformations(RoundedCornersTransformation(32f, 32f, 32f, 32f))
            }
        }
        bottomSheet.setContentView(binding.root)
        if (!bottomSheet.isShowing) {
            bottomSheet.show()
        }
    }

    private fun openBottomSheetForAddingMembers() {
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val binding =
            AddMembersBottomSheetLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val addMemberAdapter = AddMemberBottomSheetAdapter()
        var addedMemberList = arrayListOf<AddedUser>()
        var jobForCollectingData : Job? = null
        if (codeScanner == null) {
            codeScanner =
                CodeScanner(requireContext(), binding.addMembersBottomSheetLayoutCsv).apply {
                    camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                    formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
                    autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                    scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                    isAutoFocusEnabled = true // Whether to enable auto focus or not
                    isFlashEnabled = false // Whether to enable flash or not
                }
            viewModel.onEvent(MapFragmentViewModel.MapScreenEvents.StartSession)
        }
        binding.apply {
            codeScanner?.startPreview()
            addMembersBottomSheetLayoutCsv.setOnClickListener {
                codeScanner?.startPreview()
            }
            codeScanner?.decodeCallback = DecodeCallback { result ->
                val code = result.toString()
                viewModel.onEvent(MapFragmentViewModel.MapScreenEvents.AddMemberToSession(code))
                jobForCollectingData = lifecycleScope.launch {
                    viewModel.mapScreenState.collectLatest { state ->
                        val isSessionActive = state.isSessionActive
                        val user = state.currentUser
                        val isLoading = state.isLoading
                        val containsError = state.containsError

                        isLoading?.let {
                            if (it) {
                                showProgress()
                            } else {
                                hideProgress()
                            }
                        }
                        containsError?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).apply {
                                animationMode = Snackbar.ANIMATION_MODE_SLIDE
                            }.show()
                        }

                        user?.addedMembers?.let {
                            Log.d("taget",user.toString())
                            addedMemberList = it
                            addMemberAdapter.apply {
                                setData(addedMemberList)
                            }
                        }
                    }
                }
            }
            codeScanner?.errorCallback = ErrorCallback {
                runBlocking {
                    Toast.makeText(
                        requireContext(), "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            addMembersBottomSheetLayoutRvMembers.apply {
                adapter = addMemberAdapter
                layoutManager =
                    GridLayoutManager(this.context, 2, GridLayoutManager.HORIZONTAL, false)
            }
            addMembersBottomSheetLayoutTvDone.setOnClickListener {
                bottomSheet.dismiss()
                jobForCollectingData?.cancel()
            }

        }
        bottomSheet.setOnDismissListener {
            codeScanner?.releaseResources()
        }
        bottomSheet.setContentView(binding.root)
        if (!bottomSheet.isShowing) {
            bottomSheet.show()
        }
    }

    private fun userDoc(document: String): DocumentReference {
        return fireStore.collection(USER_COLLECTION_NAME).document(document)
    }

    private val updateLocation = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, p1: Intent) {
            val newLocation = LatLng(
                p1.getDoubleExtra(CURRENT_LOCATION_LATITUDE, 0.0),
                p1.getDoubleExtra(CURRENT_LOCATION_LONGITUDE, 0.0)
            )
            map.apply {
                if (currentLocation == null) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, MAP_ZOOM))
                }
                isMyLocationEnabled = true
            }
            //update the current location in firestore
            val tempCurrentLoc =
                Location(latitude = newLocation.latitude, longitude = newLocation.longitude)
            /*currentUser?.let{
                viewModel.onEvent(MapFragmentViewModel.MapScreenEvents.UpdateCurrentLocation(it,tempCurrentLoc))
            }*/
            userDoc(mAuth.currentUser!!.uid).update("currentLocation", tempCurrentLoc)
            currentLocation = tempCurrentLoc

            //updating every added member from firestore
            val addedMembers = addedMembersAdapter.getList().toMutableList()
            val updatedAddedMembers = addedMembers
            addedMembers.forEach { addedUser ->
                userDoc(addedUser.id!!).get().addOnSuccessListener {
                    val user = it.toObject(UserModel::class.java)
                    updatedAddedMembers.remove(addedUser)
                    updatedAddedMembers.add(user!!.toAddedUser())
                }
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