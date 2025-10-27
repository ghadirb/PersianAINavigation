package ir.navigation.persian.ai.crowdsource

import android.content.Context
import org.json.JSONObject
import java.io.File

class DriveHelper(private val context: Context) {
    
    // لینک پوشه: https://drive.google.com/drive/folders/1bp1Ay9kmK_bjWq_PznRfkPvhhjdhSye1
    
    fun saveReport(type: String, lat: Double, lon: Double) {
        val report = JSONObject().apply {
            put("type", type)
            put("lat", lat)
            put("lon", lon)
            put("time", System.currentTimeMillis())
        }
        
        // ذخیره محلی
        val file = File(context.filesDir, "reports.json")
        file.appendText(report.toString() + "\n")
    }
    
    // کاربران می‌توانند فایل را دستی به Drive آپلود کنند
    fun getReportsFile(): File {
        return File(context.filesDir, "reports.json")
    }
}
