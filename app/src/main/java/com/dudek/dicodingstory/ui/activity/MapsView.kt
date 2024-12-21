package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.ListStoryItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dudek.dicodingstory.databinding.ActivityMapsViewBinding
import kotlinx.coroutines.launch

class MapsView : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsViewBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val token = intent.getStringExtra("EXTRA_TOKEN")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        token?.let {
            fetchStories("Bearer $it")
        } ?: run {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun fetchStories(token: String) {
        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val call = apiService.getAllStories(token, 1) // digunakan di sini
                val stories = call.listStory?.filterNotNull() ?: emptyList()

                addMarkerToMap(stories)
            } catch (e: Exception) {
                Toast.makeText(this@MapsView, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarkerToMap(stories: List<ListStoryItem>) {
        for (story in stories) {
            val lat = story.lat as? Double ?: continue
            val lon = story.lon as? Double ?: continue
            val location = LatLng(lat, lon)
            mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(story.name)
                    .snippet(story.description)
            )
        }
        if (stories.isNotEmpty()) {
            val firstStory = stories[0]
            val lat = firstStory.lat as? Double
            val lon = firstStory.lon as? Double
            if (lat != null && lon != null) {
                val firstLocation = LatLng(lat, lon)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
            }
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}