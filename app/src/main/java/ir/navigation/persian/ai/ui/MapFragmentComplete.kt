package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ir.navigation.persian.ai.api.OSMRAPI
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.plugins.annotation.LineManager
import java.util.Locale

class MapFragmentComplete : Fragment() {
    
    private var mapView: MapView? = null
    private var map: MapLibreMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var tts: TextToSpeech? = null
    private val osmrAPI = OSMRAPI()
    
    // UI Elements
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabZoomIn: FloatingActionButton
    private lateinit var fabZoomOut: FloatingActionButton
    private lateinit var btnStartNavigation: Button
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedLimit: TextView
    private lateinit var tvNavigationInfo: TextView
    
    // Navigation state
    private var isNavigating = false
    private var currentRoute: OSMRAPI.RouteInfo? = null
    private var currentStepIndex = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainLayout = FrameLayout(requireContext())
        
        // Map
        mapView = MapView(requireContext())
        mapView?.onCreate(savedInstanceState)
        mainLayout.addView(mapView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        
        // UI Overlay
        createUIOverlay(mainLayout)
        
        // Setup
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupTTS()
        setupMap(savedInstanceState)
        requestLocationPermission()
        
        return mainLayout
    }
    
    private fun createUIOverlay(parent: FrameLayout) {
        val context = requireContext()
        
        // Speed Display (top left)
        val speedLayout = LinearLayout(context)
        speedLayout.orientation = LinearLayout.VERTICAL
        speedLayout.setBackgroundColor(0xCC000000.toInt())
        speedLayout.setPadding(16, 16, 16, 16)
        
        tvSpeed = TextView(context)
        tvSpeed.text = "0 km/h"
        tvSpeed.textSize = 24f
        tvSpeed.setTextColor(0xFFFFFFFF.toInt())
        speedLayout.addView(tvSpeed)
        
        tvSpeedLimit = TextView(context)
        tvSpeedLimit.text = "محدودیت: 80 km/h"
        tvSpeedLimit.textSize = 14f
        tvSpeedLimit.setTextColor(0xFFFFFF00.toInt())
        speedLayout.addView(tvSpeedLimit)
        
        val speedParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        speedParams.topMargin = 16
        speedParams.leftMargin = 16
        parent.addView(speedLayout, speedParams)
        
        // Navigation Info (top center)
        tvNavigationInfo = TextView(context)
        tvNavigationInfo.text = "در حال آماده‌سازی..."
        tvNavigationInfo.textSize = 16f
        tvNavigationInfo.setTextColor(0xFFFFFFFF.toInt())
        tvNavigationInfo.setBackgroundColor(0xCC0066CC.toInt())
        tvNavigationInfo.setPadding(32, 16, 32, 16)
        tvNavigationInfo.visibility = View.GONE
        
        val navInfoParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        navInfoParams.topMargin = 16
        navInfoParams.gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
        parent.addView(tvNavigationInfo, navInfoParams)
        
        // FAB Buttons (right side)
        val fabLayout = LinearLayout(context)
        fabLayout.orientation = LinearLayout.VERTICAL
        
        fabMyLocation = FloatingActionButton(context)
        fabMyLocation.setImageResource(android.R.drawable.ic_menu_mylocation)
        fabMyLocation.setOnClickListener { goToMyLocation() }
        fabLayout.addView(fabMyLocation)
        
        fabZoomIn = FloatingActionButton(context)
        fabZoomIn.setImageResource(android.R.drawable.ic_input_add)
        fabZoomIn.setOnClickListener { zoomIn() }
        fabLayout.addView(fabZoomIn)
        
        fabZoomOut = FloatingActionButton(context)
        fabZoomOut.setImageResource(android.R.drawable.ic_delete)
        fabZoomOut.setOnClickListener { zoomOut() }
        fabLayout.addView(fabZoomOut)
        
        val fabParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        fabParams.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
        fabParams.rightMargin = 16
        parent.addView(fabLayout, fabParams)
        
        // Start Navigation Button (bottom)
        btnStartNavigation = Button(context)
        btnStartNavigation.text = "🚗 شروع مسیریابی"
        btnStartNavigation.setOnClickListener { startNavigationDemo() }
        
        val btnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        btnParams.gravity = android.view.Gravity.BOTTOM
        btnParams.setMargins(16, 0, 16, 16)
        parent.addView(btnStartNavigation, btnParams)
    }
    
