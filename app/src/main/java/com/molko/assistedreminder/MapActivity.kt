package com.molko.assistedreminder

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
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
                db.reminderDao().insert(reminder)
                db.close()
                finish()
            }
        }
    }
    
    override fun onMapReady(map: GoogleMap?) {
        gMap = map?: return
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED) {
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
            ActivityCompat.requestPermissions(this, permissions, 123)
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
            }
        }
    }
}
