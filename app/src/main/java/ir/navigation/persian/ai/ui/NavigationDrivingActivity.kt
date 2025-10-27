package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import ir.navigation.persian.ai.api.OSMRAPI
import ir.navigation.persian.ai.ml.AIRouteLearning
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import java.util.Locale

/**
 * حالت رانندگی - Navigation با هشدارهای صوتی
 */
class NavigationDrivingActivity : AppCompatActivity() {
    
    private var mapView: MapView? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var tts: TextToSpeech? = null
    private val osmrAPI = OSMRAPI()
    private lateinit var aiLearning: AIRouteLearning
    private var currentRoute: OSMRAPI.RouteInfo? = null
    private var currentStepIndex = 0
    private var isNavigating = false
    
    // UI
    private lateinit var tvInstruction: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedLimit: TextView
    private lateinit var btnStop: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val destLat = intent.getDoubleExtra("destLat", 35.6997)
        val destLon = intent.getDoubleExtra("destLon", 51.3380)
        val destName = intent.getStringExtra("destName") ?: "مقصد"
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("fa", "IR") }
        aiLearning = AIRouteLearning(this)
        aiLearning.startRecording(intent.getDoubleExtra("startLat", 0.0), intent.getDoubleExtra("startLon", 0.0), destLat, destLon)
        
        setupUI(destLat, destLon, destName)
        startNavigation(destLat, destLon)
    }
    
    private fun setupUI(destLat: Double, destLon: Double, destName: String) {
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        
        // Map
        val mapFrame = FrameLayout(this)
        mapView = MapView(this)
        mapView?.onCreate(null)
        mapFrame.addView(mapView)
        
        // Top Info Panel
        val infoPanel = LinearLayout(this)
        infoPanel.orientation = LinearLayout.VERTICAL
        infoPanel.setBackgroundColor(0xCC000000.toInt())
        infoPanel.setPadding(32, 32, 32, 32)
        
        tvInstruction = TextView(this)
        tvInstruction.text = "در حال محاسبه مسیر..."
        tvInstruction.textSize = 20f
        tvInstruction.setTextColor(0xFFFFFFFF.toInt())
        infoPanel.addView(tvInstruction)
        
        tvDistance = TextView(this)
        tvDistance.text = "فاصله: -- کیلومتر"
        tvDistance.textSize = 16f
        tvDistance.setTextColor(0xFFFFFFFF.toInt())
        infoPanel.addView(tvDistance)
        
        val infoParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        infoParams.topMargin = 16
        mapFrame.addView(infoPanel, infoParams)
        
        // Speed Display
        val speedPanel = LinearLayout(this)
        speedPanel.orientation = LinearLayout.VERTICAL
        speedPanel.setBackgroundColor(0xCC000000.toInt())
        speedPanel.setPadding(24, 24, 24, 24)
        
        tvSpeed = TextView(this)
        tvSpeed.text = "0"
        tvSpeed.textSize = 48f
        tvSpeed.setTextColor(0xFFFFFFFF.toInt())
        speedPanel.addView(tvSpeed)
        
        tvSpeedLimit = TextView(this)
        tvSpeedLimit.text = "محدودیت: 80"
        tvSpeedLimit.textSize = 14f
        tvSpeedLimit.setTextColor(0xFFFFFF00.toInt())
        speedPanel.addView(tvSpeedLimit)
        
        val speedParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        speedParams.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
        speedParams.setMargins(16, 0, 0, 100)
        mapFrame.addView(speedPanel, speedParams)
        
        mainLayout.addView(mapFrame, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        // Stop Button
        btnStop = Button(this)
        btnStop.text = "⏹ توقف مسیریابی"
        btnStop.setOnClickListener { finish() }
        mainLayout.addView(btnStop)
        
        setContentView(mainLayout)
        
        // Setup Map
        mapView?.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                map.addMarker(MarkerOptions()
                    .position(LatLng(destLat, destLon))
                    .title("🏁 $destName"))
            }
        }
    }
    
    private fun startNavigation(destLat: Double, destLon: Double) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { loc ->
                lifecycleScope.launch {
                    try {
                        val routes = osmrAPI.getRoutes(loc.latitude, loc.longitude, destLat, destLon, 1)
                        if (routes.isNotEmpty()) {
                            currentRoute = routes[0]
                            isNavigating = true
                            speak("مسیریابی آغاز شد")
                            startLocationUpdates()
                            startVoiceGuidance()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@NavigationDrivingActivity, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { updateLocation(it) }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }
    
    private fun updateLocation(location: Location) {
        aiLearning.addWaypoint(location)
        val speed = location.speed * 3.6 // m/s to km/h
        tvSpeed.text = speed.toInt().toString()
        
        // Check speed limit
        val speedLimit = 80 // TODO: Get from road data
        if (speed > speedLimit) {
            tvSpeed.setTextColor(0xFFFF0000.toInt())
            speak("سرعت شما بیش از حد مجاز است")
        } else {
            tvSpeed.setTextColor(0xFFFFFFFF.toInt())
        }
        
        // Update camera
        mapView?.getMapAsync { map ->
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(location.latitude, location.longitude))
                .zoom(17.0)
                .bearing(location.bearing.toDouble())
                .build()
        }
    }
    
    private fun startVoiceGuidance() {
        lifecycleScope.launch {
            currentRoute?.steps?.forEach { step ->
                if (!isNavigating) return@launch
                
                tvInstruction.text = step.instruction
                tvDistance.text = "فاصله: ${(step.distance/1000).toInt()} کیلومتر"
                speak(step.instruction)
                
                kotlinx.coroutines.delay(10000) // 10 seconds between instructions
            }
            
            if (isNavigating) {
                speak("به مقصد رسیدید")
                aiLearning.finishRecording(0.9f)
                finish()
            }
        }
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isNavigating = false
        tts?.stop()
        tts?.shutdown()
        mapView?.onDestroy()
    }
}
