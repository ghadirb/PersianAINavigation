package ir.navigation.persian.ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import ir.navigation.persian.ai.R
import ir.navigation.persian.ai.databinding.ActivityNavigationBinding

/**
 * اکتیویتی نمایش نقشه و مسیریابی تصویری
 */
class NavigationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNavigationBinding
    private var mapView: MapView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // مقداردهی MapLibre
        Mapbox.getInstance(this)
        
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
                // نقشه آماده است
                
                // تنظیم موقعیت اولیه (تهران)
                val tehran = LatLng(35.6892, 51.3890)
                mapboxMap.cameraPosition = CameraPosition.Builder()
                    .target(tehran)
                    .zoom(12.0)
                    .build()
            }
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.fabMyLocation.setOnClickListener {
            // رفتن به موقعیت فعلی کاربر
        }
        
        binding.fabZoomIn.setOnClickListener {
            // زوم به نقشه
        }
        
        binding.fabZoomOut.setOnClickListener {
            // کاهش زوم
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
    }
}
