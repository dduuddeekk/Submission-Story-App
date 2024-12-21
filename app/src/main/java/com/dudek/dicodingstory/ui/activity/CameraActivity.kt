package com.dudek.dicodingstory.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dudek.dicodingstory.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var camera: Camera? = null
    private var isBackCamera = true
    private var imageCapture: ImageCapture? = null
    private var token: String? = null
    private var cameraProvider: ProcessCameraProvider? = null

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
    }

    private fun hasPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        return cameraPermission
    }

    private fun requestPermissions() {
        val permissions = listOf(Manifest.permission.CAMERA)
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
    }

    private fun startCamera() {
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
                    sendImageToNewStoryActivity(Uri.fromFile(compressedFile), token)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to capture image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        val compressedFile = File(cacheDir, "compressed_${file.name}")
        FileOutputStream(compressedFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }

        return compressedFile
    }

    private fun sendImageToNewStoryActivity(imageUri: Uri, token: String?) {
        val intent = Intent(this, NewStoryActivity::class.java).apply {
            putExtra("image_uri", imageUri.toString())
            putExtra(EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}