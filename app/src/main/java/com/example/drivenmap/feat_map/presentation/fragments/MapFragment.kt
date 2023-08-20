package com.example.drivenmap.feat_map.presentation.fragments

import android.Manifest
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drivenmap.R
import com.example.drivenmap.databinding.FragmentMapBinding
import com.example.drivenmap.feat_core.utils.LocationPermissionManager
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LATITUDE
import com.example.drivenmap.feat_core.utils.Utils.CURRENT_LOCATION_LONGITUDE
import com.example.drivenmap.feat_core.utils.Utils.LOCATION_UPDATES
import com.example.drivenmap.feat_core.utils.Utils.MAP_ZOOM
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.example.drivenmap.feat_map.presentation.adapters.AddedMembersAdapter
import com.example.drivenmap.feat_map.presentation.services.TrackingService
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
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
    private lateinit var locationPermissionManager: LocationPermissionManager
    private lateinit var serviceIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater)
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        requireActivity().registerReceiver(locationReceiver, filter)

        serviceIntent = Intent(requireActivity().applicationContext, TrackingService::class.java)
        requireActivity().startService(serviceIntent)

        requireActivity().registerReceiver(updateLocation, IntentFilter(LOCATION_UPDATES))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentMapMvMain.onCreate(savedInstanceState)
        binding.fragmentMapMvMain.getMapAsync {
            map = it
        }
        val backgroundLocationPermissionRequest = requireActivity().registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    Log.d("taget", "background")
                }

                else -> {
                    Log.d("taget", "no permissions")

                }
            }
        }
        locationPermissionManager = LocationPermissionManager(this) {
            setViews()
        }
        locationPermissionManager.getBackgroundLocationPermission(
            backgroundLocationPermissionRequest,
            requireContext()
        )
    }

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
            fragmentMapRvAddedMembers.apply {
                adapter = addedMembersAdapter.apply {
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
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private val updateLocation = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent) {
            currentLocation = LatLng(
                p1.getDoubleExtra(CURRENT_LOCATION_LATITUDE, 0.0),
                p1.getDoubleExtra(CURRENT_LOCATION_LONGITUDE, 0.0)
            )
            map.apply {
                animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_ZOOM))
                addMarker(MarkerOptions().apply {
                    position(currentLocation)
                    icon(BitmapDescriptorFactory.defaultMarker())
                })
            }
        }
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            locationPermissionManager.showDialogIfLocationIsNotEnable()
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
            unregisterReceiver(locationReceiver)
            unregisterReceiver(updateLocation)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.fragmentMapMvMain.onSaveInstanceState(outState)
    }
}