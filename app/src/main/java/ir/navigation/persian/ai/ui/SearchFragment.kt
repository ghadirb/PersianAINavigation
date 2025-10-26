package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ir.navigation.persian.ai.api.NominatimAPI
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    
    private val nominatimAPI = NominatimAPI()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(requireContext())
        title.text = "🔍 جستجوی مکان"
        title.textSize = 20f
        layout.addView(title)
        
        val editText = EditText(requireContext())
        editText.hint = "نام مکان را وارد کنید"
        layout.addView(editText)
        
        val resultText = TextView(requireContext())
        resultText.text = ""
        resultText.setPadding(16, 16, 16, 16)
        layout.addView(resultText)
        
        val btnSearch = Button(requireContext())
        btnSearch.text = "جستجو"
        btnSearch.setOnClickListener {
            val query = editText.text.toString()
            if (query.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        resultText.text = "در حال جستجو..."
                        val results = nominatimAPI.search(query, limit = 5)
                        if (results.isNotEmpty()) {
                            resultText.text = results.joinToString("\n\n") { 
                                "📍 ${it.displayName}\nLat: ${it.lat}, Lon: ${it.lon}"
                            }
                        } else {
                            resultText.text = "نتیجه‌ای یافت نشد"
                        }
                    } catch (e: Exception) {
                        resultText.text = "خطا: ${e.message}"
                    }
                }
            } else {
                Toast.makeText(requireContext(), "لطفاً نام مکان را وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(btnSearch)
        
        return layout
    }
}
