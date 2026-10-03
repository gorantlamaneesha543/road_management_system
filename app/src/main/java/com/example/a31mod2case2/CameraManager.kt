package com.example.a31mod2case2

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File

class CameraManager(
    private val fragment: Fragment,
    private val previewView: PreviewView
) {

    private var imageCapture: ImageCapture? = null

    private val handler = Handler(Looper.getMainLooper())

    private var running = false

    private val captureInterval = 5000L

    fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(fragment.requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider =
                        previewView.surfaceProvider
                }

            imageCapture =
                ImageCapture.Builder()
                    .setTargetRotation(Surface.ROTATION_0)
                    .setCaptureMode(
                        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    )
                    .build()

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    fragment,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (e: Exception) {

                Log.e(
                    "CameraManager",
                    "Camera start failed",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(fragment.requireContext()))
    }

    fun startContinuousCapture(
        callback: (File) -> Unit
    ) {

        if (running) return

        running = true

        handler.post(object : Runnable {

            override fun run() {

                if (!running) return

                capturePhoto(callback)

                handler.postDelayed(
                    this,
                    captureInterval
                )
            }
        })
    }

    private fun capturePhoto(
        callback: (File) -> Unit
    ) {

        val capture = imageCapture ?: return

        val file = File.createTempFile(
            "road_inspection_",
            ".jpg",
            fragment.requireContext().cacheDir
        )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(file)
                .build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(
                fragment.requireContext()
            ),
            object :
                ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults:
                    ImageCapture.OutputFileResults
                ) {

                    callback(file)
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    Log.e(
                        "CameraManager",
                        "Capture failed",
                        exception
                    )
                }
            }
        )
    }

    fun stopContinuousCapture() {

        running = false

        handler.removeCallbacksAndMessages(null)
    }

    fun release() {

        stopContinuousCapture()
    }
}
