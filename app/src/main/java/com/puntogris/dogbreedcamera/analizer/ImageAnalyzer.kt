package com.puntogris.dogbreedcamera.analizer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.puntogris.dogbreedcamera.model.BreedResult
import timber.log.Timber

class ImageAnalyzer(
    private val listener: BreedResultListener
) : ImageAnalysis.Analyzer {

    interface BreedResultListener {
        fun onBreedResult(result: BreedResult)
    }

    private val localModel = LocalModel.Builder().setAssetFilePath("DogBreedModel.tflite").build()

    private val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.3f)
            .setMaxPerObjectLabelCount(1)
            .build()

    private val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        objectDetector.process(image)
            .addOnFailureListener {
                Timber.d(it.printStackTrace().toString())
            }
            .addOnSuccessListener {
                for (detectedObject in it) {
                    if (detectedObject.labels.isNotEmpty()) {
                        listener.onBreedResult(
                            BreedResult(
                                detectedObject.labels[0].text, detectedObject.boundingBox
                            )
                        )
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}