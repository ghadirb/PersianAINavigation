package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    
    private lateinit var recyclerChat: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainLayout = LinearLayout(requireContext())
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setPadding(16, 16, 16, 16)
        
        // Title
        val title = TextView(requireContext())
        title.text = "🤖 چت با دستیار هوشمند"
        title.textSize = 20f
        title.setPadding(0, 0, 0, 16)
        mainLayout.addView(title)
        
        // Chat area (ScrollView)
        val scrollView = ScrollView(requireContext())
        recyclerChat = LinearLayout(requireContext())
        recyclerChat.orientation = LinearLayout.VERTICAL
        scrollView.addView(recyclerChat)
        
        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        mainLayout.addView(scrollView, scrollParams)
        
        // Welcome message
        addAIMessage("سلام! من دستیار مسیریابی شما هستم. چطور می‌توانم کمکتان کنم؟")
        
        // Input area
        val inputLayout = LinearLayout(requireContext())
        inputLayout.orientation = LinearLayout.HORIZONTAL
        inputLayout.setPadding(0, 16, 0, 0)
        
        etMessage = EditText(requireContext())
        etMessage.hint = "پیام خود را بنویسید..."
        val etParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        inputLayout.addView(etMessage, etParams)
        
        btnSend = Button(requireContext())
        btnSend.text = "ارسال"
        btnSend.setOnClickListener { sendMessage() }
        inputLayout.addView(btnSend)
        
        mainLayout.addView(inputLayout)
        
        return mainLayout
    }
    
    private fun sendMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isEmpty()) return
        
        addUserMessage(message)
        etMessage.text.clear()
        
        // Simulate AI response
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            
            val response = when {
                message.contains("مسیر") || message.contains("راه") -> 
                    "برای مسیریابی، به تب نقشه بروید و دکمه 'شروع مسیریابی' را بزنید."
                message.contains("ترافیک") -> 
                    "اطلاعات ترافیک در حال حاضر در دسترس نیست. این قابلیت به زودی اضافه خواهد شد."
                message.contains("کجا") || message.contains("جایی") -> 
                    "می‌توانید از تب جستجو استفاده کنید تا مکان مورد نظر را پیدا کنید."
                message.contains("ذخیره") -> 
                    "در تب 'ذخیره' می‌توانید مکان‌های خود را ذخیره و مدیریت کنید."
                message.contains("سلام") || message.contains("درود") -> 
                    "سلام! چطور می‌توانم کمکتان کنم؟"
                else -> 
                    "متوجه شدم. آیا می‌خواهید از مسیریابی، جستجو یا سایر امکانات استفاده کنید؟"
            }
            
            addAIMessage(response)
        }
    }
    
    private fun addUserMessage(text: String) {
        val messageView = TextView(requireContext())
        messageView.text = text
        messageView.textSize = 16f
        messageView.setPadding(16, 12, 16, 12)
        messageView.setBackgroundColor(0xFF0066CC.toInt())
        messageView.setTextColor(0xFFFFFFFF.toInt())
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(64, 8, 8, 8)
        params.gravity = android.view.Gravity.END
        
        recyclerChat.addView(messageView, params)
    }
    
    private fun addAIMessage(text: String) {
        val messageView = TextView(requireContext())
        messageView.text = "🤖 $text"
        messageView.textSize = 16f
        messageView.setPadding(16, 12, 16, 12)
        messageView.setBackgroundColor(0xFF333333.toInt())
        messageView.setTextColor(0xFFFFFFFF.toInt())
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 8, 64, 8)
        
        recyclerChat.addView(messageView, params)
    }
}
