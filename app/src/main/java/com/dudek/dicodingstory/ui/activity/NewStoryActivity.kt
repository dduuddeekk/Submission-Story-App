package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.databinding.ActivityNewStoryBinding
import com.dudek.dicodingstory.ui.model.NewStoryViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private val viewModel: NewStoryViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra(EXTRA_TOKEN)
        viewModel.setToken(token ?: "")
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token is required to upload stories!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val imageUri = intent.getStringExtra(IMAGE_URI)

        binding.apply {
            if (imageUri.isNullOrEmpty()) {
                Glide.with(this@NewStoryActivity)
                    .load(R.drawable.ic_insert_photo_24)
                    .into(ivCover)
            } else {
                Glide.with(this@NewStoryActivity)
                    .load(imageUri)
                    .into(ivCover)
            }

            ibCamera.setOnClickListener {
                val intent = Intent(this@NewStoryActivity, CameraActivity::class.java)
                intent.putExtra(EXTRA_TOKEN, token)
                startActivity(intent)
                finish()
            }

            swIncludeLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    getCurrentLocation()
                } else {
                    latitude = null
                    longitude = null
                }
            }

            btUpload.setOnClickListener {
                val description = etStory.text.toString()
                viewModel.setDescription(description)
                if (description.isNotEmpty() && imageUri != null) {
                    uploadStory(token, description, imageUri, latitude, longitude)
                } else {
                    Toast.makeText(this@NewStoryActivity, "Description cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    Toast.makeText(this, "Location is not found. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun uploadStory(token: String, description: String, imageUri: String, latitude: Double?, longitude: Double?) {
        val image = Uri.parse(imageUri)
        Log.d("NewStoryActivity", "Parsed URI: $image")

        val imageFile = uriToFile(this, image)
        Log.d("NewStoryActivity", "Image File: ${imageFile?.absolutePath}")

        if (imageFile == null) {
            Toast.makeText(this, "Failed to convert image to file", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBody = MultipartBody.Part.createFormData(
            "photo", imageFile.name, imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().uploadStory("Bearer $token", description, imageBody, latitude, longitude)
                }
                if (!response.error!!) {
                    Toast.makeText(this@NewStoryActivity, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@NewStoryActivity, MainActivity::class.java).apply {
                        putExtra("EXTRA_TOKEN", token)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@NewStoryActivity, "Failed to upload: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NewStoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val file = File(context.cacheDir, "${System.currentTimeMillis()}_temp.jpg")

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("NewStoryActivity", "Error converting URI to file: ${e.message}")
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val IMAGE_URI = "image_uri"
        private const val REQUEST_PERMISSIONS = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
    }
}