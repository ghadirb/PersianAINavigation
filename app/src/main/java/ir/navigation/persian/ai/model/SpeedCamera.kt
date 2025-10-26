package ir.navigation.persian.ai.model

import com.google.gson.annotations.SerializedName

/**
 * مدل دوربین کنترل سرعت
 */
data class SpeedCamera(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("type")
    val type: CameraType,
    
    @SerializedName("speed_limit")
    val speedLimit: Int, // km/h
    
    @SerializedName("direction")
    val direction: Double? = null, // جهت دوربین در درجه
    
    @SerializedName("verified")
    val verified: Boolean = false,
    
    @SerializedName("last_update")
    val lastUpdate: Long = System.currentTimeMillis()
)

enum class CameraType {
    @SerializedName("fixed")
    FIXED_CAMERA,           // دوربین ثابت
    
    @SerializedName("mobile")
    MOBILE_CAMERA,          // دوربین سیار
    
    @SerializedName("speed_bump")
    SPEED_BUMP,             // سرعت‌گیر
    
    @SerializedName("traffic_light")
    TRAFFIC_LIGHT,          // چراغ راهنمایی با دوربین
    
    @SerializedName("average_speed")
    AVERAGE_SPEED_CAMERA    // دوربین میانگین سرعت
}

/**
 * پیغام هشدار صوتی
 */
data class VoiceAlert(
    val message: String,
    val distance: Int,  // متر
    val priority: AlertPriority
)

enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
