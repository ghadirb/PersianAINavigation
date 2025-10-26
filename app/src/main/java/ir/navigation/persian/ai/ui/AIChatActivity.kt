package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.navigation.persian.ai.ai.NavigationAIAssistant
import ir.navigation.persian.ai.crypto.KeyManager
import ir.navigation.persian.ai.databinding.ActivityAichatBinding
import kotlinx.coroutines.launch

class AIChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAichatBinding
    private lateinit var keyManager: KeyManager
    private lateinit var aiAssistant: NavigationAIAssistant
    private lateinit var chatAdapter: ChatAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAichatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        keyManager = KeyManager(this)
        aiAssistant = NavigationAIAssistant(this, keyManager)
        
        setupUI()
        updateModelInfo()
    }
    
    private fun setupUI() {
        // Setup RecyclerView
        chatAdapter = ChatAdapter()
        binding.recyclerChat.apply {
            layoutManager = LinearLayoutManager(this@AIChatActivity)
            adapter = chatAdapter
        }
        
        // Send message button
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.setText("")
            }
        }
        
        // Clear chat button
        binding.btnClearChat.setOnClickListener {
            aiAssistant.clearHistory()
            chatAdapter.clearMessages()
            Toast.makeText(this, "تاریخچه پاک شد", Toast.LENGTH_SHORT).show()
        }
        
        // Add quick action buttons
        binding.btnQuickStart.setOnClickListener {
            binding.etMessage.setText("مسیریابی را شروع کن")
        }
        
        binding.btnQuickStop.setOnClickListener {
            binding.etMessage.setText("مسیریابی را متوقف کن")
        }
        
        binding.btnQuickLocation.setOnClickListener {
            binding.etMessage.setText("موقعیت من کجاست؟")
        }
        
        binding.btnQuickRoute.setOnClickListener {
            binding.etMessage.setText("بهترین مسیر به مقصد چیست؟")
        }
    }
    
    private fun sendMessage(message: String) {
        // Add user message to chat
        chatAdapter.addMessage(ChatMessage(message, true))
        binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
        
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        
        lifecycleScope.launch {
            val response = aiAssistant.sendMessage(message)
            
            // Hide loading
            binding.progressBar.visibility = View.GONE
            binding.btnSend.isEnabled = true
            
            // Add AI response to chat
            chatAdapter.addMessage(ChatMessage(response, false))
            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }
    
    private fun updateModelInfo() {
        val modelName = keyManager.getActiveModelName()
        val keyIndex = keyManager.getCurrentKeyIndex() + 1
        val keyCount = keyManager.getKeyCount()
        
        binding.tvModelInfo.text = "مدل: $modelName | کلید: $keyIndex/$keyCount"
    }
    
    // Simple data class for messages
    data class ChatMessage(val content: String, val isUser: Boolean)
    
    // Simple adapter for chat
    inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
        
        private val messages = mutableListOf<ChatMessage>()
        
        fun addMessage(message: ChatMessage) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
        }
        
        fun clearMessages() {
            messages.clear()
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MessageViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(
                    if (viewType == 0) ir.navigation.persian.ai.R.layout.item_message_user 
                    else ir.navigation.persian.ai.R.layout.item_message_ai,
                    parent,
                    false
                )
            return MessageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }
        
        override fun getItemCount(): Int = messages.size
        
        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) 0 else 1
        }
        
        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val textView: android.widget.TextView = view.findViewById(ir.navigation.persian.ai.R.id.tvMessage)
            
            fun bind(message: ChatMessage) {
                textView.text = message.content
            }
        }
    }
}
