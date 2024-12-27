package com.puntogris.dogbreedcamera.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentWelcomeBinding
import com.puntogris.dogbreedcamera.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private val binding by viewBinding(FragmentWelcomeBinding::bind)

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRequestPermissionLauncher()
        binding.welcomeLayout.setOnClickListener { requestCameraPermission() }
    }

    private fun setupRequestPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) navigateToCamera() else permissionDenied()
            }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun permissionDenied() {
        Toast.makeText(
            requireContext(),
            R.string.toast_camera_permission_required,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToCamera() {
        findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
    }
}