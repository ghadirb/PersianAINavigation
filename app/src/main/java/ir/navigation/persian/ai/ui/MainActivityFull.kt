package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import ir.navigation.persian.ai.api.NominatimAPI
import ir.navigation.persian.ai.api.OSMRAPI
import ir.navigation.persian.ai.data.CameraData
import ir.navigation.persian.ai.db.SavedPlaceDatabase
import ir.navigation.persian.ai.ml.AIRouteLearning
import ir.navigation.persian.ai.model.CameraType
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import java.util.Locale

/**
 * مسیریاب کامل - مانند Google Maps و نشان
 */
class MainActivityFull : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var tts: TextToSpeech? = null
    private val nominatimAPI = NominatimAPI()
    private val osmrAPI = OSMRAPI()
    private lateinit var database: SavedPlaceDatabase
    private lateinit var aiLearning: AIRouteLearning
    private var currentMapView: MapView? = null
    private var currentMap: MapLibreMap? = null
    private var currentLocation: Location? = null
    private var speedTextView: TextView? = null
    private var selectedDestination: LatLng? = null
    private var selectedDestinationName: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = SavedPlaceDatabase(this)
        aiLearning = AIRouteLearning(this)
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
        tabLayout.addTab(tabLayout.newTab().setText("🔍 جستجو"))
        tabLayout.addTab(tabLayout.newTab().setText("📍 ذخیره"))
        tabLayout.addTab(tabLayout.newTab().setText("🤖 AI"))
        tabLayout.addTab(tabLayout.newTab().setText("⚙️ سایر"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showMapTab()
                    1 -> showSearchTab()
                    2 -> showSavedTab()
                    3 -> showChatTab()
                    4 -> showSettingsTab()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    // ========== TAB 1: نقشه با کلیک و انتخاب مقصد ==========
    private fun showMapTab() {
        contentFrame.removeAllViews()
        currentMapView?.onDestroy()
        
        try {
            val frame = FrameLayout(this)
            val mapView = MapView(this)
            currentMapView = mapView
            mapView.onCreate(null)
            frame.addView(mapView)
            
            // Speed Display
            val speedLayout = LinearLayout(this)
            speedLayout.orientation = LinearLayout.VERTICAL
            speedLayout.setBackgroundColor(0xCC000000.toInt())
            speedLayout.setPadding(24, 24, 24, 24)
            
            speedTextView = TextView(this)
            speedTextView?.text = "0 km/h"
            speedTextView?.textSize = 28f
            speedTextView?.setTextColor(0xFFFFFFFF.toInt())
            speedLayout.addView(speedTextView)
            
            val speedParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            speedParams.gravity = android.view.Gravity.TOP or android.view.Gravity.START
            speedParams.setMargins(16, 16, 0, 0)
            frame.addView(speedLayout, speedParams)
            
            // FAB Buttons
            val fabLayout = LinearLayout(this)
            fabLayout.orientation = LinearLayout.VERTICAL
            
            val fabGPS = FloatingActionButton(this)
            fabGPS.setImageResource(android.R.drawable.ic_menu_mylocation)
            fabGPS.setOnClickListener { goToMyLocation(mapView) }
            fabLayout.addView(fabGPS)
            
            val fabZoomIn = FloatingActionButton(this)
            fabZoomIn.setImageResource(android.R.drawable.ic_input_add)
            fabZoomIn.setOnClickListener { zoomIn(mapView) }
            fabLayout.addView(fabZoomIn)
            
            val fabZoomOut = FloatingActionButton(this)
            fabZoomOut.setImageResource(android.R.drawable.ic_delete)
            fabZoomOut.setOnClickListener { zoomOut(mapView) }
            fabLayout.addView(fabZoomOut)
            
            val fabParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            fabParams.gravity = android.view.Gravity.END or android.view.Gravity.BOTTOM
            fabParams.setMargins(0, 0, 32, 200)
            frame.addView(fabLayout, fabParams)
            
            // Setup Map with Click Listener
            mapView.getMapAsync { map ->
                currentMap = map
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    map.cameraPosition = CameraPosition.Builder().target(LatLng(35.6892, 51.3890)).zoom(12.0).build()
                    
                    // Add real cameras
                    CameraData.getTehranCameras().forEach { camera ->
                        val icon = when(camera.type) {
                            CameraType.FIXED_CAMERA -> "📷"
                            CameraType.AVERAGE_SPEED_CAMERA -> "📹"
                            CameraType.SPEED_BUMP -> "🚨"
                            CameraType.TRAFFIC_LIGHT -> "🚦"
                            CameraType.MOBILE_CAMERA -> "📱"
                        }
                        map.addMarker(MarkerOptions()
                            .position(LatLng(camera.latitude, camera.longitude))
                            .title("$icon ${camera.speedLimit} km/h"))
                    }
                    
                    // Map Click Listener - انتخاب مقصد
                    map.addOnMapClickListener { latLng ->
                        showDestinationDialog(latLng)
                        true
                    }
                    
                    tts?.speak("نقشه آماده است", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
            
            contentFrame.addView(frame)
        } catch (e: Exception) {
            showError("خطا: ${e.message}")
        }
    }
    
    private fun showDestinationDialog(latLng: LatLng) {
        selectedDestination = latLng
        selectedDestinationName = "مکان انتخابی"
        
        // Add marker
        currentMap?.addMarker(MarkerOptions()
            .position(latLng)
            .title("📍 مقصد انتخابی"))
        
        // Show dialog
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("مقصد انتخاب شد")
        dialog.setMessage("${latLng.latitude}, ${latLng.longitude}")
        dialog.setPositiveButton("مسیریابی") { _, _ ->
            showRouteOptions(latLng, "مکان انتخابی")
        }
        dialog.setNegativeButton("ذخیره") { _, _ ->
            database.addPlace("مکان ${System.currentTimeMillis()}", latLng.latitude, latLng.longitude)
            Toast.makeText(this, "✅ ذخیره شد", Toast.LENGTH_SHORT).show()
        }
        dialog.setNeutralButton("انصراف", null)
        dialog.show()
    }
    
    private fun showRouteOptions(destination: LatLng, name: String) {
        currentLocation?.let { loc ->
            lifecycleScope.launch {
                try {
                    val routes = osmrAPI.getRoutes(loc.latitude, loc.longitude, destination.latitude, destination.longitude, 3)
                    
                    if (routes.isNotEmpty()) {
                        // Show route selection dialog
                        val routeNames = routes.mapIndexed { index, route ->
                            val distance = (route.distance/1000).toInt()
                            val duration = (route.duration/60).toInt()
                            "مسیر ${index+1}: $distance کیلومتر، $duration دقیقه"
                        }.toTypedArray()
                        
                        AlertDialog.Builder(this@MainActivityFull)
                            .setTitle("انتخاب مسیر")
                            .setItems(routeNames) { _, which ->
                                startDrivingMode(destination, name, routes[which])
                            }
                            .setNegativeButton("انصراف", null)
                            .show()
                        
                        tts?.speak("${routes.size} مسیر یافت شد", TextToSpeech.QUEUE_FLUSH, null, null)
                    } else {
                        Toast.makeText(this@MainActivityFull, "مسیری یافت نشد", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivityFull, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Toast.makeText(this, "ابتدا موقعیت فعلی را مشخص کنید", Toast.LENGTH_SHORT).show()
    }
    
    private fun startDrivingMode(destination: LatLng, name: String, route: OSMRAPI.RouteInfo) {
        val intent = Intent(this, NavigationDrivingActivity::class.java)
        intent.putExtra("startLat", currentLocation?.latitude ?: 0.0)
        intent.putExtra("startLon", currentLocation?.longitude ?: 0.0)
        intent.putExtra("destLat", destination.latitude)
        intent.putExtra("destLon", destination.longitude)
        intent.putExtra("destName", name)
        startActivity(intent)
    }
    
    private fun goToMyLocation(mapView: MapView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    currentLocation = it
                    val speed = it.speed * 3.6
                    speedTextView?.text = "${speed.toInt()} km/h"
                    mapView.getMapAsync { map -> map.cameraPosition = CameraPosition.Builder().target(LatLng(it.latitude, it.longitude)).zoom(15.0).build() }
                    Toast.makeText(this, "📍 موقعیت شما", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }
    
    private fun zoomIn(mapView: MapView) {
        mapView.getMapAsync { map ->
            val current = map.cameraPosition
            map.cameraPosition = CameraPosition.Builder().target(current.target).zoom(current.zoom + 1).build()
        }
    }
    
    private fun zoomOut(mapView: MapView) {
        mapView.getMapAsync { map ->
            val current = map.cameraPosition
            map.cameraPosition = CameraPosition.Builder().target(current.target).zoom(current.zoom - 1).build()
        }
    }
    
    // ========== TAB 2: جستجو کامل با کلیک ==========
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
        
        val scrollView = ScrollView(this)
        val resultLayout = LinearLayout(this)
        resultLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(resultLayout)
        layout.addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        val btnSearch = Button(this)
        btnSearch.text = "جستجو"
        btnSearch.setOnClickListener {
            val query = editText.text.toString()
            if (query.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val results = nominatimAPI.search(query, 10)
                        resultLayout.removeAllViews()
                        
                        results.forEach { place ->
                            val placeCard = LinearLayout(this@MainActivityFull)
                            placeCard.orientation = LinearLayout.VERTICAL
                            placeCard.setPadding(24, 24, 24, 24)
                            placeCard.setBackgroundColor(0xFFEEEEEE.toInt())
                            
                            val placeName = TextView(this@MainActivityFull)
                            placeName.text = "📍 ${place.displayName}"
                            placeName.textSize = 18f
                            placeCard.addView(placeName)
                            
                            val placeCoords = TextView(this@MainActivityFull)
                            placeCoords.text = "${place.lat}, ${place.lon}"
                            placeCoords.textSize = 14f
                            placeCard.addView(placeCoords)
                            
                            val buttonLayout = LinearLayout(this@MainActivityFull)
                            buttonLayout.orientation = LinearLayout.HORIZONTAL
                            
                            val btnShow = Button(this@MainActivityFull)
                            btnShow.text = "نمایش"
                            btnShow.setOnClickListener {
                                tabLayout.selectTab(tabLayout.getTabAt(0))
                                showLocationOnMap(LatLng(place.lat.toDouble(), place.lon.toDouble()), place.displayName)
                            }
                            buttonLayout.addView(btnShow)
                            
                            val btnRoute = Button(this@MainActivityFull)
                            btnRoute.text = "مسیریابی"
                            btnRoute.setOnClickListener {
                                showRouteOptions(LatLng(place.lat.toDouble(), place.lon.toDouble()), place.displayName)
                            }
                            buttonLayout.addView(btnRoute)
                            
                            val btnSave = Button(this@MainActivityFull)
                            btnSave.text = "ذخیره"
                            btnSave.setOnClickListener {
                                database.addPlace(place.displayName, place.lat.toDouble(), place.lon.toDouble())
                                Toast.makeText(this@MainActivityFull, "✅ ذخیره شد", Toast.LENGTH_SHORT).show()
                            }
                            buttonLayout.addView(btnSave)
                            
                            placeCard.addView(buttonLayout)
                            
                            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(0, 0, 0, 16)
                            resultLayout.addView(placeCard, params)
                        }
                        
                        tts?.speak("${results.size} نتیجه یافت شد", TextToSpeech.QUEUE_FLUSH, null, null)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivityFull, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        layout.addView(btnSearch)
        
        contentFrame.addView(layout)
    }
    
    private fun showLocationOnMap(latLng: LatLng, name: String) {
        showMapTab()
        currentMap?.let { map ->
            map.cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.0).build()
            map.addMarker(MarkerOptions().position(latLng).title("📍 $name"))
            selectedDestination = latLng
            selectedDestinationName = name
        }
    }
    
    // ========== TAB 3: ذخیره با مسیریابی ==========
    private fun showSavedTab() {
        contentFrame.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "📍 مکان‌های ذخیره شده"
        title.textSize = 20f
        layout.addView(title)
        
        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL
        
        val places = database.getAllPlaces()
        places.forEach { place ->
            val placeCard = LinearLayout(this)
            placeCard.orientation = LinearLayout.VERTICAL
            placeCard.setPadding(24, 24, 24, 24)
            placeCard.setBackgroundColor(0xFFEEEEEE.toInt())
            
            val placeName = TextView(this)
            placeName.text = place.name
            placeName.textSize = 18f
            placeCard.addView(placeName)
            
            val buttonLayout = LinearLayout(this)
            buttonLayout.orientation = LinearLayout.HORIZONTAL
            
            val btnShow = Button(this)
            btnShow.text = "نمایش"
            btnShow.setOnClickListener {
                tabLayout.selectTab(tabLayout.getTabAt(0))
                showLocationOnMap(LatLng(place.latitude, place.longitude), place.name)
            }
            buttonLayout.addView(btnShow)
            
            val btnRoute = Button(this)
            btnRoute.text = "مسیریابی"
            btnRoute.setOnClickListener {
                showRouteOptions(LatLng(place.latitude, place.longitude), place.name)
            }
            buttonLayout.addView(btnRoute)
            
            placeCard.addView(buttonLayout)
            
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 16)
            listLayout.addView(placeCard, params)
        }
        
        scrollView.addView(listLayout)
        layout.addView(scrollView)
        contentFrame.addView(layout)
    }
    
    // ========== TAB 4 & 5: Chat & Settings ==========
    private fun showChatTab() {
        contentFrame.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "🤖 دستیار هوشمند AI"
        title.textSize = 20f
        title.setTextColor(0xFF000000.toInt())
        layout.addView(title)
        
        val scrollView = ScrollView(this)
        val chatLayout = LinearLayout(this)
        chatLayout.orientation = LinearLayout.VERTICAL
        chatLayout.setPadding(16, 16, 16, 16)
        
        val welcomeMsg = TextView(this)
        welcomeMsg.text = "سلام! من دستیار هوشمند مسیریابی هستم.\n\nچطور می‌توانم کمکتان کنم؟\n\n• مسیریابی\n• جستجوی مکان\n• ذخیره مکان‌ها\n• هشدارهای صوتی"
        welcomeMsg.setPadding(16, 16, 16, 16)
        welcomeMsg.setBackgroundColor(0xFFE3F2FD.toInt())
        welcomeMsg.setTextColor(0xFF000000.toInt())
        welcomeMsg.textSize = 16f
        chatLayout.addView(welcomeMsg)
        
        scrollView.addView(chatLayout)
        layout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        val inputLayout = LinearLayout(this)
        inputLayout.orientation = LinearLayout.HORIZONTAL
        
        val editText = EditText(this)
        editText.hint = "پیام خود را بنویسید..."
        editText.setPadding(16, 16, 16, 16)
        inputLayout.addView(editText, LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        
        val btnSend = Button(this)
        btnSend.text = "ارسال"
        btnSend.setOnClickListener {
            val msg = editText.text.toString()
            if (msg.isNotEmpty()) {
                // پیام کاربر
                val userMsg = TextView(this)
                userMsg.text = "شما: $msg"
                userMsg.setPadding(16, 16, 16, 16)
                userMsg.setBackgroundColor(0xFF2196F3.toInt())
                userMsg.setTextColor(0xFFFFFFFF.toInt())
                userMsg.textSize = 14f
                val userParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                userParams.setMargins(0, 8, 0, 8)
                userMsg.layoutParams = userParams
                chatLayout.addView(userMsg)
                
                // پاسخ AI
                val aiMsg = TextView(this)
                val response = when {
                    msg.contains("مسیر") || msg.contains("راه") -> "برای مسیریابی، به تب نقشه بروید و روی نقشه کلیک کنید تا مقصد را انتخاب کنید."
                    msg.contains("جستجو") || msg.contains("پیدا") -> "از تب جستجو می‌توانید هر مکانی را جستجو کنید."
                    msg.contains("ذخیره") || msg.contains("save") -> "مکان‌های ذخیره شده در تب ذخیره قابل مشاهده هستند."
                    msg.contains("صدا") || msg.contains("هشدار") -> "هشدارهای صوتی فارسی در حالت رانندگی فعال می‌شوند."
                    else -> "متوجه شدم. از تب‌های بالا برای استفاده از امکانات مختلف استفاده کنید."
                }
                aiMsg.text = "AI: $response"
                aiMsg.setPadding(16, 16, 16, 16)
                aiMsg.setBackgroundColor(0xFFE8F5E9.toInt())
                aiMsg.setTextColor(0xFF000000.toInt())
                aiMsg.textSize = 14f
                val aiParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                aiParams.setMargins(0, 8, 0, 8)
                aiMsg.layoutParams = aiParams
                chatLayout.addView(aiMsg)
                
                editText.text.clear()
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
                tts?.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        inputLayout.addView(btnSend)
        
        layout.addView(inputLayout)
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
        info.text = "\n\nمسیریاب هوشمند فارسی\nنسخه 1.0\n\nامکانات:\n✅ نقشه MapLibre\n✅ کلیک روی نقشه\n✅ مسیریابی OSRM\n✅ چند مسیر\n✅ حالت رانندگی\n✅ هشدارهای صوتی\n✅ جستجو کامل\n✅ ذخیره مکان‌ها"
        info.setPadding(16, 16, 16, 16)
        layout.addView(info)
        
        contentFrame.addView(layout)
    }
    
    private fun showError(msg: String) {
        val tv = TextView(this)
        tv.text = msg
        tv.textSize = 16f
        tv.gravity = android.view.Gravity.CENTER
        contentFrame.addView(tv)
    }
    
    override fun onResume() {
        super.onResume()
        currentMapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        currentMapView?.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        currentMapView?.onDestroy()
    }
}
