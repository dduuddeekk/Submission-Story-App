package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
                    return@setOnClickListener
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_READ_MEDIA_IMAGES
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    Toast.makeText(this, "Location is not found. Try again!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                102
            )
        }
    }

    private fun uploadStory(token: String, description: String, imageUri: String, latitude: Double?, longitude: Double?) {
        val image = Uri.parse(imageUri)
        val imageFile = uriToFile(this, image)

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
                if (response.error == false) {
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
            return null
        }

        val file = File(context.cacheDir, "temp_image.jpg")

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return file
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_READ_EXTERNAL_STORAGE -> {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                }
                REQUEST_READ_MEDIA_IMAGES -> {
                    Toast.makeText(this, "Media access permission granted", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            when (requestCode) {
                REQUEST_READ_EXTERNAL_STORAGE -> {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
                REQUEST_READ_MEDIA_IMAGES -> {
                    Toast.makeText(this, "Media access permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val IMAGE_URI = "image_uri"
        private const val REQUEST_READ_MEDIA_IMAGES = 101
        private const val REQUEST_READ_EXTERNAL_STORAGE = 102
    }
}