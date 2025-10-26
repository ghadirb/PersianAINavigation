package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MapFragment : Fragment() {
    
    private var mapView: MapView? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mapView = MapView(requireContext())
        mapView?.onCreate(savedInstanceState)
        
        mapView?.getMapAsync { map ->
            try {
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    val tehran = LatLng(35.6892, 51.3890)
                    map.cameraPosition = CameraPosition.Builder()
                        .target(tehran)
                        .zoom(12.0)
                        .build()
                    Toast.makeText(requireContext(), "🗺️ نقشه بارگذاری شد", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        return mapView!!
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
        mapView?.onDestroy()
        mapView = null
    }
}
