package ir.navigation.persian.ai.tts

/**
 * حالت‌های TTS
 */
enum class TTSMode {
    OFFLINE_ANDROID,    // Android TTS (آفلاین - پیش‌فرض)
    OFFLINE_ONNX,       // Sherpa ONNX (آفلاین - کیفیت بالا)
    ONLINE_API          // API آنلاین (کیفیت بالاتر)
}

/**
 * تنظیمات TTS
 */
data class TTSSettings(
    val mode: TTSMode = TTSMode.OFFLINE_ANDROID,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f,
    val autoSwitch: Boolean = true  // تبدیل خودکار به آفلاین در صورت قطع اینترنت
)
