package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MainActivityComplete : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var contentFrame: FrameLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        
        contentFrame = FrameLayout(this)
        mainLayout.addView(contentFrame, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        tabLayout = TabLayout(this)
        mainLayout.addView(tabLayout, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT))
        
        setContentView(mainLayout)
        setupTabs()
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
            val mapView = MapView(this)
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(35.6892, 51.3890)).zoom(12.0).build()
                    Toast.makeText(this, "🗺️ نقشه آماده", Toast.LENGTH_SHORT).show()
                }
            }
            contentFrame.addView(mapView)
        } catch (e: Exception) {
            showError("خطا: ${e.message}")
        }
    }
    
    private fun showSavedTab() {
        contentFrame.removeAllViews()
        showSimpleText("📍 مکان‌های ذخیره شده\n\nبزودی")
    }
    
    private fun showSearchTab() {
        contentFrame.removeAllViews()
        showSimpleText("🔍 جستجو\n\nبزودی")
    }
    
    private fun showChatTab() {
        contentFrame.removeAllViews()
        showSimpleText("🤖 چت AI\n\nبزودی")
    }
    
    private fun showSettingsTab() {
        contentFrame.removeAllViews()
        showSimpleText("⚙️ تنظیمات\n\nبزودی")
    }
    
    private fun showSimpleText(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 24f
        tv.gravity = android.view.Gravity.CENTER
        contentFrame.addView(tv)
    }
    
    private fun showError(msg: String) {
        val tv = TextView(this)
        tv.text = msg
        tv.textSize = 16f
        tv.gravity = android.view.Gravity.CENTER
        contentFrame.addView(tv)
    }
}
