package ir.navigation.persian.ai.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * موتور TTS هوشمند با پشتیبانی از 3 حالت
 * - Android TTS (آفلاین - پیش‌فرض)
 * - Sherpa ONNX (آفلاین - کیفیت بالا)
 * - Online API (آنلاین - کیفیت بالاتر)
 */
class SmartTTSEngine(private val context: Context) {
    
    private var androidTTS: TextToSpeech? = null
    private var sherpaEngine: SherpaONNXEngine? = null
    private var onlineEngine: OnlineTTSEngine? = null
    
    private var currentMode = TTSMode.OFFLINE_ANDROID
    private var settings = TTSSettings()
    private var isInitialized = false
    
    companion object {
        private const val TAG = "SmartTTSEngine"
    }
    
    /**
     * مقداردهی با حالت انتخابی
     */
    suspend fun initialize(mode: TTSMode = TTSMode.OFFLINE_ANDROID): Boolean = withContext(Dispatchers.IO) {
        try {
            currentMode = mode
            
            when (mode) {
                TTSMode.OFFLINE_ANDROID -> {
                    initializeAndroidTTS()
                }
                TTSMode.OFFLINE_ONNX -> {
                    sherpaEngine = SherpaONNXEngine(context)
                    val success = sherpaEngine?.initialize() ?: false
                    if (!success) {
                        Log.w(TAG, "Sherpa ONNX failed, falling back to Android TTS")
                        currentMode = TTSMode.OFFLINE_ANDROID
                        initializeAndroidTTS()
                    }
                }
                TTSMode.ONLINE_API -> {
                    onlineEngine = OnlineTTSEngine(context)
                    val success = onlineEngine?.initialize() ?: false
                    if (!success) {
                        Log.w(TAG, "Online TTS failed, falling back to Android TTS")
                        currentMode = TTSMode.OFFLINE_ANDROID
                        initializeAndroidTTS()
                    }
                }
            }
            
            isInitialized = true
            Log.d(TAG, "Smart TTS initialized with mode: $currentMode")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Smart TTS", e)
            false
        }
    }
    
    /**
     * مقداردهی Android TTS
     */
    private suspend fun initializeAndroidTTS() = withContext(Dispatchers.Main) {
        androidTTS = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                androidTTS?.language = Locale("fa", "IR")
                androidTTS?.setSpeechRate(settings.speed)
                androidTTS?.setPitch(settings.pitch)
                Log.d(TAG, "Android TTS initialized")
            }
        }
    }
    
    /**
     * تبدیل متن به گفتار
     */
    suspend fun synthesize(text: String, speed: Float = 1.0f): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Engine not initialized")
            return false
        }
        
        return try {
            when (currentMode) {
                TTSMode.OFFLINE_ANDROID -> {
                    synthesizeWithAndroid(text, speed)
                }
                TTSMode.OFFLINE_ONNX -> {
                    val success = sherpaEngine?.synthesize(text, speed) ?: false
                    if (!success && settings.autoSwitch) {
                        Log.w(TAG, "Sherpa failed, using Android TTS")
                        synthesizeWithAndroid(text, speed)
                    } else {
                        success
                    }
                }
                TTSMode.ONLINE_API -> {
                    val success = onlineEngine?.synthesize(text, speed) ?: false
                    if (!success && settings.autoSwitch) {
                        Log.w(TAG, "Online TTS failed, using Android TTS")
                        synthesizeWithAndroid(text, speed)
                    } else {
                        success
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Synthesis failed", e)
            false
        }
    }
    
    /**
     * استفاده از Android TTS
     */
    private suspend fun synthesizeWithAndroid(text: String, speed: Float): Boolean = withContext(Dispatchers.Main) {
        try {
            androidTTS?.setSpeechRate(speed)
            androidTTS?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Android TTS failed", e)
            false
        }
    }
    
    /**
     * تغییر حالت TTS
     */
    suspend fun switchMode(newMode: TTSMode): Boolean {
        if (newMode == currentMode) return true
        
        Log.d(TAG, "Switching TTS mode from $currentMode to $newMode")
        release()
        return initialize(newMode)
    }
    
    /**
     * تنظیم settings
     */
    fun updateSettings(newSettings: TTSSettings) {
        settings = newSettings
        androidTTS?.setSpeechRate(newSettings.speed)
        androidTTS?.setPitch(newSettings.pitch)
    }
    
    /**
     * دریافت حالت فعلی
     */
    fun getCurrentMode(): TTSMode = currentMode
    
    /**
     * بررسی آماده بودن
     */
    fun isReady(): Boolean {
        return when (currentMode) {
            TTSMode.OFFLINE_ANDROID -> androidTTS != null
            TTSMode.OFFLINE_ONNX -> sherpaEngine?.isReady() ?: false
            TTSMode.ONLINE_API -> onlineEngine?.isReady() ?: false
        }
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        androidTTS?.stop()
        androidTTS?.shutdown()
        androidTTS = null
        
        sherpaEngine?.release()
        sherpaEngine = null
        
        onlineEngine?.release()
        onlineEngine = null
        
        isInitialized = false
    }
}
