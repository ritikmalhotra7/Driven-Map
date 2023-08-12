package com.example.drivenmap.feat_map.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.example.drivenmap.feat_map.utils.Utils.REQUEST_CODE_LOCATION_PERMISSION
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class LocationPermissionManager(
    private val fragment: Fragment,
    private val onPermissionGranted: () -> Unit
) : EasyPermissions(), EasyPermissions.PermissionCallbacks {

    init {
        getLocationPermissions(fragment.requireContext(), onPermissionGranted)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        onRequestPermissionsResult(requestCode, permissions, grantResults, fragment)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            onPermissionGranted?.let {
                it()
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && somePermissionPermanentlyDenied(
                fragment,
                perms
            )
        ) {
            AppSettingsDialog.Builder(fragment).build().show()
        } else {
            requestPermissions()
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun getLocationPermissions(ctx: Context, ifLocationAlreadyGranted: () -> Unit) {
        if (hasLocationPermissions(ctx)) {
            val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                ctx.startActivity(settingIntent)
            }
            ifLocationAlreadyGranted()
        } else {
            requestPermissions()
        }
    }

    private fun hasLocationPermissions(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermissions(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            hasPermissions(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                fragment,
                "You need to accept location permissions",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            requestPermissions(
                fragment,
                "You need to accept location permissions",
                Utils.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}