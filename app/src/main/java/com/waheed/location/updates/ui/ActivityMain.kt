package com.waheed.location.updates.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.androidisland.ezpermission.EzPermission
import com.waheed.location.updates.livedata.LocationViewModel
import com.waheed.location.updates.livedata.R
import com.waheed.location.updates.utils.LocationUtil
import kotlinx.android.synthetic.main.main_activity.*

/**
 * Created by Waheed on 03,December,2019
 */

/**
 * Constants Values
 */
const val LOCATION_REQUEST = 100
const val LOCATION_PERMISSION_REQUEST = 101


/**
 * Main Activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var locationViewModel: LocationViewModel
    private var isGPSEnabled = false


    /**
     * onCreate of activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Instance of LocationViewModel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)


        //Check weather Location/GPS is ON or OFF
        LocationUtil(this).turnGPSOn(object :
            LocationUtil.OnLocationOnListener {

            override fun locationStatus(isLocationOn: Boolean) {
                this@MainActivity.isGPSEnabled = isLocationOn
            }
        })
    }

    /**
     * Observe LocationViewModel LiveData to get updated location
     */
    private fun observeLocationUpdates() {
        locationViewModel.getLocationData.observe(this, Observer {
            longitude.text = it.longitude.toString()
            latitude.text = it.latitude.toString()
            info.text = getString(R.string.location_successfully_received)
        })
    }


    /**
     * onStart lifecycle of activity
     */
    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }


    /**
     * Initiate Location updated by checking Location/GPS settings is ON or OFF
     * Requesting permissions to read location.
     */
    private fun startLocationUpdates() {
        when {
            !isGPSEnabled -> {
                info.text = getString(R.string.enable_gps)
            }

            isPermissionsGranted() -> {
                observeLocationUpdates()
            }
            else -> {
                askLocationPermission()
            }
        }
    }

    /**
     * Check the availability of location permissions
     */
    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    /**
     *
     */
    private fun askLocationPermission() {
        EzPermission
            .with(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .request { granted, denied, permanentlyDenied ->
                if (granted.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    granted.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) { // Granted
                    startLocationUpdates()

                } else if (denied.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) { // Denied

                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Location Permission Denied")
                    dialog.setMessage("The app will not work without providing location permission. Allow permission ?")
                    dialog.setNegativeButton("Cancel") { _, _ -> }
                    dialog.setPositiveButton("Allow") { _, _ ->
                        askLocationPermission()
                    }
                    dialog.setOnCancelListener { } //important
                    dialog.show()

                } else if (permanentlyDenied.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permanentlyDenied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) { // Permanently denied
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Location Permission Permanently Denied")
                    dialog.setMessage("The app will not work without providing location permission.\n\n Open App settings -> Permissions -> Allow permission.")
                    dialog.setNegativeButton("Not now") { _, _ -> }
                    dialog.setPositiveButton("Settings") { _, _ ->
                        startActivity(
                            EzPermission.appDetailSettingsIntent(
                                this
                            )
                        )
                    }
                    dialog.setOnCancelListener { } //important
                    dialog.show()
                }

            }
    }

}