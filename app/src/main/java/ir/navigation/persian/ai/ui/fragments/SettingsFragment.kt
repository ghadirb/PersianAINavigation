package ir.navigation.persian.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ir.navigation.persian.ai.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadSettings()
    }
    
    private fun setupUI() {
        binding.switchVoiceAlerts.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "هشدارهای صوتی فعال شد" else "هشدارهای صوتی غیرفعال شد",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Save setting
        }
        
        binding.switchSpeedCameraAlert.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "هشدار دوربین فعال شد" else "هشدار دوربین غیرفعال شد",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Save setting
        }
        
        binding.btnAddCamera.setOnClickListener {
            Toast.makeText(requireContext(), "افزودن دوربین سرعت", Toast.LENGTH_SHORT).show()
            // TODO: Show add camera dialog
        }
    }
    
    private fun loadSettings() {
        // TODO: Load settings from preferences
        binding.tvCameraCount.text = "تعداد دوربین‌ها: 0"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
