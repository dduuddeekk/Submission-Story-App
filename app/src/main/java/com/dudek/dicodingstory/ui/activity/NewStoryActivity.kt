package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.StoryUploadResponse
import com.dudek.dicodingstory.databinding.ActivityNewStoryBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
//import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra("EXTRA_TOKEN")
        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.ivCover)
        }

        binding.apply {
            ibCamera.setOnClickListener {
                val intent = Intent(this@NewStoryActivity, CameraActivity::class.java)
                startActivity(intent)
            }

            btUpload.setOnClickListener {
                val description = etStory.text.toString().trim()
                if (description.isEmpty() || imageUri == null) {
                    Toast.makeText(this@NewStoryActivity, "Description or image is missing!", Toast.LENGTH_SHORT).show()
                    Log.d("NewStoryActivity", "Description or image is missing!")
                    return@setOnClickListener
                }

                val file = imageUri.path?.let { file -> File(file) }
                file?.let { it1 -> uploadStory(token, description, it1) }
            }
        }
    }

    private fun uploadStory(token: String?, description: String, photoFile: File) {
        val apiService = ApiConfig.getApiService()

        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoBody = MultipartBody.Part.createFormData(
            "photo", photoFile.name, photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        val call = if (token != null) {
            apiService.uploadStory("Bearer $token", descriptionBody, photoBody)
        } else {
            apiService.addStory(descriptionBody, photoBody)
        }

        call.enqueue(object : Callback<StoryUploadResponse> {
            override fun onResponse(call: Call<StoryUploadResponse>, response: Response<StoryUploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@NewStoryActivity, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
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
