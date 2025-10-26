package ir.navigation.persian.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.navigation.persian.ai.crypto.KeyManager
import ir.navigation.persian.ai.databinding.ActivityUnlockBinding
import kotlinx.coroutines.launch

/**
 * صفحه ورود رمز و باز کردن قفل کلیدها
 */
class UnlockActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUnlockBinding
    private lateinit var keyManager: KeyManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        keyManager = KeyManager(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnUnlock.setOnClickListener {
            val password = binding.etPassword.text.toString()
            
            if (password.isEmpty()) {
                Toast.makeText(this, "لطفا رمز را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            unlockKeys(password)
        }
        
        binding.btnDownloadKeys.setOnClickListener {
            downloadKeys()
        }
        
        binding.tvSkip.setOnClickListener {
            // ادامه بدون باز کردن قفل
            navigateToMain()
        }
    }
    
    private fun unlockKeys(password: String) {
        binding.btnUnlock.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = "در حال رمزگشایی..."
        
        lifecycleScope.launch {
            val success = keyManager.unlockKeys(password)
            
            binding.btnUnlock.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE
            
            if (success) {
                binding.tvStatus.text = "قفل باز شد! ✓"
                binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                
                val modelName = keyManager.getActiveModelName()
                val keyCount = keyManager.getKeyCount()
                
                Toast.makeText(
                    this@UnlockActivity,
                    "مدل فعال: $modelName\nتعداد کلیدها: $keyCount",
                    Toast.LENGTH_LONG
                ).show()
                
                // انتقال به صفحه اصلی بعد از 1 ثانیه
                binding.root.postDelayed({
                    navigateToMain()
                }, 1000)
                
            } else {
                binding.tvStatus.text = "رمز اشتباه است یا فایل کلیدها موجود نیست"
                binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                Toast.makeText(this@UnlockActivity, "رمزگشایی ناموفق بود", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun downloadKeys() {
        binding.btnDownloadKeys.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = "در حال دانلود کلیدها از Google Drive..."
        
        lifecycleScope.launch {
            val success = keyManager.downloadEncryptedKeys()
            
            binding.btnDownloadKeys.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE
            
            if (success) {
                binding.tvStatus.text = "کلیدها دانلود شدند. حالا رمز را وارد کنید"
                binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                Toast.makeText(this@UnlockActivity, "دانلود موفق بود", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvStatus.text = "خطا در دانلود کلیدها"
                binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                Toast.makeText(this@UnlockActivity, "دانلود ناموفق بود", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
