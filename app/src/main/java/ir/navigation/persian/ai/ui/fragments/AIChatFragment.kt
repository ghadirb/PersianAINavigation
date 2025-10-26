package ir.navigation.persian.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ir.navigation.persian.ai.databinding.FragmentAiChatBinding

class AIChatFragment : Fragment() {
    
    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set chat adapter
    }
    
    private fun setupUI() {
        binding.fabSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), "ارسال: $message", Toast.LENGTH_SHORT).show()
                binding.etMessage.text?.clear()
                // TODO: Send message to AI
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
