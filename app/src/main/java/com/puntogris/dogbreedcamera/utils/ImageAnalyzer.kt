package com.puntogris.dogbreedcamera.utils

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageAnalyzer @Inject constructor():ImageAnalysis.Analyzer {

    private val _dogBreedResult = MutableLiveData<String>()
    val dogBreedResult:LiveData<String> = _dogBreedResult

    private val localModel = LocalModel.Builder()
        .setAssetFilePath("dog_breed_detection.tflite")
        .build()

    private val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.6f)
            .setMaxPerObjectLabelCount(1)
            .build()

    private val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null){
            val image = InputImage.fromMediaImage(mediaImage,imageProxy.imageInfo.rotationDegrees)
            objectDetector.process(image)
                .addOnFailureListener{
                    Log.d(TAG, it.printStackTrace().toString())
                }
                .addOnSuccessListener { listDetectedObjects ->
                    for (detectedObject in listDetectedObjects){
                       detectedObject.labels[0]?.let { label ->
                           _dogBreedResult.value = label.text
                       }
                    }
                }.addOnCompleteListener{
                    imageProxy.close()
                }
        }
    }
}