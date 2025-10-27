package ir.navigation.persian.ai.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * موتور TTS آنلاین
 * کیفیت بالاتر - نیاز به اینترنت
 */
class OnlineTTSEngine(private val context: Context) {
    
    private val client = OkHttpClient()
    private var isInitialized = false
    
    companion object {
        private const val TAG = "OnlineTTSEngine"
        // API های رایگان TTS فارسی
        private const val TTS_API_URL = "https://api.text-to-speech.com/v1/synthesize" // نمونه
    }
    
    /**
     * مقداردهی
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            // بررسی اتصال اینترنت
            if (isNetworkAvailable()) {
                isInitialized = true
                Log.d(TAG, "Online TTS initialized")
                return@withContext true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize online TTS", e)
            false
        }
    }
    
    /**
     * تبدیل متن به گفتار آنلاین
     */
    suspend fun synthesize(text: String, speed: Float = 1.0f): Boolean = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            Log.e(TAG, "Engine not initialized")
            return@withContext false
        }
        
        try {
            // ساخت درخواست JSON
            val json = JSONObject().apply {
                put("text", text)
                put("language", "fa")
                put("speed", speed)
                put("voice", "female") // یا male
            }
            
            val requestBody = json.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(TTS_API_URL)
                .post(requestBody)
                .build()
            
            // ارسال درخواست
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val audioData = response.body?.bytes()
                    if (audioData != null) {
                        playAudio(audioData)
                        return@withContext true
                    }
                }
            }
            
            false
        } catch (e: IOException) {
            Log.e(TAG, "Network error during synthesis", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Synthesis failed", e)
            false
        }
    }
    
    /**
     * پخش صدا
     */
    private fun playAudio(audioData: ByteArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(22050)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(audioData.size)
                .build()
            
            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio", e)
        }
    }
    
    /**
     * بررسی اتصال اینترنت
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && 
                (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR))
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        isInitialized = false
    }
    
    /**
     * بررسی آماده بودن
     */
    fun isReady(): Boolean = isInitialized && isNetworkAvailable()
}
