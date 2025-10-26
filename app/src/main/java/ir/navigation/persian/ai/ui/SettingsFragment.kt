package ir.navigation.persian.ai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        val title = TextView(requireContext())
        title.text = "⚙️ تنظیمات"
        title.textSize = 20f
        layout.addView(title)
        
        val btnKeys = Button(requireContext())
        btnKeys.text = "🔑 مدیریت کلیدهای API"
        btnKeys.setOnClickListener {
            val intent = Intent(requireContext(), UnlockActivity::class.java)
            startActivity(intent)
        }
        layout.addView(btnKeys)
        
        val info = TextView(requireContext())
        info.text = "\n\n" +
                "📱 مسیریاب هوشمند فارسی\n" +
                "نسخه 1.0\n\n" +
                "✅ نقشه: MapLibre\n" +
                "✅ جستجو: Nominatim\n" +
                "✅ مسیریابی: OSRM\n" +
                "✅ ذخیره: SQLite\n" +
                "✅ AI: آماده"
        info.textSize = 14f
        layout.addView(info)
        
        return layout
    }
}
