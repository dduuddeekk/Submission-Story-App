package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.StoryUploadResponse
import com.dudek.dicodingstory.databinding.ActivityNewStoryBinding
import com.dudek.dicodingstory.ui.model.NewStoryViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private val viewModel: NewStoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
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
                    Log.d("NewStoryActivity", "Description or image is missing!")
                    return@setOnClickListener
                }

                viewModel.setDescription(description)

                val file = uriToFile(viewModel.imageUri.value!!)
                if (file != null) {
                    uploadStory(viewModel.token.value!!, description, file)
                } else {
                    Toast.makeText(this@NewStoryActivity, "Invalid file!", Toast.LENGTH_SHORT).show()
                }
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
            Log.e("NewStoryActivity", "Error converting URI to File: ${e.message}", e)
            null
        }
    }

    private fun uploadStory(token: String, description: String, photoFile: File) {
        val apiService = ApiConfig.getApiService()

        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoBody = MultipartBody.Part.createFormData(
            "photo", photoFile.name, photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        apiService.uploadStory("Bearer $token", descriptionBody, photoBody).enqueue(object : Callback<StoryUploadResponse> {
            override fun onResponse(call: Call<StoryUploadResponse>, response: Response<StoryUploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@NewStoryActivity, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@NewStoryActivity, MainActivity::class.java)
                    intent.putExtra("EXTRA_TOKEN", viewModel.token.value)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@NewStoryActivity, "Failed to upload: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.d("NewStoryActivity", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoryUploadResponse>, t: Throwable) {
                Toast.makeText(this@NewStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("NewStoryActivity", "Failure: ${t.message}")
            }
        })
    }
}
