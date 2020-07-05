package com.puntogris.dogbreedcamera.ui

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentCameraBinding
import java.io.File
import java.lang.Math.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.Manifest
import kotlin.math.ln

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()

            val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val rotation = binding.viewFinder.display.rotation
            ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                analyzeFrames(image)
            })
            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis)
                preview?.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun analyzeFrames(imageProxy: ImageProxy){
        val localModel = LocalModel.Builder()
            .setAssetFilePath("dog_breed_detection.tflite")
            .build()


        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.6f)
                .setMaxPerObjectLabelCount(1)
                .build()
        val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

        val image = InputImage.fromMediaImage(imageProxy.image!!,imageProxy.imageInfo.rotationDegrees)
        objectDetector.process(image)
            .addOnFailureListener{
                Log.d(TAG, it.printStackTrace().toString())
            }
            .addOnSuccessListener {
                if (it.isNotEmpty()){
                    println(it)
                for (detedtedObject in it){
                    if(detedtedObject.labels.isNotEmpty()){
                    binding.dogBreed.text = detedtedObject.labels[0].text
                    }
                }}
            }.addOnCompleteListener{
                imageProxy.close()
            }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
            <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var rotationMatrix: Matrix

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

        val image = imageProxy.image ?: return null

        // Initialise Buffer
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            Log.d(TAG, "Initalise toBitmap()")
            rotationMatrix = Matrix()
            rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            )
        }

        // Pass image to an image analyser
        //yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

        // Create the Bitmap in the correct orientation
        return Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            rotationMatrix,
            false
        )
    }


companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}