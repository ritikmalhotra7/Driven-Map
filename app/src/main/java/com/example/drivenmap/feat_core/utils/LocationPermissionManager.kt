package com.example.drivenmap.feat_core.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class LocationPermissionManager(
    private val fragment: Fragment,
    private val onPermissionGranted: () -> Unit
) {
    private lateinit var builder: AlertDialog.Builder
    private var dialog: AlertDialog? = null

    init {
        getForegroundLocationPermissions(fragment.requireContext())
        showDialogIfLocationIsNotEnable()
    }

    private fun getForegroundLocationPermissions(context: Context) {
        val locationPermissionRequest = fragment.requireActivity().registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    onPermissionGranted()
                    Log.d("taget", "fine-location")
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    getForegroundLocationPermissions(context)
                    Log.d("taget", "coarse")
                }

                else -> {
                    getForegroundLocationPermissions(context)
                    Log.d("taget", "no permissions")

                }
            }
        }
        Log.d(
            "taget", "${
                !checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                        !checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            }"
        )
        if (!checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            !checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            onPermissionGranted()
        }
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun getBackgroundLocationPermission(
        backgroundLocationPermissionRequest: ActivityResultLauncher<Array<String>>,
        context: Context
    ) {
        if (checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            backgroundLocationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    fun showDialogIfLocationIsNotEnable() {
        val locationManager =
            fragment.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isLocationEnabled ) {
            if(dialog == null){
                // Show a dialog to ask the user to turn on location
                builder = AlertDialog.Builder(fragment.requireContext()).apply {
                    setTitle("Turn on Location")
                    setMessage("This app needs location to work properly. Please turn on location.")
                    setPositiveButton(
                        "Turn on Location",
                        DialogInterface.OnClickListener { dialog, which ->
                            // Start the location settings activity
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            fragment.requireActivity().startActivity(intent)
                            this@LocationPermissionManager.dialog = null
                        })
                    setNegativeButton("Cancel"){dialog,int ->
                        dialog.cancel()
                        this@LocationPermissionManager.dialog = null
                    }
                }
                dialog = builder.show()
            }
        }
    }

}