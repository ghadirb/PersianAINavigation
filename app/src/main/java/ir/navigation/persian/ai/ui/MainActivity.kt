package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.navigation.persian.ai.R
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * MainActivity - صفحه اصلی (بدون UnlockActivity)
 */
class MainActivity : AppCompatActivity() {
    
    private var mapView: MapView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        
        try {
            setupMap(savedInstanceState)
            Toast.makeText(this, "✅ مسیریاب هوشمند آماده است", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { map ->
            try {
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    // تنظیم موقعیت اولیه (تهران)
                    val tehran = LatLng(35.6892, 51.3890)
                    map.cameraPosition = CameraPosition.Builder()
                        .target(tehran)
                        .zoom(12.0)
                        .build()
                    
                    Toast.makeText(this, "🗺️ نقشه بارگذاری شد", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "خطا در بارگذاری نقشه: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
