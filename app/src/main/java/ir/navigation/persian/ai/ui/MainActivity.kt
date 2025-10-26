package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ir.navigation.persian.ai.R
import ir.navigation.persian.ai.api.NominatimAPI
import ir.navigation.persian.ai.api.OSMRAPI
import ir.navigation.persian.ai.databinding.ActivityMainNavigationBinding
import ir.navigation.persian.ai.db.SavedPlaceDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import java.util.Locale

/**
 * MainActivity - مسیریاب کامل با تمام امکانات
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainNavigationBinding
    private var mapView: MapView? = null
    private var map: MapLibreMap? = null
    
    // APIs
    private val nominatimAPI = NominatimAPI()
    private val osmrAPI = OSMRAPI()
    private val database by lazy { SavedPlaceDatabase(this) }
    
    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    
    // TTS
    private var tts: TextToSpeech? = null
    private var isNavigating = false
    private var currentRoutes: List<OSMRAPI.RouteInfo> = emptyList()
    private var selectedRoute: OSMRAPI.RouteInfo? = null
    private var currentStepIndex = 0
    
    // Search
    private var searchJob: Job? = null
    private var selectedLocation: Pair<Double, Double>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupTTS()
        setupMap(savedInstanceState)
        setupUI()
        setupSearch()
        requestLocationPermission()
        
        Toast.makeText(this, "مسیریاب هوشمند آماده است ✅", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapLibreMap ->
            this.map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                // تنظیم موقعیت اولیه (تهران)
                val tehran = LatLng(35.6892, 51.3890)
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(tehran)
                    .zoom(12.0)
                    .build()
            }
        }
    }
    
    private fun setupUI() {
        // دکمه موقعیت فعلی
        binding.fabMyLocation.setOnClickListener {
            getCurrentLocation()
        }
        
        // دکمه مکان‌های ذخیره شده
        binding.fabSavedPlaces.setOnClickListener {
            showSavedPlaces()
        }
        
        // دکمه توقف مسیریابی
        binding.btnStopNavigation.setOnClickListener {
            stopNavigation()
        }
        
        // دکمه نمایش مسیرها
        binding.btnShowRoutes.setOnClickListener {
            selectedLocation?.let { (lat, lon) ->
                calculateRoutes(lat, lon)
            }
        }
        
        // دکمه ذخیره مکان
        binding.btnSaveLocation.setOnClickListener {
            saveCurrentLocation()
        }
        
        // دکمه بستن dialog
        binding.btnCloseActions.setOnClickListener {
            binding.cardLocationActions.visibility = View.GONE
        }
    }
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString()
                if (query.length >= 3) {
                    searchJob = lifecycleScope.launch {
                        delay(500) // debounce
                        searchLocation(query)
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("fa", "IR")
            }
        }
    }
    
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            getCurrentLocation()
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    map?.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(it.latitude, it.longitude))
                        .zoom(15.0)
                        .build()
                    Toast.makeText(this, "موقعیت شما یافت شد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun searchLocation(query: String) {
        lifecycleScope.launch {
            try {
                val results = nominatimAPI.search(query)
                if (results.isNotEmpty()) {
                    // TODO: نمایش نتایج در RecyclerView
                    showSearchResult(results.first())
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطا در جستجو", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showSearchResult(result: NominatimAPI.SearchResult) {
        val location = LatLng(result.lat, result.lon)
        map?.cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(15.0)
            .build()
        
        // نمایش marker
        map?.addMarker(MarkerOptions().position(location).title(result.displayName))
        
        // نمایش گزینه‌ها
        selectedLocation = Pair(result.lat, result.lon)
        binding.tvLocationName.text = result.displayName
        binding.cardLocationActions.visibility = View.VISIBLE
    }
    
    private fun calculateRoutes(endLat: Double, endLon: Double) {
        currentLocation?.let { loc ->
            lifecycleScope.launch {
                try {
                    binding.cardLocationActions.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "در حال محاسبه مسیرها...", Toast.LENGTH_SHORT).show()
                    
                    val routes = osmrAPI.getRoutes(
                        loc.latitude, loc.longitude,
                        endLat, endLon,
                        alternatives = 3
                    )
                    
                    if (routes.isNotEmpty()) {
                        currentRoutes = routes
                        showRoutesOnMap(routes)
                        speak("${routes.size} مسیر پیدا شد")
                    } else {
                        Toast.makeText(this@MainActivity, "مسیری یافت نشد", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } ?: Toast.makeText(this, "ابتدا موقعیت فعلی را مشخص کنید", Toast.LENGTH_SHORT).show()
    }
    
    private fun showRoutesOnMap(routes: List<OSMRAPI.RouteInfo>) {
        // TODO: رسم Polyline روی نقشه
        // برای الان فقط اولین مسیر را انتخاب می‌کنیم
        selectedRoute = routes.first()
        startNavigation()
    }
    
    private fun startNavigation() {
        selectedRoute?.let { route ->
            isNavigating = true
            currentStepIndex = 0
            binding.cardNavigationStatus.visibility = View.VISIBLE
            
            // نمایش اطلاعات
            binding.tvDistance.text = "فاصله: ${(route.distance / 1000).toInt()} کیلومتر"
            binding.tvEta.text = "زمان: ${(route.duration / 60).toInt()} دقیقه"
            
            // شروع هشدارهای صوتی
            speak("شروع به حرکت کنید")
            startVoiceGuidance()
        }
    }
    
    private fun startVoiceGuidance() {
        lifecycleScope.launch {
            selectedRoute?.steps?.forEachIndexed { index, step ->
                if (!isNavigating) return@launch
                
                delay(5000) // برای تست: هر 5 ثانیه یک دستور
                
                binding.tvNavigationInstruction.text = step.instruction
                binding.tvDistance.text = "فاصله: ${(step.distance).toInt()} متر"
                
                speak("${step.instruction}. در ${step.distance.toInt()} متر جلو")
            }
            
            if (isNavigating) {
                speak("به مقصد رسیدید")
                stopNavigation()
            }
        }
    }
    
    private fun stopNavigation() {
        isNavigating = false
        binding.cardNavigationStatus.visibility = View.GONE
        Toast.makeText(this, "مسیریابی متوقف شد", Toast.LENGTH_SHORT).show()
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }
    
    private fun showSavedPlaces() {
        val places = database.getAllPlaces()
        if (places.isEmpty()) {
            Toast.makeText(this, "مکانی ذخیره نشده است", Toast.LENGTH_SHORT).show()
        } else {
            // TODO: نمایش لیست
            Toast.makeText(this, "${places.size} مکان ذخیره شده", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveCurrentLocation() {
        selectedLocation?.let { (lat, lon) ->
            AlertDialog.Builder(this)
                .setTitle("ذخیره مکان")
                .setMessage("نام مکان را وارد کنید:")
                .setPositiveButton("ذخیره") { _, _ ->
                    val place = SavedPlaceDatabase.SavedPlace(
                        name = binding.tvLocationName.text.toString(),
                        latitude = lat,
                        longitude = lon
                    )
                    database.savePlace(place)
                    Toast.makeText(this, "مکان ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                    binding.cardLocationActions.visibility = View.GONE
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
    }
    
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}
