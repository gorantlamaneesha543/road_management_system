package com.example.a31mod2case2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a31mod2case2.databinding.FragmentReportsBinding
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReports()
    }

    private fun loadReports() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getSummary()
                val currentBinding = _binding ?: return@launch

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        currentBinding.tvReportTitle.text = "📋 Road Inspection Reports"
                        currentBinding.tvReportData.text = """
                            ========================================
                            OFFICIAL ROAD MANAGEMENT SYSTEM REPORT
                            ========================================
                            
                            • Inspections Conducted        : ${data.total_inspections}
                            • Road Damages Detected        : ${data.total_damages}
                            • Potholes Identified          : ${data.potholes}
                            • Cracks Identified            : ${data.cracks}
                            • High-Risk Cases (Severity)   : ${data.high_severity}
                            
                            ----------------------------------------
                            What is a High-Risk Case?
                            A high-risk case is any road damage classified 
                            as HIGH severity (e.g., severe potholes or 
                            deep structural cracks) posing an immediate 
                            hazard to motorists and requiring urgent repair.
                            ----------------------------------------
                            
                            Status: Connected to Backend & SQLite Database.
                        """.trimIndent()
                    }
                } else {
                    currentBinding.tvReportData.text = "Failed to load report data from backend server."
                }
            } catch (e: Exception) {
                val currentBinding = _binding
                if (currentBinding != null) {
                    currentBinding.tvReportData.text = """
                        Unable to connect to Backend Server.
                        
                        Error: ${e.message}
                        
                        Please ensure python app.py is running on your computer at http://10.48.112.192:5000/
                    """.trimIndent()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
