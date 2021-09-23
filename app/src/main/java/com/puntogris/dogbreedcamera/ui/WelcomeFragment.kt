package com.puntogris.dogbreedcamera.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentWelcomeBinding
import com.puntogris.dogbreedcamera.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>(R.layout.fragment_welcome) {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun initializeViews() {
        binding.welcomeFragment = this
        setupRequestPermissionLauncher()
    }

    private fun setupRequestPermissionLauncher(){
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) navigateToCamera() else permissionDenied()
            }
    }

    fun requestCameraPermission(){
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun permissionDenied(){
        Toast.makeText(requireContext(), R.string.toast_camera_permission_required,Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCamera(){
        findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
    }
}