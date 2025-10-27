package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import ir.navigation.persian.ai.api.NominatimAPI
import ir.navigation.persian.ai.api.OSMRAPI
import ir.navigation.persian.ai.db.SavedPlaceDatabase
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import java.util.Locale

class MainActivityComplete : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var tts: TextToSpeech? = null
    private val nominatimAPI = NominatimAPI()
    private val osmrAPI = OSMRAPI()
    private lateinit var database: SavedPlaceDatabase
    private var currentMapView: MapView? = null
    private var currentLocation: Location? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = SavedPlaceDatabase(this)
        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("fa", "IR") }
        
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
        currentMapView?.onDestroy()
        try {
            val frame = FrameLayout(this)
            val mapView = MapView(this)
            currentMapView = mapView
            mapView.onCreate(null)
            frame.addView(mapView)
            
            val fabGPS = FloatingActionButton(this)
            fabGPS.setImageResource(android.R.drawable.ic_menu_mylocation)
            fabGPS.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        loc?.let {
                            currentLocation = it
                            mapView.getMapAsync { map -> map.cameraPosition = CameraPosition.Builder().target(LatLng(it.latitude, it.longitude)).zoom(15.0).build() }
                            Toast.makeText(this, "📍 موقعیت شما", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                }
            }
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = android.view.Gravity.END or android.view.Gravity.BOTTOM
            params.setMargins(0, 0, 32, 200)
            frame.addView(fabGPS, params)
            
            val btnNav = Button(this)
            btnNav.text = "🚗 مسیریابی"
            btnNav.setOnClickListener {
                currentLocation?.let { loc ->
                    lifecycleScope.launch {
                        try {
                            val routes = osmrAPI.getRoutes(loc.latitude, loc.longitude, 35.6997, 51.3380, 3)
                            if (routes.isNotEmpty()) {
                                tts?.speak("${routes.size} مسیر یافت شد. فاصله ${(routes[0].distance/1000).toInt()} کیلومتر", TextToSpeech.QUEUE_FLUSH, null, null)
                                Toast.makeText(this@MainActivityComplete, "✅ ${routes.size} مسیر یافت شد", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivityComplete, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: Toast.makeText(this, "ابتدا موقعیت را مشخص کنید", Toast.LENGTH_SHORT).show()
            }
            val btnParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            btnParams.gravity = android.view.Gravity.BOTTOM
            btnParams.setMargins(32, 0, 32, 32)
            frame.addView(btnNav, btnParams)
            
            mapView.getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    map.cameraPosition = CameraPosition.Builder().target(LatLng(35.6892, 51.3890)).zoom(12.0).build()
                    map.addMarker(MarkerOptions().position(LatLng(35.7000, 51.4000)).title("📷 دوربین"))
                    map.addMarker(MarkerOptions().position(LatLng(35.6800, 51.3800)).title("🚨 سرعت‌گیر"))
                    tts?.speak("نقشه آماده است", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
            contentFrame.addView(frame)
        } catch (e: Exception) {
            showError("خطا: ${e.message}")
        }
    }
    
    private fun showSavedTab() {
        contentFrame.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "📍 مکان‌های ذخیره شده"
        title.textSize = 20f
        layout.addView(title)
        
        val btnAdd = Button(this)
        btnAdd.text = "افزودن مکان تست"
        btnAdd.setOnClickListener {
            database.addPlace("مکان ${System.currentTimeMillis()}", 35.6892, 51.3890)
            showSavedTab()
        }
        layout.addView(btnAdd)
        
        val places = database.getAllPlaces()
        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL
        
        places.forEach { place ->
            val tv = TextView(this)
            tv.text = "${place.name}\n${place.latitude}, ${place.longitude}"
            tv.setPadding(16, 16, 16, 16)
            listLayout.addView(tv)
        }
        
        scrollView.addView(listLayout)
        layout.addView(scrollView)
        contentFrame.addView(layout)
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
                lifecycleScope.launch {
                    try {
                        val results = nominatimAPI.search(query, 5)
                        resultText.text = results.joinToString("\n\n") { "📍 ${it.name}\n${it.lat}, ${it.lon}" }
                        tts?.speak("${results.size} نتیجه یافت شد", TextToSpeech.QUEUE_FLUSH, null, null)
                    } catch (e: Exception) {
                        resultText.text = "خطا: ${e.message}"
                    }
                }
            }
        }
        layout.addView(btnSearch)
        contentFrame.addView(layout)
    }
    
    private fun showChatTab() {
        contentFrame.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "🤖 دستیار هوشمند"
        title.textSize = 20f
        layout.addView(title)
        
        val scrollView = ScrollView(this)
        val chatLayout = LinearLayout(this)
        chatLayout.orientation = LinearLayout.VERTICAL
        
        val welcomeMsg = TextView(this)
        welcomeMsg.text = "سلام! چطور می‌توانم کمکتان کنم؟"
        welcomeMsg.setPadding(16, 16, 16, 16)
        welcomeMsg.setBackgroundColor(0xFF333333.toInt())
        welcomeMsg.setTextColor(0xFFFFFFFF.toInt())
        chatLayout.addView(welcomeMsg)
        
        scrollView.addView(chatLayout)
        layout.addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        val editText = EditText(this)
        editText.hint = "پیام خود را بنویسید..."
        layout.addView(editText)
        
        val btnSend = Button(this)
        btnSend.text = "ارسال"
        btnSend.setOnClickListener {
            val msg = editText.text.toString()
            if (msg.isNotEmpty()) {
                val userMsg = TextView(this)
                userMsg.text = msg
                userMsg.setPadding(16, 16, 16, 16)
                userMsg.setBackgroundColor(0xFF0066CC.toInt())
                userMsg.setTextColor(0xFFFFFFFF.toInt())
                chatLayout.addView(userMsg)
                
                val aiMsg = TextView(this)
                aiMsg.text = "متوجه شدم. از تب نقشه برای مسیریابی استفاده کنید."
                aiMsg.setPadding(16, 16, 16, 16)
                aiMsg.setBackgroundColor(0xFF333333.toInt())
                aiMsg.setTextColor(0xFFFFFFFF.toInt())
                chatLayout.addView(aiMsg)
                
                editText.text.clear()
                tts?.speak("پیام دریافت شد", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        layout.addView(btnSend)
        contentFrame.addView(layout)
    }
    
    private fun showSettingsTab() {
        contentFrame.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "⚙️ تنظیمات"
        title.textSize = 20f
        layout.addView(title)
        
        val btnUnlock = Button(this)
        btnUnlock.text = "مدیریت کلیدهای API"
        btnUnlock.setOnClickListener {
            startActivity(Intent(this, UnlockActivity::class.java))
        }
        layout.addView(btnUnlock)
        
        val info = TextView(this)
        info.text = "\n\nمسیریاب هوشمند فارسی\nنسخه 1.0\n\nامکانات:\n✅ نقشه MapLibre\n✅ مسیریابی OSRM\n✅ جستجوی Nominatim\n✅ هشدارهای صوتی فارسی\n✅ چت AI\n✅ ذخیره مکان‌ها"
        info.setPadding(16, 16, 16, 16)
        layout.addView(info)
        
        contentFrame.addView(layout)
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
    
    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        currentMapView?.onDestroy()
    }
}