    private fun setupMap(savedInstanceState: Bundle?) {
        mapView?.getMapAsync { mapLibreMap ->
            this.map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                val tehran = LatLng(35.6892, 51.3890)
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(tehran)
                    .zoom(13.0)
                    .build()
                
                // Add sample markers
                addSampleMarkers(mapLibreMap)
                
                Toast.makeText(requireContext(), "🗺️ نقشه آماده است", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addSampleMarkers(map: MapLibreMap) {
        // دوربین
        map.addMarker(MarkerOptions()
            .position(LatLng(35.7000, 51.4000))
            .title("📷 دوربین")
            .snippet("دوربین ثبت تخلف"))
        
        // سرعت‌گیر
        map.addMarker(MarkerOptions()
            .position(LatLng(35.6800, 51.3800))
            .title("🚨 سرعت‌گیر")
            .snippet("محدودیت: 60 km/h"))
    }
    
    private fun setupTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("fa", "IR")
            }
        }
    }
    
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            goToMyLocation()
        }
    }
    
    private fun goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    map?.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(it.latitude, it.longitude))
                        .zoom(15.0)
                        .build()
                    
                    // Update speed
                    val speed = it.speed * 3.6 // m/s to km/h
                    tvSpeed.text = "${speed.toInt()} km/h"
                    
                    Toast.makeText(requireContext(), "📍 موقعیت شما", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun zoomIn() {
        map?.cameraPosition?.let {
            map?.cameraPosition = CameraPosition.Builder()
                .target(it.target)
                .zoom(it.zoom + 1)
                .build()
        }
    }
    
    private fun zoomOut() {
        map?.cameraPosition?.let {
            map?.cameraPosition = CameraPosition.Builder()
                .target(it.target)
                .zoom(it.zoom - 1)
                .build()
        }
    }
    
    private fun startNavigationDemo() {
        if (isNavigating) {
            stopNavigation()
            return
        }
        
        currentLocation?.let { loc ->
            lifecycleScope.launch {
                try {
                    btnStartNavigation.isEnabled = false
                    Toast.makeText(requireContext(), "در حال محاسبه مسیر...", Toast.LENGTH_SHORT).show()
                    
                    // مقصد نمونه (میدان آزادی تهران)
                    val destination = LatLng(35.6997, 51.3380)
                    
                    val routes = osmrAPI.getRoutes(
                        loc.latitude, loc.longitude,
                        destination.latitude, destination.longitude,
                        alternatives = 3
                    )
                    
                    if (routes.isNotEmpty()) {
                        currentRoute = routes.first()
                        drawRouteOnMap(routes)
                        startNavigationMode()
                        speak("مسیریابی آغاز شد. ${routes.size} مسیر یافت شد")
                    } else {
                        Toast.makeText(requireContext(), "مسیری یافت نشد", Toast.LENGTH_SHORT).show()
                    }
                    
                    btnStartNavigation.isEnabled = true
                } catch (e: Exception) {
                    btnStartNavigation.isEnabled = true
                    Toast.makeText(requireContext(), "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "ابتدا موقعیت فعلی را مشخص کنید", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun drawRouteOnMap(routes: List<OSMRAPI.RouteInfo>) {
        // TODO: Draw polylines
        Toast.makeText(requireContext(), "${routes.size} مسیر روی نقشه", Toast.LENGTH_SHORT).show()
    }
    
    private fun startNavigationMode() {
        isNavigating = true
        btnStartNavigation.text = "⏹ توقف مسیریابی"
        tvNavigationInfo.visibility = View.VISIBLE
        
        currentRoute?.let { route ->
            tvNavigationInfo.text = "فاصله: ${(route.distance/1000).toInt()} کیلومتر | زمان: ${(route.duration/60).toInt()} دقیقه"
            
            // Start voice guidance
            lifecycleScope.launch {
                route.steps.forEach { step ->
                    if (!isNavigating) return@launch
                    
                    tvNavigationInfo.text = "${step.instruction}\nفاصله: ${step.distance.toInt()} متر"
                    speak(step.instruction)
                    
                    kotlinx.coroutines.delay(8000) // 8 seconds between instructions
                }
                
                if (isNavigating) {
                    speak("به مقصد رسیدید")
                    stopNavigation()
                }
            }
        }
    }
    
    private fun stopNavigation() {
        isNavigating = false
        btnStartNavigation.text = "🚗 شروع مسیریابی"
        tvNavigationInfo.visibility = View.GONE
        Toast.makeText(requireContext(), "مسیریابی متوقف شد", Toast.LENGTH_SHORT).show()
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        tts?.stop()
        tts?.shutdown()
        mapView?.onDestroy()
        mapView = null
    }
}
