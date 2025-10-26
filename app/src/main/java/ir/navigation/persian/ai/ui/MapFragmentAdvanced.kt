package ir.navigation.persian.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

/**
 * MapFragment پیشرفته با امکانات اضافی
 */
class MapFragmentAdvanced : Fragment() {
    
    private var mapView: MapView? = null
    private var map: MapLibreMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Main layout
        val mainLayout = FrameLayout(requireContext())
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        // Create map
        mapView = MapView(requireContext())
        mapView?.onCreate(savedInstanceState)
        mainLayout.addView(mapView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        
        // Add FAB button for location
        val fabLocation = FloatingActionButton(requireContext())
        fabLocation.setImageResource(android.R.drawable.ic_menu_mylocation)
        fabLocation.setOnClickListener { goToMyLocation() }
        
        val fabParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        fabParams.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
        fabParams.setMargins(0, 0, 48, 48)
        mainLayout.addView(fabLocation, fabParams)
        
        // Setup map
        setupMap(savedInstanceState)
        
        return mainLayout
    }
    
    private fun setupMap(savedInstanceState: Bundle?) {
        mapView?.getMapAsync { mapLibreMap ->
            this.map = mapLibreMap
            
            try {
                mapLibreMap.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                    // تهران
                    val tehran = LatLng(35.6892, 51.3890)
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(tehran)
                        .zoom(12.0)
                        .build()
                    
                    Toast.makeText(requireContext(), "🗺️ نقشه آماده است", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "خطا در بارگذاری نقشه", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun goToMyLocation() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }
        
        // Get location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                
                // Move camera to current location
                map?.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(15.0)
                    .build()
                
                Toast.makeText(
                    requireContext(), 
                    "📍 موقعیت شما: ${location.latitude}, ${location.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "موقعیت یافت نشد", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            goToMyLocation()
        }
    }
    
    // Lifecycle methods
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
