package ir.navigation.persian.ai.model

import com.google.gson.annotations.SerializedName

/**
 * داده‌های مسیر برای یادگیری ماشین
 */
data class RouteData(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("start_lat")
    val startLat: Double,
    
    @SerializedName("start_lon")
    val startLon: Double,
    
    @SerializedName("end_lat")
    val endLat: Double,
    
    @SerializedName("end_lon")
    val endLon: Double,
    
    @SerializedName("route_points")
    val routePoints: List<RoutePoint>,
    
    @SerializedName("traffic_data")
    val trafficData: TrafficData,
    
    @SerializedName("time_taken")
    val timeTaken: Long, // میلی‌ثانیه
    
    @SerializedName("distance")
    val distance: Double, // متر
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("day_of_week")
    val dayOfWeek: Int, // 1-7 (یکشنبه تا شنبه)
    
    @SerializedName("hour_of_day")
    val hourOfDay: Int, // 0-23
    
    @SerializedName("user_selected")
    val userSelected: Boolean = false // آیا کاربر این مسیر را انتخاب کرده؟
)

data class RoutePoint(
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("speed")
    val speed: Float, // km/h
    
    @SerializedName("timestamp")
    val timestamp: Long
)

data class TrafficData(
    @SerializedName("avg_speed")
    val averageSpeed: Float, // km/h
    
    @SerializedName("congestion_level")
    val congestionLevel: CongestionLevel,
    
    @SerializedName("incidents")
    val incidents: List<TrafficIncident> = emptyList()
)

enum class CongestionLevel {
    @SerializedName("free_flow")
    FREE_FLOW,
    
    @SerializedName("light")
    LIGHT,
    
    @SerializedName("moderate")
    MODERATE,
    
    @SerializedName("heavy")
    HEAVY,
    
    @SerializedName("severe")
    SEVERE
}

data class TrafficIncident(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("severity")
    val severity: Int // 1-5
)

/**
 * نتیجه پیش‌بینی مسیر توسط مدل
 */
data class RoutePrediction(
    val routes: List<PredictedRoute>,
    val confidence: Float
)

data class PredictedRoute(
    val routePoints: List<RoutePoint>,
    val estimatedTime: Long, // میلی‌ثانیه
    val score: Float, // امتیاز پیشنهاد (0-1)
    val reason: String // دلیل پیشنهاد
)
