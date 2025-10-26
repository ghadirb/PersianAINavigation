package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.navigation.persian.ai.R

/**
 * MainActivity - تست ساده
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // فقط یک صفحه ساده - بدون نقشه
            setContentView(R.layout.activity_test_simple)
            Toast.makeText(this, "✅ برنامه باز شد", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            e.printStackTrace()
        }
    }
}
