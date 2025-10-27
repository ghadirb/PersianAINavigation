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
import org.maplibre.android.MapLibre
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
        
        // مقداردهی اولیه MapLibre (قبل از استفاده از MapView)
        MapLibre.getInstance(this)
        
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
            
            // نوار اطلاعات بالا (مانند نشان و Google Maps)
            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.VERTICAL
            topBar.setBackgroundColor(0xEE2196F3.toInt()) // آبی مانند نشان
            topBar.setPadding(16, 32, 16, 16)
            
            // سرعت فعلی
            speedTextView = TextView(this)
            speedTextView?.text = "🚗 0 km/h"
            speedTextView?.textSize = 24f
            speedTextView?.setTextColor(0xFFFFFFFF.toInt())
            speedTextView?.gravity = android.view.Gravity.CENTER
            topBar.addView(speedTextView)
            
            // اطلاعات مسیر
            val routeInfo = TextView(this)
            routeInfo.text = "📍 آماده مسیریابی"
            routeInfo.textSize = 14f
            routeInfo.setTextColor(0xFFFFFFFF.toInt())
            routeInfo.gravity = android.view.Gravity.CENTER
            topBar.addView(routeInfo)
            
            val topParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            topParams.gravity = android.view.Gravity.TOP
            frame.addView(topBar, topParams)
            
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
        layout.setBackgroundColor(0xFFF5F5F5.toInt())
        
        // نوار جستجو (مانند نشان)
        val searchBar = LinearLayout(this)
        searchBar.orientation = LinearLayout.HORIZONTAL
        searchBar.setBackgroundColor(0xFFFFFFFF.toInt())
        searchBar.setPadding(16, 32, 16, 16)
        searchBar.elevation = 8f
        
        val editText = EditText(this)
        editText.hint = "🔍 جستجو در تمام دنیا..."
        editText.textSize = 16f
        editText.setPadding(16, 16, 16, 16)
        editText.setBackgroundColor(0xFFF0F0F0.toInt())
        searchBar.addView(editText, LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        
        val btnSearch = Button(this)
        btnSearch.text = "🔍"
        btnSearch.setBackgroundColor(0xFF2196F3.toInt())
        btnSearch.setTextColor(0xFFFFFFFF.toInt())
        searchBar.addView(btnSearch)
        
        layout.addView(searchBar)
        
        // راهنما
        val hint = TextView(this)
        hint.text = "💡 می‌توانید هر مکانی در دنیا را جستجو کنید:\n• میدان آزادی تهران\n• برج ایفل پاریس\n• تایمز اسکوئر نیویورک"
        hint.textSize = 12f
        hint.setTextColor(0xFF666666.toInt())
        hint.setPadding(32, 16, 32, 16)
        layout.addView(hint)
        
        val scrollView = ScrollView(this)
        val resultLayout = LinearLayout(this)
        resultLayout.orientation = LinearLayout.VERTICAL
        resultLayout.setPadding(16, 16, 16, 16)
        scrollView.addView(resultLayout)
        layout.addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
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
                            buttonLayout.setPadding(0, 16, 0, 0)
                            
                            val btnShow = Button(this@MainActivityFull)
                            btnShow.text = "🗺️ نمایش"
                            btnShow.setBackgroundColor(0xFF4CAF50.toInt())
                            btnShow.setTextColor(0xFFFFFFFF.toInt())
                            btnShow.setOnClickListener {
                                tabLayout.selectTab(tabLayout.getTabAt(0))
                                showLocationOnMap(LatLng(place.lat.toDouble(), place.lon.toDouble()), place.displayName)
                            }
                            val btnShowParams = LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                            )
                            btnShowParams.setMargins(0, 0, 8, 0)
                            buttonLayout.addView(btnShow, btnShowParams)
                            
                            val btnRoute = Button(this@MainActivityFull)
                            btnRoute.text = "🚗 مسیریابی"
                            btnRoute.setBackgroundColor(0xFF2196F3.toInt())
                            btnRoute.setTextColor(0xFFFFFFFF.toInt())
                            btnRoute.setOnClickListener {
                                showRouteOptions(LatLng(place.lat.toDouble(), place.lon.toDouble()), place.displayName)
                            }
                            val btnRouteParams = LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                            )
                            btnRouteParams.setMargins(4, 0, 4, 0)
                            buttonLayout.addView(btnRoute, btnRouteParams)
                            
                            val btnSave = Button(this@MainActivityFull)
                            btnSave.text = "💾 ذخیره"
                            btnSave.setBackgroundColor(0xFFFF9800.toInt())
                            btnSave.setTextColor(0xFFFFFFFF.toInt())
                            btnSave.setOnClickListener {
                                database.addPlace(place.displayName, place.lat.toDouble(), place.lon.toDouble())
                                Toast.makeText(this@MainActivityFull, "✅ ذخیره شد در تب ذخیره", Toast.LENGTH_SHORT).show()
                                tts?.speak("ذخیره شد", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                            val btnSaveParams = LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                            )
                            btnSaveParams.setMargins(8, 0, 0, 0)
                            buttonLayout.addView(btnSave, btnSaveParams)
                            
                            placeCard.addView(buttonLayout)
                            
                            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(0, 0, 0, 16)
                            resultLayout.addView(placeCard, params)
                        }
                        
                        tts?.speak("${results.size} نتیجه یافت شد", TextToSpeech.QUEUE_FLUSH, null, null)
                    } catch (e: Exception) {
                        resultLayout.removeAllViews()
                        val errorMsg = TextView(this@MainActivityFull)
                        errorMsg.text = "⚠️ خطا در جستجو\n\n${e.message}\n\nلطفاً اتصال اینترنت را بررسی کنید"
                        errorMsg.textSize = 16f
                        errorMsg.gravity = android.view.Gravity.CENTER
                        errorMsg.setPadding(32, 64, 32, 64)
                        errorMsg.setTextColor(0xFFFF5722.toInt())
                        resultLayout.addView(errorMsg)
                    }
                }
            } else {
                Toast.makeText(this@MainActivityFull, "لطفاً نام مکان را وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }
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
        layout.setBackgroundColor(0xFFF5F5F5.toInt())
        
        // هدر
        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.setBackgroundColor(0xFF2196F3.toInt())
        header.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "📍 مکان‌های ذخیره شده"
        title.textSize = 22f
        title.setTextColor(0xFFFFFFFF.toInt())
        header.addView(title)
        
        val subtitle = TextView(this)
        subtitle.text = "مکان‌های مورد علاقه شما"
        subtitle.textSize = 14f
        subtitle.setTextColor(0xFFE3F2FD.toInt())
        header.addView(subtitle)
        
        layout.addView(header)
        
        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL
        
        val places = database.getAllPlaces()
        
        if (places.isEmpty()) {
            val emptyMsg = TextView(this)
            emptyMsg.text = "📭 هنوز مکانی ذخیره نکرده‌اید\n\nاز تب جستجو مکان‌های مورد علاقه را ذخیره کنید"
            emptyMsg.textSize = 16f
            emptyMsg.gravity = android.view.Gravity.CENTER
            emptyMsg.setPadding(32, 64, 32, 64)
            emptyMsg.setTextColor(0xFF666666.toInt())
            listLayout.addView(emptyMsg)
        }
        
        places.forEach { place ->
            val placeCard = LinearLayout(this)
            placeCard.orientation = LinearLayout.VERTICAL
            placeCard.setPadding(24, 24, 24, 24)
            placeCard.setBackgroundColor(0xFFFFFFFF.toInt())
            placeCard.elevation = 4f
            
            val placeName = TextView(this)
            placeName.text = "📍 ${place.name}"
            placeName.textSize = 18f
            placeName.setTextColor(0xFF000000.toInt())
            placeCard.addView(placeName)
            
            val placeCoords = TextView(this)
            placeCoords.text = "${place.latitude}, ${place.longitude}"
            placeCoords.textSize = 12f
            placeCoords.setTextColor(0xFF666666.toInt())
            placeCard.addView(placeCoords)
            
            val buttonLayout = LinearLayout(this)
            buttonLayout.orientation = LinearLayout.HORIZONTAL
            buttonLayout.setPadding(0, 16, 0, 0)
            
            val btnShow = Button(this)
            btnShow.text = "🗺️ نمایش"
            btnShow.setBackgroundColor(0xFF4CAF50.toInt())
            btnShow.setTextColor(0xFFFFFFFF.toInt())
            btnShow.setOnClickListener {
                tabLayout.selectTab(tabLayout.getTabAt(0))
                showLocationOnMap(LatLng(place.latitude, place.longitude), place.name)
            }
            val btnShowParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            btnShowParams.setMargins(0, 0, 8, 0)
            buttonLayout.addView(btnShow, btnShowParams)
            
            val btnRoute = Button(this)
            btnRoute.text = "🚗 مسیریابی"
            btnRoute.setBackgroundColor(0xFF2196F3.toInt())
            btnRoute.setTextColor(0xFFFFFFFF.toInt())
            btnRoute.setOnClickListener {
                showRouteOptions(LatLng(place.latitude, place.longitude), place.name)
            }
            val btnRouteParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            btnRouteParams.setMargins(4, 0, 4, 0)
            buttonLayout.addView(btnRoute, btnRouteParams)
            
            val btnDelete = Button(this)
            btnDelete.text = "🗑️ حذف"
            btnDelete.setBackgroundColor(0xFFF44336.toInt())
            btnDelete.setTextColor(0xFFFFFFFF.toInt())
            btnDelete.setOnClickListener {
                database.deletePlace(place.id)
                Toast.makeText(this, "❌ حذف شد", Toast.LENGTH_SHORT).show()
                showSavedTab() // رفرش
            }
            val btnDeleteParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            btnDeleteParams.setMargins(8, 0, 0, 0)
            buttonLayout.addView(btnDelete, btnDeleteParams)
            
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
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(this)
        title.text = "⚙️ تنظیمات و مدیریت"
        title.textSize = 22f
        title.setTextColor(0xFF000000.toInt())
        layout.addView(title)
        
        // دکمه مدیریت کلیدها
        val btnUnlock = Button(this)
        btnUnlock.text = "🔑 مدیریت کلیدهای API"
        btnUnlock.setOnClickListener {
            startActivity(Intent(this, UnlockActivity::class.java))
        }
        val btnUnlockParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnUnlockParams.setMargins(0, 16, 0, 8)
        layout.addView(btnUnlock, btnUnlockParams)
        
        // توضیحات کلیدها
        val keysInfo = TextView(this)
        keysInfo.text = "برای استفاده از امکانات کامل، کلیدهای API را وارد کنید:\n• Google Maps API\n• OpenAI API\n• Neshan API"
        keysInfo.setPadding(16, 8, 16, 16)
        keysInfo.setTextColor(0xFF666666.toInt())
        keysInfo.textSize = 14f
        layout.addView(keysInfo)
        
        // خط جداکننده
        val divider1 = View(this)
        divider1.setBackgroundColor(0xFFCCCCCC.toInt())
        val dividerParams1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2
        )
        dividerParams1.setMargins(0, 16, 0, 16)
        layout.addView(divider1, dividerParams1)
        
        // اطلاعات برنامه
        val appInfo = TextView(this)
        appInfo.text = "📱 مسیریاب هوشمند فارسی\n🔢 نسخه 1.0.0\n👨‍💻 توسعه با Kotlin\n🗺️ MapLibre GL"
        appInfo.textSize = 16f
        appInfo.setTextColor(0xFF000000.toInt())
        appInfo.setPadding(16, 16, 16, 16)
        layout.addView(appInfo)
        
        // خط جداکننده
        val divider2 = View(this)
        divider2.setBackgroundColor(0xFFCCCCCC.toInt())
        val dividerParams2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2
        )
        dividerParams2.setMargins(0, 16, 0, 16)
        layout.addView(divider2, dividerParams2)
        
        // امکانات
        val featuresTitle = TextView(this)
        featuresTitle.text = "✨ امکانات برنامه:"
        featuresTitle.textSize = 18f
        featuresTitle.setTextColor(0xFF000000.toInt())
        layout.addView(featuresTitle)
        
        val features = TextView(this)
        features.text = """
            ✅ نقشه تعاملی MapLibre
            ✅ کلیک روی نقشه برای انتخاب مقصد
            ✅ مسیریابی با OSRM
            ✅ نمایش چند مسیر مختلف
            ✅ حالت رانندگی با GPS
            ✅ هشدارهای صوتی فارسی
            ✅ 3 حالت TTS (Android/ONNX/Online)
            ✅ دوربین‌های سرعت تهران
            ✅ هشدار سرعت‌گیر
            ✅ تشخیص تخطی از سرعت
            ✅ جستجوی مکان با Nominatim
            ✅ ذخیره مکان‌های مورد علاقه
            ✅ دستیار هوشمند AI
            ✅ یادگیری مسیرها با AI
            ✅ اشتراک‌گذاری با Google Drive
        """.trimIndent()
        features.textSize = 14f
        features.setTextColor(0xFF333333.toInt())
        features.setPadding(16, 8, 16, 16)
        layout.addView(features)
        
        // خط جداکننده
        val divider3 = View(this)
        divider3.setBackgroundColor(0xFFCCCCCC.toInt())
        val dividerParams3 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2
        )
        dividerParams3.setMargins(0, 16, 0, 16)
        layout.addView(divider3, dividerParams3)
        
        // راهنما
        val helpTitle = TextView(this)
        helpTitle.text = "📖 راهنمای استفاده:"
        helpTitle.textSize = 18f
        helpTitle.setTextColor(0xFF000000.toInt())
        layout.addView(helpTitle)
        
        val help = TextView(this)
        help.text = """
            🗺️ نقشه: روی نقشه کلیک کنید تا مقصد را انتخاب کنید
            🔍 جستجو: نام مکان را جستجو کنید
            📍 ذخیره: مکان‌های ذخیره شده را مشاهده کنید
            🤖 AI: با دستیار هوشمند چت کنید
            ⚙️ سایر: تنظیمات و مدیریت کلیدها
        """.trimIndent()
        help.textSize = 14f
        help.setTextColor(0xFF333333.toInt())
        help.setPadding(16, 8, 16, 16)
        layout.addView(help)
        
        scrollView.addView(layout)
        contentFrame.addView(scrollView)
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
    
    override fun onBackPressed() {
        // دیالوگ خروج مانند نشان
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
        dialog.setTitle("🚗 خروج از برنامه")
        dialog.setMessage("آیا می‌خواهید از مسیریاب خارج شوید؟")
        dialog.setPositiveButton("خروج") { _, _ ->
            tts?.speak("خداحافظ", TextToSpeech.QUEUE_FLUSH, null, null)
            finishAffinity()
        }
        dialog.setNegativeButton("ادامه", null)
        dialog.show()
    }
}
