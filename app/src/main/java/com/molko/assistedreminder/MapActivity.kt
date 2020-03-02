package com.molko.assistedreminder

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var gMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var selectedLatLng: LatLng
    lateinit var geofencingClient: GeofencingClient
    
    val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
    val GEOFENCE_RADIUS = 500.0
    val GEOFENCE_EXPIRATION = 120 * 24 * 60 * 60 * 1000
    val GEOFENCE_DWELL_DELAY = 2 * 60 * 1000
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        (map_fragment as SupportMapFragment).getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        
        searchMap.setOnClickListener {
            with (gMap) {
                val locations = Geocoder(applicationContext, Locale.getDefault())
                    .getFromLocationName(searchAutoComplete.text.toString(), 1)
                val latLng = LatLng(locations.get(0).latitude, locations.get(0).longitude)
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
            }
        }
        
        mapCreate.setOnClickListener {
            val text = editMapMessage.text.toString()
            if (text.isEmpty()) {
                toast("Please provide reminder text")
                return@setOnClickListener
            }
            if (selectedLatLng == null) {
                toast("Please select a location")
                return@setOnClickListener
            }
            
            val reminder = Reminder(
                uid = null,
                time = null,
                location = String.format("%.3f,%.3f", selectedLatLng.latitude, selectedLatLng.longitude),
                message = text
            )
            doAsync {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "reminders"
                ).build()
                val uid = db.reminderDao().insert(reminder).toInt()
                reminder.uid = uid
                db.close()
                createGeofence(selectedLatLng, reminder, geofencingClient)
            }
            finish()
        }
    }
    
    private fun createGeofence(selectedLatLng: LatLng, reminder: Reminder, geofencingClient: GeofencingClient) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(selectedLatLng.latitude, selectedLatLng.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()
        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geofence)
            .build()
        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("uid", reminder.uid)
            .putExtra("message", reminder.message)
            .putExtra("location", reminder.location)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        geofencingClient.addGeofences(geofenceRequest, pendingIntent)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 123) {
            var permissionsGranted = grantResults.isNotEmpty()
            for (res in grantResults) {
                if (res == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false
                }
            }
            if (!permissionsGranted) {
                toast("The app needs all the permissions to function")
            }
        }
    }
    
    override fun onMapReady(map: GoogleMap?) {
        gMap = map?: return
        val permissions = mutableListOf<String>()
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        
        var permissionsGranted = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false
            }
        }
        
        if (permissionsGranted) {
            gMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener{location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    with (gMap) {
                        animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                    }
                }
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 123)
        }
        
        gMap.setOnMapClickListener { latLng :LatLng ->
            with (gMap) {
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var title = ""
                var city = ""
                try {
                    val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    city = addressList.get(0).locality
                    title = addressList.get(0).getAddressLine(0)
                }
                catch (e: Exception) {}
                val marker = addMarker(MarkerOptions()
                    .position(latLng)
                    .snippet(title)
                    .title(city)
                )
                marker.showInfoWindow()
                selectedLatLng = latLng
                addCircle(CircleOptions()
                    .center(latLng)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(100, 150, 150, 150))
                )
            }
        }
    }
}
