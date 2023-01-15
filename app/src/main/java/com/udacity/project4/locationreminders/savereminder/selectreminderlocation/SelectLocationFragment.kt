package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


@SuppressLint("UnspecifiedImmutableFlag")

class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "locationReminders.geofence.action.ACTION_GEOFENCE_EVENT"
    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var map: GoogleMap
    private lateinit var interestPoint: Marker

    private var zoomLevel = 15f
    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1



    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        setHasOptionsMenu(true)
        this.setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
          mapFragment.getMapAsync(this)

        return binding.root
    }


    //region MAPS'REGION

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        val latitude = 37.422160
        val longitude = -122.084270
        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

        setMapLongClick(map)

        setMapStyle(map)

        setPoiClick(map)

        enableMyLocation()

        onLocationSelected()
    }

    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { latLng ->
//            binding.btnSaveLocation.visibility = View.VISIBLE
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            interestPoint = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Random Location")
                    .snippet(snippet)

            )
            Toast.makeText(this.context,"Random Location",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
//            binding.btnSaveLocation.visibility = View.VISIBLE
             interestPoint = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            interestPoint.showInfoWindow()
        }
    }

    private fun onLocationSelected() {

        binding.btnSaveLocation.setOnClickListener {
            if(this::interestPoint.isInitialized){
                    _viewModel.latitude.value = interestPoint.position.latitude
                    _viewModel.longitude.value = interestPoint.position.longitude
                    _viewModel.reminderSelectedLocationStr.value =interestPoint.title
            }else{
//                Toast.makeText(this.context,"Please select a location",Toast.LENGTH_SHORT).show()
            }
          findNavController().popBackStack()
        }

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }


//endregion


    //region PERMISSIONS'REGION

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        }
        else {
            this.requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted() : Boolean {

        return  ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                enableMyLocation()

            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }
//endregion


    //region MENU'REGION
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // endregion


}




