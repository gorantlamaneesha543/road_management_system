package com.example.a31mod2case2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a31mod2case2.databinding.FragmentInspectorBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class InspectorFragment : Fragment() {

    private var _binding: FragmentInspectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraManager: CameraManager
    private lateinit var locationManager: LocationManager
    private var inspectionRunning = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = LocationManager(requireContext())
        setupButtons()
        checkPermissions()
    }

    private fun setupButtons() {
        binding.btnStartInspection.setOnClickListener {
            if (hasPermissions()) {
                if (!inspectionRunning) {
                    startInspection()
                } else {
                    stopInspection()
                }
            } else {
                requestPermissions()
            }
        }
    }

    private fun startInspection() {
        inspectionRunning = true
        binding.btnStartInspection.text = "STOP INSPECTION"
        binding.btnStartInspection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error))
        binding.tvInspectionStatus.text = "🔴 LIVE INSPECTION RUNNING"

        cameraManager = CameraManager(this, binding.previewView)
        cameraManager.startCamera()
        cameraManager.startContinuousCapture { file ->
            processInspectionImage(file)
        }

        Toast.makeText(requireContext(), "Real-time road inspection started", Toast.LENGTH_SHORT).show()
    }

    private fun stopInspection() {
        inspectionRunning = false
        if (::cameraManager.isInitialized) {
            cameraManager.stopContinuousCapture()
        }
        binding.btnStartInspection.text = "START INSPECTION"
        binding.btnStartInspection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        binding.tvInspectionStatus.text = "Camera Ready"

        Toast.makeText(requireContext(), "Inspection stopped", Toast.LENGTH_SHORT).show()
    }

    private fun processInspectionImage(file: File) {
        lifecycleScope.launch {
            val location = locationManager.getCurrentLocation()
            if (location == null) {
                if (_binding != null) {
                    binding.tvInspectionStatus.text = "GPS location unavailable"
                }
                file.delete()
                return@launch
            }

            try {
                binding.tvInspectionStatus.text = "Analyzing frame via Groq AI..."
                val imageRequest = file.asRequestBody("image/jpeg".toMediaType())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, imageRequest)
                val latitude = location.latitude.toString().toRequestBody()
                val longitude = location.longitude.toString().toRequestBody()

                val response = ApiClient.api.inspectRoad(imagePart, latitude, longitude)
                val currentBinding = _binding ?: return@launch

                if (response.isSuccessful) {
                    val result = response.body()
                    result?.inspection?.let { inspection ->
                        currentBinding.tvInspectionStatus.text = "🟢 AI Analysis Complete"
                        currentBinding.tvDamageType.text = "Damage: ${inspection.damage_type}"
                        currentBinding.tvSeverity.text = "Severity: ${inspection.severity}"
                        currentBinding.tvConfidence.text = "Confidence: ${String.format("%.1f", inspection.confidence * 100)}%"
                        currentBinding.tvLocation.text = "GPS: ${String.format("%.6f", inspection.latitude)}, ${String.format("%.6f", inspection.longitude)}"
                        currentBinding.tvRecommendation.text = "Recommendation: ${inspection.recommendation}"
                    }
                } else {
                    currentBinding.tvInspectionStatus.text = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                val currentBinding = _binding
                if (currentBinding != null) {
                    currentBinding.tvInspectionStatus.text = "Connection error: ${e.message}\nCheck if python app.py is running."
                }
            } finally {
                file.delete()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 100)
    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            requestPermissions()
        }
    }

    override fun onDestroyView() {
        if (::cameraManager.isInitialized) {
            cameraManager.release()
        }
        _binding = null
        super.onDestroyView()
    }
}
