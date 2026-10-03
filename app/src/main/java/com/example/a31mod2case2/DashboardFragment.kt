package com.example.a31mod2case2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a31mod2case2.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSummary()

        binding.btnRefresh.setOnClickListener {
            loadSummary()
        }
    }

    private fun loadSummary() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getSummary()
                val currentBinding = _binding ?: return@launch

                if (response.isSuccessful) {
                    val summary = response.body()
                    if (summary != null) {
                        currentBinding.tvTotal.text = "Total Inspections Conducted: ${summary.total_inspections}"
                        currentBinding.tvDamages.text = "Road Damages Detected: ${summary.total_damages}"
                        currentBinding.tvPotholes.text = "Potholes Identified: ${summary.potholes}"
                        currentBinding.tvCracks.text = "Cracks Identified: ${summary.cracks}"
                        currentBinding.tvHighRisk.text = "High-Risk Cases (Severity HIGH): ${summary.high_severity}"
                        Toast.makeText(requireContext(), "Dashboard updated from backend", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load summary from backend", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val currentBinding = _binding
                if (currentBinding != null) {
                    currentBinding.tvTotal.text = "Total Inspections Conducted: --"
                    currentBinding.tvDamages.text = "Road Damages Detected: --"
                    currentBinding.tvPotholes.text = "Potholes Identified: --"
                    currentBinding.tvCracks.text = "Cracks Identified: --"
                    currentBinding.tvHighRisk.text = "High-Risk Cases (Severity HIGH): --"
                }
                Toast.makeText(requireContext(), "Backend connection error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
