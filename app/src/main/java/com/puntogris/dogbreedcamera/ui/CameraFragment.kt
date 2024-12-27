package com.puntogris.dogbreedcamera.ui

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.analizer.ImageAnalyzer
import com.puntogris.dogbreedcamera.databinding.FragmentCameraBinding
import com.puntogris.dogbreedcamera.model.BreedResult
import com.puntogris.dogbreedcamera.utils.gone
import com.puntogris.dogbreedcamera.utils.viewBinding
import com.puntogris.dogbreedcamera.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera), ImageAnalyzer.BreedResultListener {

    private val binding by viewBinding(FragmentCameraBinding::bind)

    private lateinit var preview: Preview
    private lateinit var cameraExecutor: ExecutorService

    private val imageAnalyzer = ImageAnalyzer(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) enableOverlay() else disableOverlay()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build()

            val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                preview.surfaceProvider = binding.viewFinder.surfaceProvider
            } catch (exc: Exception) {
                Timber.e(exc, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun disableOverlay() {
        binding.overlayView.gone()
        binding.overlaySwitch.setText(R.string.enable_object_detection_overlay)
    }

    private fun enableOverlay() {
        binding.overlayView.visible()
        binding.overlaySwitch.setText(R.string.disable_object_detection_overlay)
    }

    override fun onBreedResult(result: BreedResult) {
        binding.overlayView.updateOverlay(result.overlayRect)
        binding.dogBreed.text = result.label
    }
}