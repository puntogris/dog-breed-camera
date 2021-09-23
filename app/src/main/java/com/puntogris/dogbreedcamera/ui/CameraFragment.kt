package com.puntogris.dogbreedcamera.ui

import android.util.DisplayMetrics
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentCameraBinding
import com.puntogris.dogbreedcamera.ui.base.BaseFragment
import com.puntogris.dogbreedcamera.analizer.ImageAnalyzer
import com.puntogris.dogbreedcamera.utils.gone
import com.puntogris.dogbreedcamera.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(R.layout.fragment_camera) {

    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var cameraExecutor: ExecutorService
    @Inject lateinit var imageAnalyzer : ImageAnalyzer

    override fun initializeViews() {
        with(binding){
            lifecycleOwner = viewLifecycleOwner
            overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) enableOverlay() else disableOverlay()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        subscribeUi()
        startCamera()
    }

    private fun subscribeUi(){
        imageAnalyzer.dogBreedResult.observe(viewLifecycleOwner){ result ->
            result?.let{
                with(binding){
                    overlayView.updateOverlay(it.overlayRect)
                    dogBreed.text = it.label
                }
            }
        }
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
                .also { it.setAnalyzer(cameraExecutor, imageAnalyzer) }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            } catch(exc: Exception) {
                Timber.e(exc, "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun disableOverlay() {
        binding.overlayView.gone()
        binding.overlaySwitch.text = requireContext().getText(R.string.enable_object_detection_overlay)
    }

    private fun enableOverlay() {
        binding.overlayView.visible()
        binding.overlaySwitch.text = requireContext().getText(R.string.disable_object_detection_overlay)
    }

}