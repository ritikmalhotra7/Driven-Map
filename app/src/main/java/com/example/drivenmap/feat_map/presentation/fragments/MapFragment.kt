package com.example.drivenmap.feat_map.presentation.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drivenmap.R
import com.example.drivenmap.databinding.FragmentMapBinding
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.presentation.adapters.AddedMembersAdapter
import com.example.drivenmap.feat_map.presentation.services.TrackingService
import com.example.drivenmap.feat_map.utils.LocationPermissionManager
import com.example.drivenmap.feat_map.utils.Utils
import com.example.drivenmap.feat_map.utils.Utils.CURRENT_LOCATION_LATITUDE
import com.example.drivenmap.feat_map.utils.Utils.CURRENT_LOCATION_LONGITUDE
import com.example.drivenmap.feat_map.utils.Utils.LOCATION_UPDATES
import com.example.drivenmap.feat_map.utils.Utils.MAP_ZOOM
import com.example.drivenmap.feat_map.utils.Utils.REQUEST_CODE_LOCATION_PERMISSION
import com.example.drivenmap.feat_map.utils.Utils.hasLocationPermissions
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding by lazy {
        _binding!!
    }
    @Inject
    lateinit var fireStore: FirebaseFirestore

    private val apiKey = "AIzaSyDPD7o85GIZjzgYhRPp_G-YMVXoseISB9U"
    private lateinit var addedMembersAdapter: AddedMembersAdapter
    private lateinit var map: GoogleMap
    private lateinit var currentLocation: LatLng
    private lateinit var locationPermissionManager:LocationPermissionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

   /* override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
           setViews()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && EasyPermissions.somePermissionPermanentlyDenied(
                this,
                perms
            )
        ) {
            requestPermissions()
        }
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentMapMvMain.onCreate(savedInstanceState)
        binding.fragmentMapMvMain.getMapAsync {
            map = it
        }
        locationPermissionManager = LocationPermissionManager(this){
            setViews()
        }
    /*if(hasLocationPermissions(requireContext())){
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingIntent)
            }
            setViews()
        }else{

        }*/

    }
    /*private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            requireActivity(),
            "You need to accept location permissions",
            Utils.REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }*/

    private fun setViews() {
        Places.initialize(requireContext().applicationContext, apiKey)
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
        })
        binding.apply {
            addedMembersAdapter = AddedMembersAdapter()
            fragmentMapRvAddedMembers.adapter = addedMembersAdapter.apply {
                setData(
                    listOf(
                        AddedUser(
                            id = UUID.randomUUID().toString(),
                            name = "xyz1",
                            distanceAway = "1.2KM"
                        ),
                        AddedUser(
                            id = UUID.randomUUID().toString(),
                            name = "xyz2",
                            distanceAway = "1.0KM"
                        ),
                        AddedUser(
                            id = UUID.randomUUID().toString(),
                            name = "xyz4",
                            distanceAway = "1.6KM"
                        )
                    )
                )
            }
            fragmentMapRvAddedMembers.layoutManager = LinearLayoutManager(requireContext())
            requireActivity().registerReceiver(updateLocation, IntentFilter(LOCATION_UPDATES))
            val intent = Intent(requireContext(),TrackingService::class.java)
            requireActivity().startService(intent)
        }

    }

    private val updateLocation = object:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent) {
            currentLocation = LatLng(p1.getDoubleExtra(CURRENT_LOCATION_LATITUDE,0.0),p1.getDoubleExtra(CURRENT_LOCATION_LONGITUDE,0.0))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_ZOOM))
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.fragmentMapMvMain.onSaveInstanceState(outState)
    }
}