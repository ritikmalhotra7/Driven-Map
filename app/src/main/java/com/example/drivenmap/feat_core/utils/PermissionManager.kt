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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class PermissionManager constructor(
    fragment: Fragment,
    private val foregroundPermLauncher: ActivityResultLauncher<Array<String>>,
    private val backgroundPermLauncher: ActivityResultLauncher<String>,
    private val alreadyGranted :()->Unit
) {
    private var dialog: AlertDialog? = null

    init {
        requestForegroundLocationPerm(fragment.requireActivity())
        showDialogIfLocationIsNotEnable(fragment)
    }
    private fun checkPermission(context: Context, perm: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            perm
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForegroundLocationPerm(activity: FragmentActivity) {
        if (!checkPermission(activity.baseContext, Manifest.permission.ACCESS_FINE_LOCATION) &&
            !checkPermission(activity.baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            foregroundPermLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }else{
            alreadyGranted()
        }
    }

    fun requestBackgroundLocationPerm(activity: AppCompatActivity, onGranted: (() -> Unit)?) {
        if (!checkPermission(activity.baseContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundPermLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    fun showDialogIfLocationIsNotEnable(fragment: Fragment) {
        val locationManager =
            fragment.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isLocationEnabled) {
            if (dialog == null) {
                // Show a dialog to ask the user to turn on location
                val builder = AlertDialog.Builder(fragment.requireContext()).apply {
                    setTitle("Turn on Location")
                    setMessage("This app needs location to work properly. Please turn on location.")
                    setPositiveButton(
                        "Turn on Location",
                        DialogInterface.OnClickListener { dialog, which ->
                            // Start the location settings activity
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            fragment.requireActivity().startActivity(intent)
                            this@PermissionManager.dialog = null
                        })
                    setNegativeButton("Cancel") { dialog, int ->
                        dialog.cancel()
                        this@PermissionManager.dialog = null
                    }
                }
                dialog = builder.show()
            }
        }
    }
}