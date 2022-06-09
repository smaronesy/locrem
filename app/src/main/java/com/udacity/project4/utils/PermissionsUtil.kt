package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

@TargetApi(29)
fun foregroundLocationPermissionApproved(context: Context): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION))
    return foregroundLocationApproved
}

@TargetApi(29)
fun backgroundLocationPermissionApproved(context: Context, runningQOrLater: Boolean): Boolean {
    val backgroundPermissionApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    return backgroundPermissionApproved
}

@TargetApi(29 )
fun requestForegroundLocationPermissions(fragment: Fragment) {
    if (foregroundLocationPermissionApproved(fragment.context!!))
        true
    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    Log.d(ContentValues.TAG, "Request foreground only location permission")
    fragment.requestPermissions(
        permissionsArray,
        resultCode
    )
}

@TargetApi(29 )
fun requestBackgroundLocationPermissions(fragment: Fragment) {
    Snackbar.make(
        fragment.view!!,
        R.string.require_backgound_permission,
        Snackbar.LENGTH_INDEFINITE
    )
        .setAction(R.string.settings) {
            fragment.startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }.show()
}

fun getLocationSettingsResponseTask(fragment: Fragment): Task<LocationSettingsResponse> {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    val settingsClient = LocationServices.getSettingsClient(fragment.requireActivity())
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())

    return locationSettingsResponseTask
}

fun requestDeviceLocationPermissions(fragment: Fragment, exception: Exception, resolve:Boolean = true) {
        if (exception is ResolvableApiException && resolve){
            // Location settings are not satisfied, but this can be fixed
            // by showing the user a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                exception.startResolutionForResult(fragment.requireActivity(),
                    REQUEST_TURN_DEVICE_LOCATION_ON)
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(ContentValues.TAG, "Error geting location settings resolution: " + sendEx.message)
            }
        } else {
            Snackbar.make(
                fragment.view!!,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestDeviceLocationPermissions(fragment, exception, resolve)
            }.show()
        }
}


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
