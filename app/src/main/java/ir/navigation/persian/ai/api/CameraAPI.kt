package ir.navigation.persian.ai.api

import ir.navigation.persian.ai.model.SpeedCamera
import ir.navigation.persian.ai.model.CameraType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

/**
 * API برای دریافت دوربین‌های کنترل سرعت
 * از منابع عمومی و کاربران
 */
class CameraAPI {
    
    /**
     * دریافت دوربین‌های اطراف یک نقطه
     * @param lat عرض جغرافیایی
     * @param lon طول جغرافیایی
     * @param radius شعاع جستجو (کیلومتر)
     */
    suspend fun getCamerasNearby(
        lat: Double,
        lon: Double,
        radius: Double = 50.0
    ): List<SpeedCamera> = withContext(Dispatchers.IO) {
        try {
            // استفاده از API عمومی OpenStreetMap
            // برای دوربین‌های ثبت شده توسط کاربران
            val url = "https://overpass-api.de/api/interpreter?data=" +
                    "[out:json];node[\"highway\"=\"speed_camera\"]" +
                    "(around:${radius * 1000},$lat,$lon);out;"
            
            val response = URL(url).readText()
            val json = org.json.JSONObject(response)
            val elements = json.getJSONArray("elements")
            
            val cameras = mutableListOf<SpeedCamera>()
            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject("tags")
                
                cameras.add(
                    SpeedCamera(
                        id = "osm_${element.getLong("id")}",
                        latitude = element.getDouble("lat"),
                        longitude = element.getDouble("lon"),
                        type = CameraType.FIXED_CAMERA,
                        speedLimit = tags?.optInt("maxspeed", 100) ?: 100,
                        direction = tags?.optDouble("direction"),
                        verified = true
                    )
                )
            }
            
            cameras
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * دریافت تمام دوربین‌های ایران از دیتابیس محلی
     * این دیتابیس باید به صورت دوره‌ای آپدیت شود
     */
    fun getAllIranCameras(): List<SpeedCamera> {
        return listOf(
            // تهران
            SpeedCamera("teh_1", 35.7580, 51.4089, CameraType.FIXED_CAMERA, 110, 90.0, true),
            SpeedCamera("teh_2", 35.7520, 51.4150, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            SpeedCamera("teh_3", 35.6892, 51.3890, CameraType.FIXED_CAMERA, 100, 180.0, true),
            SpeedCamera("teh_4", 35.6850, 51.3950, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("teh_5", 35.7100, 51.3500, CameraType.FIXED_CAMERA, 90, 270.0, true),
            SpeedCamera("teh_6", 35.6500, 51.4200, CameraType.AVERAGE_SPEED_CAMERA, 110, 45.0, true),
            SpeedCamera("teh_7", 35.7300, 51.4800, CameraType.FIXED_CAMERA, 100, 90.0, true),
            SpeedCamera("teh_8", 35.6997, 51.3380, CameraType.TRAFFIC_LIGHT, 50, null, true),
            SpeedCamera("teh_9", 35.7800, 51.0500, CameraType.AVERAGE_SPEED_CAMERA, 120, 270.0, true),
            SpeedCamera("teh_10", 35.7600, 51.4200, CameraType.FIXED_CAMERA, 90, 0.0, true),
            
            // مشهد
            SpeedCamera("msh_1", 36.2974, 59.6067, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("msh_2", 36.3100, 59.5800, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            SpeedCamera("msh_3", 36.2800, 59.6200, CameraType.FIXED_CAMERA, 90, 180.0, true),
            
            // اصفهان
            SpeedCamera("isf_1", 32.6546, 51.6680, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("isf_2", 32.6700, 51.6500, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            SpeedCamera("isf_3", 32.6400, 51.6800, CameraType.FIXED_CAMERA, 90, 180.0, true),
            
            // شیراز
            SpeedCamera("shz_1", 29.5918, 52.5836, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("shz_2", 29.6100, 52.5600, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            
            // تبریز
            SpeedCamera("tbz_1", 38.0800, 46.2919, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("tbz_2", 38.0900, 46.3100, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            
            // کرج
            SpeedCamera("krj_1", 35.8327, 50.9916, CameraType.FIXED_CAMERA, 100, 0.0, true),
            SpeedCamera("krj_2", 35.8400, 51.0000, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
            
            // اهواز
            SpeedCamera("ahv_1", 31.3183, 48.6706, CameraType.FIXED_CAMERA, 100, 0.0, true),
            
            // قم
            SpeedCamera("qom_1", 34.6416, 50.8746, CameraType.FIXED_CAMERA, 100, 0.0, true),
            
            // کرمان
            SpeedCamera("krm_1", 30.2839, 57.0834, CameraType.FIXED_CAMERA, 100, 0.0, true),
            
            // رشت
            SpeedCamera("rsh_1", 37.2808, 49.5832, CameraType.FIXED_CAMERA, 100, 0.0, true),
            
            // آزادراه تهران-قم
            SpeedCamera("hwy_1", 35.5000, 51.2000, CameraType.AVERAGE_SPEED_CAMERA, 120, 180.0, true),
            SpeedCamera("hwy_2", 35.3000, 51.1000, CameraType.FIXED_CAMERA, 120, 0.0, true),
            
            // آزادراه تهران-کرج
            SpeedCamera("hwy_3", 35.7500, 51.1000, CameraType.AVERAGE_SPEED_CAMERA, 120, 270.0, true),
            SpeedCamera("hwy_4", 35.8000, 51.0500, CameraType.FIXED_CAMERA, 120, 90.0, true),
            
            // آزادراه تهران-ساوه
            SpeedCamera("hwy_5", 35.6000, 50.8000, CameraType.AVERAGE_SPEED_CAMERA, 120, 270.0, true),
            
            // سرعت‌گیرها
            SpeedCamera("bump_1", 35.7000, 51.4000, CameraType.SPEED_BUMP, 40, null, true),
            SpeedCamera("bump_2", 35.6800, 51.3800, CameraType.SPEED_BUMP, 40, null, true),
            SpeedCamera("bump_3", 35.7200, 51.4200, CameraType.SPEED_BUMP, 40, null, true)
        )
    }
    
    /**
     * فیلتر دوربین‌ها بر اساس نوع
     */
    fun filterByType(cameras: List<SpeedCamera>, type: CameraType): List<SpeedCamera> {
        return cameras.filter { it.type == type }
    }
    
    /**
     * محاسبه فاصله تا دوربین (متر)
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // شعاع زمین به متر
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
