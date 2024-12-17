package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.databinding.ActivityNewStoryBinding
import com.dudek.dicodingstory.ui.model.NewStoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private val viewModel: NewStoryViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission denied to access media.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        val token = intent.getStringExtra("EXTRA_TOKEN")
        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token is required to upload stories!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.setToken(token)
        viewModel.setImageUri(imageUri)

        viewModel.imageUri.observe(this) { uri ->
            uri?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.ivCover)
            }
        }

        binding.apply {
            ibCamera.setOnClickListener {
                val intent = Intent(this@NewStoryActivity, CameraActivity::class.java)
                intent.putExtra("EXTRA_TOKEN", viewModel.token.value)
                startActivity(intent)
                finish()
            }

            btUpload.setOnClickListener {
                val description = etStory.text.toString().trim()
                if (description.isEmpty() || viewModel.imageUri.value == null) {
                    Toast.makeText(this@NewStoryActivity, "Description or image is missing!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val file = uriToFile(viewModel.imageUri.value!!)
                if (file != null) {
                    uploadStory(viewModel.token.value!!, description, file)
                } else {
                    Toast.makeText(this@NewStoryActivity, "Invalid file!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            val name = cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            } ?: "temp_image.jpg"

            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, name)
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Toast.makeText(this@NewStoryActivity, "Error converting URI to File: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun uploadStory(token: String, description: String, photoFile: File) {
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoBody = MultipartBody.Part.createFormData(
            "photo", photoFile.name, photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().uploadStory("Bearer $token", descriptionBody, photoBody)
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
}