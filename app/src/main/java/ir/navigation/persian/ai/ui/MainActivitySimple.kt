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
        
        try {
            // Create MapView programmatically (NOT in XML!)
            val mapView = org.maplibre.android.maps.MapView(this)
            
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                map.setStyle(org.maplibre.android.maps.Style.Builder()
                    .fromUri("https://demotiles.maplibre.org/style.json")) {
                    
                    // تهران
                    val tehran = org.maplibre.android.geometry.LatLng(35.6892, 51.3890)
                    map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                        .target(tehran)
                        .zoom(12.0)
                        .build()
                    
                    Toast.makeText(this, "🗺️ نقشه بارگذاری شد", Toast.LENGTH_SHORT).show()
                }
            }
            
            contentFrame.addView(mapView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        } catch (e: Exception) {
            val textView = TextView(this)
            textView.text = "خطا در بارگذاری نقشه:\n${e.message}"
            textView.textSize = 16f
            textView.gravity = android.view.Gravity.CENTER
            contentFrame.addView(textView)
        }
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
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "🔍 جستجوی مکان"
        title.textSize = 20f
        layout.addView(title)
        
        val editText = EditText(this)
        editText.hint = "نام مکان (مثلاً میدان آزادی)"
        layout.addView(editText)
        
        val resultText = TextView(this)
        resultText.setPadding(16, 16, 16, 16)
        layout.addView(resultText)
        
        val btnSearch = Button(this)
        btnSearch.text = "جستجو"
        btnSearch.setOnClickListener {
            val query = editText.text.toString()
            if (query.isNotEmpty()) {
                resultText.text = "در حال جستجو..."
                // TODO: Nominatim API
                resultText.text = "جستجو برای: $query\n(API در حال پیاده‌سازی)"
            } else {
                Toast.makeText(this, "لطفاً نام مکان را وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(btnSearch)
        
        contentFrame.addView(layout)
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
