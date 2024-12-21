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
import android.util.Log
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
    private var cameraProvider: ProcessCameraProvider? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d(TAG, "Selected image URI: $uri")
            uri?.let {
                // Mengecek apakah file picker URI dapat diakses
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.close()

                    // Kirimkan URI ke NewStoryActivity
                    sendImageToNewStoryActivity(it, token)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to access the image URI: ${e.message}")
                    Toast.makeText(this, "Cannot access the image URI", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra(EXTRA_TOKEN)
        Log.d(TAG, "Token received: $token")

        if (hasPermissions()) {
            Log.d(TAG, "All permissions granted.")
            initializeFeatures()
        } else {
            Log.d(TAG, "Requesting permissions.")
            requestPermissions()
        }

        binding.ivCameraSwitch.setOnClickListener {
            isBackCamera = !isBackCamera
            Log.d(TAG, "Camera switched. isBackCamera: $isBackCamera")
            startCamera()
        }

        binding.cbvCapture.setOnClickListener {
            Log.d(TAG, "Capture button clicked.")
            captureImage()
        }

        binding.ivGalleryButton.setOnClickListener {
            Log.d(TAG, "Gallery button clicked.")
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

        Log.d(TAG, "Camera permission: $cameraPermission, Gallery permission: $galleryPermission")
        return cameraPermission && galleryPermission
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        Log.d(TAG, "Requesting permissions: $permissions")
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "Permissions result for requestCode: $requestCode")
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            Log.d(TAG, "All permissions granted after request.")
            initializeFeatures()
        } else {
            Log.d(TAG, "Permission denied.")
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeFeatures() {
        Log.d(TAG, "Initializing features.")
        startCamera()
        loadGalleryImage()
    }

    private fun startCamera() {
        Log.d(TAG, "Starting camera.")
        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)

        cameraProviderFeature.addListener({
            try {
                cameraProvider = cameraProviderFeature.get()
                cameraProvider?.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.pvCamera.surfaceProvider
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(
                        if (isBackCamera) CameraSelector.LENS_FACING_BACK
                        else CameraSelector.LENS_FACING_FRONT
                    )
                    .build()

                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Log.d(TAG, "Camera started successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Camera start failed: ", e)
                Toast.makeText(this, "Camera Failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        Log.d(TAG, "Capturing image.")
        val outputDir = cacheDir
        val outputFile = File.createTempFile("captured_image", ".jpg", outputDir)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Image captured: ${outputFile.absolutePath}")
                    val compressedFile = compressImage(outputFile)
                    val savedUri = Uri.fromFile(compressedFile)
                    Log.d(TAG, "Compressed image URI: $savedUri")
                    sendImageToNewStoryActivity(savedUri, token)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed: ", exception)
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to capture image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun loadGalleryImage() {
        Log.d(TAG, "Loading gallery image.")
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val imageId = it.getLong(columnIndex)
                val imageUri = Uri.withAppendedPath(uri, imageId.toString())
                Log.d(TAG, "Latest gallery image URI: $imageUri")

                Glide.with(this)
                    .load(imageUri)
                    .into(binding.ivGalleryButton)
            }
        } ?: Log.e(TAG, "Cursor is null or empty.")
    }

    private fun compressImage(file: File): File {
        Log.d(TAG, "Compressing image: ${file.absolutePath}")
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
        Log.d(TAG, "Image compressed: ${file.absolutePath}")
        return file
    }

    private fun sendImageToNewStoryActivity(imageUri: Uri, token: String?) {
        Log.d(TAG, "Sending image to NewStoryActivity. URI: $imageUri, Token: $token")
        val intent = Intent(this, NewStoryActivity::class.java).apply {
            putExtra("image_uri", imageUri.toString())
            putExtra(EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called. Releasing camera resources.")
        cameraProvider?.unbindAll()
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}