package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.inject

const val KEY_REMINDER = "reminder"

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    //Check if API level is 29 and above
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    //Check if API level is 30 and above
    private val running30OrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.R

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE) {
            if (grantResults.size > 0 &&
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED) {
                checkDeviceLocationSettingsAndStartGeofence()
            }
//            else if (grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED) {
//                println("Not Granted")
//
//                Snackbar.make(
//                    binding.root,
//                    R.string.permission_denied_explanation,
//                    Snackbar.LENGTH_INDEFINITE
//                ).setAction(R.string.settings) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }.show()
//            }
        } else if (grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            ((requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    || requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE) &&
                    grantResults[LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)) {
            Snackbar.make(
                binding.root,
                R.string.gep_permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        }
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        _viewModel.reminderTitle.value = reminderTitle.text.toString()
        _viewModel.reminderDescription.value = reminderDescription.text.toString()

        // Retrieving chosen location info in case of configuration change
        if(savedInstanceState != null){
            val reminder = savedInstanceState.getSerializable(KEY_REMINDER) as ReminderDataItem
            _viewModel.reminderTitle.value = reminder.title
            _viewModel.reminderDescription.value = reminder.description
            _viewModel.reminderSelectedLocationStr.value = reminder.location
            _viewModel.latitude.value = reminder.latitude
            _viewModel.longitude.value = reminder.longitude
        }

        geofencingClient = LocationServices.getGeofencingClient(this.requireActivity())

        binding.saveReminder.setOnClickListener {

            if(runningQOrLater) {
                if(running30OrLater) {
                    if(!foregroundLocationPermissionApproved(requireContext())){
                        requestForegroundLocationPermissions(this)
                    }
                    if (!backgroundLocationPermissionApproved(requireContext(), runningQOrLater)) {
                        requestBackgroundLocationPermissions(this)
                    }

                    if(backgroundLocationPermissionApproved(this.requireContext(), runningQOrLater)) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }

                } else {

                    if(!foregroundLocationPermissionApproved(requireContext())
                        || !backgroundLocationPermissionApproved(this.requireContext(), runningQOrLater)) {
                        requestForegroundAndBackgroundLocationPermissions(this, runningQOrLater)
                    } else {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DEVICE_LOCATION_SETTINGS) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationSettingsResponseTask =
            getLocationSettingsResponseTask(this)

        locationSettingsResponseTask.addOnFailureListener { exception ->
            requestDeviceLocationPermissions(this, exception, resolve)
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForReminder()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun addGeofenceForReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        val reminder = ReminderDataItem(title, description, location, latitude, longitude)

        if(title != null && title != "" && latitude != null) {
            // use the user entered reminder details to:
            // 1) add a geofencing request
            val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(
                    latitude!!,
                    longitude!!,
                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Toast.makeText(requireActivity(), R.string.geofences_added,
                        Toast.LENGTH_SHORT)
                        .show()
                    Log.e("Add Geofence", geofence.requestId)
                }
                addOnFailureListener {
                    Toast.makeText(requireActivity(), R.string.geofences_not_added,
                        Toast.LENGTH_SHORT).show()
                    if ((it.message != null)) {
                        Log.w(TAG, it.message.toString())
                    }
                }
            }
        }
        // 2) save the reminder to the local db
        _viewModel.validateAndSaveReminder(reminder)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // saving chosen location info in case of configuration change
        val reminder = ReminderDataItem(_viewModel.reminderTitle.value,
        _viewModel.reminderDescription.value,
        _viewModel.reminderSelectedLocationStr.value,
        _viewModel.latitude.value,
        _viewModel.longitude.value)
        outState.putSerializable(KEY_REMINDER, reminder)

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "Reminders.addReminder.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val REQUEST_CODE_DEVICE_LOCATION_SETTINGS = 27
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val LOCATION_PERMISSION_INDEX = 1


