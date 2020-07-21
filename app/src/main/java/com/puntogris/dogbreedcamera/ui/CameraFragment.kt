package com.puntogris.dogbreedcamera.ui

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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.puntogris.dogbreedcamera.utils.ImageAnalyzer
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var cameraExecutor: ExecutorService
    @Inject lateinit var imageAnalyzer : ImageAnalyzer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)

        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            imageAnalyzerBinding = imageAnalyzer
            overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) enableOverlay() else disableOverlay()
            }
        }
        imageAnalyzer.dogBreedResult.observe(viewLifecycleOwner, Observer {result ->
            result?.let{ binding.overlayView.updateOverlay(result.overlayRect)}
        })

        return binding.root
    }

    private fun disableOverlay() {
        binding.overlayView.visibility = View.GONE
        binding.overlaySwitch.text = requireContext().getText(R.string.enable_object_detection_overlay)
    }

    private fun enableOverlay() {
        binding.overlayView.visibility = View.VISIBLE
        binding.overlaySwitch.text = requireContext().getText(R.string.disable_object_detection_overlay)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val rotation = binding.viewFinder.display.rotation

            preview = Preview.Builder()
                .build()

            val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, imageAnalyzer) }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis)
                preview.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
            } catch(exc: Exception) {
                Log.e("CameraXBasic", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

}