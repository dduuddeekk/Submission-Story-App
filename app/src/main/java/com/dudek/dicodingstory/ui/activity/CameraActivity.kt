package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var camera: Camera? = null
    private var isBackCamera = true
    private var imageCapture: ImageCapture? = null
    private var token: String? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = File(cacheDir, "gallery_image.jpg")
                contentResolver.openInputStream(it)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val compressedFile = compressImage(file)
                sendImageToNewStoryActivity(Uri.fromFile(compressedFile), token)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra(EXTRA_TOKEN)

        if (hasPermissions()) {
            initializeFeatures()
        } else {
            requestPermissions()
        }

        binding.ivCameraSwitch.setOnClickListener {
            isBackCamera = !isBackCamera
            startCamera()
        }

        binding.cbvCapture.setOnClickListener {
            captureImage()
        }

        binding.ivGalleryButton.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }
    }

    private fun hasPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        return cameraPermission && galleryPermission
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            initializeFeatures()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeFeatures() {
        startCamera()
        loadGalleryImage()
    }

    private fun startCamera() {
        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)

        cameraProviderFeature.addListener({
            try {
                val cameraProvider = cameraProviderFeature.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build()
                preview.surfaceProvider = binding.pvCamera.surfaceProvider

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(
                        if (isBackCamera) CameraSelector.LENS_FACING_BACK
                        else CameraSelector.LENS_FACING_FRONT
                    )
                    .build()

                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera Failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val outputDir = cacheDir
        val outputFile = File.createTempFile("captured_image", ".jpg", outputDir)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val compressedFile = compressImage(outputFile)
                    val savedUri = Uri.fromFile(compressedFile)
                    sendImageToNewStoryActivity(savedUri, token)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun loadGalleryImage() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val imageId = cursor.getLong(columnIndex)
            val imageUri = Uri.withAppendedPath(uri, imageId.toString())

            Glide.with(this)
                .load(imageUri)
                .into(binding.ivGalleryButton)

            cursor.close()
        }
    }

    private fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var quality = 100
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        while (outputStream.toByteArray().size > 1_000_000) {
            outputStream.reset()
            quality -= 5
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        file.outputStream().use {
            it.write(outputStream.toByteArray())
        }
        return file
    }

    private fun sendImageToNewStoryActivity(imageUri: Uri, token: String?) {
        val intent = Intent(this, NewStoryActivity::class.java).apply {
            putExtra("image_uri", imageUri.toString())
            putExtra(EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}
