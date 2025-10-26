package ir.navigation.persian.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.navigation.persian.ai.databinding.ActivityMainBinding

/**
 * MainActivity - صفحه اصلی برنامه
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        // دکمه باز کردن نقشه
        binding.btnOpenMap.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
        }
        
        // دکمه شروع مسیریابی
        binding.btnStartNavigation.setOnClickListener {
            Toast.makeText(this, "شروع مسیریابی", Toast.LENGTH_SHORT).show()
            // TODO: Start navigation
        }
        
        // دکمه توقف مسیریابی
        binding.btnStopNavigation.setOnClickListener {
            Toast.makeText(this, "توقف مسیریابی", Toast.LENGTH_SHORT).show()
            // TODO: Stop navigation
        }
        
        // دکمه چت با AI
        binding.btnOpenAIChat.setOnClickListener {
            Toast.makeText(this, "چت با AI", Toast.LENGTH_SHORT).show()
            // TODO: Open AI chat
        }
        
        // سایر دکمه‌ها
        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "ورود به Google Drive - قابلیت غیرفعال است", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnSyncData.setOnClickListener {
            Toast.makeText(this, "همگام‌سازی - قابلیت غیرفعال است", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnAddCamera.setOnClickListener {
            Toast.makeText(this, "افزودن دوربین سرعت", Toast.LENGTH_SHORT).show()
            // TODO: Add camera
        }
        
        binding.btnRefreshKeys.setOnClickListener {
            Toast.makeText(this, "بروزرسانی کلیدها", Toast.LENGTH_SHORT).show()
            // TODO: Refresh keys
        }
        
        binding.switchVoiceAlerts.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "هشدارهای صوتی فعال" else "هشدارهای صوتی غیرفعال",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        updateStatus()
    }
    
    private fun updateStatus() {
        binding.tvStatus.text = "وضعیت: آماده"
        binding.tvCurrentLocation.text = "موقعیت فعلی: -"
        binding.tvAlert.text = "هشدار: -"
        binding.tvPrediction.text = "پیش‌بینی مسیر: -"
        binding.tvCameraCount.text = "تعداد دوربین‌ها: 0"
        binding.tvKeyStatus.text = "مدل: آماده | کلید: 0/0"
    }
}
