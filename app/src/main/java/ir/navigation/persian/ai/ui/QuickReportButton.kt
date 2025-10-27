package ir.navigation.persian.ai.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import org.json.JSONObject
import java.io.File

class QuickReportButton(context: Context) : LinearLayout(context) {
    
    init {
        orientation = HORIZONTAL
        
        // دکمه دوربین
        val btnCamera = Button(context).apply {
            text = "📷 دوربین"
            setOnClickListener { report("camera") }
        }
        addView(btnCamera)
        
        // دکمه سرعت‌گیر
        val btnBump = Button(context).apply {
            text = "⚠️ سرعت‌گیر"
            setOnClickListener { report("bump") }
        }
        addView(btnBump)
        
        // دکمه پلیس
        val btnPolice = Button(context).apply {
            text = "👮 پلیس"
            setOnClickListener { report("police") }
        }
        addView(btnPolice)
    }
    
    private fun report(type: String) {
        // ذخیره محلی
        val file = File(context.filesDir, "reports.json")
        val data = JSONObject().apply {
            put("type", type)
            put("lat", 0.0) // از GPS
            put("lon", 0.0)
            put("time", System.currentTimeMillis())
        }
        file.appendText(data.toString() + "\n")
        
        Toast.makeText(context, "✅ گزارش ثبت شد", Toast.LENGTH_SHORT).show()
    }
}
