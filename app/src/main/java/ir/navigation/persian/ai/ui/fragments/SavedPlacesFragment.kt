package ir.navigation.persian.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ir.navigation.persian.ai.databinding.FragmentSavedPlacesBinding

class SavedPlacesFragment : Fragment() {
    
    private var _binding: FragmentSavedPlacesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        loadSavedPlaces()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewPlaces.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set adapter with saved places
    }
    
    private fun setupUI() {
        binding.fabAddPlace.setOnClickListener {
            Toast.makeText(requireContext(), "افزودن مکان جدید", Toast.LENGTH_SHORT).show()
            // TODO: Show dialog to add new place
        }
    }
    
    private fun loadSavedPlaces() {
        // TODO: Load saved places from database
        // For now, show empty state
        binding.tvEmptyState.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
