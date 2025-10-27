package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

/**
 * MainActivity بدون Fragment - همه چیز ساده
 */
class MainActivitySimple : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var contentFrame: FrameLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Main layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        
        // Content area
        contentFrame = FrameLayout(this)
        mainLayout.addView(contentFrame, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        
        // Tab Layout
        tabLayout = TabLayout(this)
        mainLayout.addView(tabLayout, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        
        setContentView(mainLayout)
        
        // Setup tabs
        setupTabs()
        
        // Show first tab
        showMapTab()
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("🗺️ نقشه"))
        tabLayout.addTab(tabLayout.newTab().setText("📍 ذخیره"))
        tabLayout.addTab(tabLayout.newTab().setText("🔍 جستجو"))
        tabLayout.addTab(tabLayout.newTab().setText("🤖 AI"))
        tabLayout.addTab(tabLayout.newTab().setText("⚙️ سایر"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showMapTab()
                    1 -> showSavedTab()
                    2 -> showSearchTab()
                    3 -> showChatTab()
                    4 -> showSettingsTab()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun showMapTab() {
        contentFrame.removeAllViews()
        
        val textView = TextView(this)
        textView.text = "🗺️ نقشه\n\nنقشه در حال توسعه است"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        
        contentFrame.addView(textView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }
    
    private fun showSavedTab() {
        contentFrame.removeAllViews()
        
        val textView = TextView(this)
        textView.text = "📍 مکان‌های ذخیره شده\n\nدر حال توسعه"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        
        contentFrame.addView(textView)
    }
    
    private fun showSearchTab() {
        contentFrame.removeAllViews()
        
        val textView = TextView(this)
        textView.text = "🔍 جستجو\n\nدر حال توسعه"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        
        contentFrame.addView(textView)
    }
    
    private fun showChatTab() {
        contentFrame.removeAllViews()
        
        val textView = TextView(this)
        textView.text = "🤖 چت AI\n\nدر حال توسعه"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        
        contentFrame.addView(textView)
    }
    
    private fun showSettingsTab() {
        contentFrame.removeAllViews()
        
        val textView = TextView(this)
        textView.text = "⚙️ تنظیمات\n\nدر حال توسعه"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        
        contentFrame.addView(textView)
    }
}
