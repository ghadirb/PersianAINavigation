package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ir.navigation.persian.ai.db.SavedPlaceDatabase

class SavedFragment : Fragment() {
    
    private lateinit var database: SavedPlaceDatabase
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        database = SavedPlaceDatabase(requireContext())
        
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(requireContext())
        title.text = "📍 مکان‌های ذخیره شده"
        title.textSize = 20f
        layout.addView(title)
        
        val places = database.getAllPlaces()
        
        if (places.isEmpty()) {
            val empty = TextView(requireContext())
            empty.text = "\n\nهیچ مکانی ذخیره نشده است"
            empty.textSize = 16f
            layout.addView(empty)
        } else {
            places.forEach { place ->
                val placeView = TextView(requireContext())
                placeView.text = "\n${place.name}\nLat: ${place.latitude}, Lon: ${place.longitude}"
                placeView.textSize = 14f
                placeView.setPadding(16, 16, 16, 16)
                layout.addView(placeView)
            }
        }
        
        val btnAdd = Button(requireContext())
        btnAdd.text = "➕ افزودن مکان تست"
        btnAdd.setOnClickListener {
            val place = SavedPlaceDatabase.SavedPlace(
                name = "مکان تستی ${System.currentTimeMillis()}",
                latitude = 35.6892,
                longitude = 51.3890
            )
            database.savePlace(place)
            Toast.makeText(requireContext(), "✅ ذخیره شد", Toast.LENGTH_SHORT).show()
        }
        layout.addView(btnAdd)
        
        return layout
    }
}
