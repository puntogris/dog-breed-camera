package com.puntogris.dogbreedcamera.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.puntogris.dogbreedcamera.R
import com.puntogris.dogbreedcamera.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding:FragmentWelcomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
        binding.welcomeFragment = this

        return binding.root
    }

    fun requestCameraPermission(){
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) navigateToCamera()
                 else permissionDenied()
            }
        when {
            permissionGranted() -> navigateToCamera()
            shouldShowRequestPermissionRationale("Camera") -> {
                //what to do here
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun permissionGranted() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun permissionDenied(){
        Toast.makeText(requireContext(),"We need access to the camera to run the app.",Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCamera(){
        findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
    }
}