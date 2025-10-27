package ir.navigation.persian.ai.model

/**
 * مدل هشدار پلیس راه
 */
data class PoliceAlert(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val type: PoliceType,
    val reportTime: Long = System.currentTimeMillis(),
    val reportedBy: String = "user",
    val verified: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + (30 * 60 * 1000) // 30 دقیقه
)

enum class PoliceType(val displayName: String, val icon: String) {
    TRAFFIC_POLICE("پلیس راهور", "👮"),
    HIGHWAY_POLICE("پلیس راه", "🚔"),
    SPEED_TRAP("کنترل سرعت", "📷"),
    CHECKPOINT("ایست بازرسی", "🛑"),
    ACCIDENT("تصادف", "🚨")
}

/**
 * سیستم گزارش پلیس توسط کاربران (مانند Waze)
 */
data class UserReport(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val type: ReportType,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 0,
    val downvotes: Int = 0
)

enum class ReportType(val displayName: String, val icon: String) {
    POLICE("پلیس", "👮"),
    ACCIDENT("تصادف", "🚨"),
    HAZARD("خطر", "⚠️"),
    TRAFFIC("ترافیک", "🚗"),
    ROAD_CLOSED("راه بسته", "🚧"),
    CAMERA("دوربین", "📷"),
    SPEED_BUMP("سرعت‌گیر", "⚠️")
}
