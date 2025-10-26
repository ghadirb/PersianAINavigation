package ir.navigation.persian.ai.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import ai.onnxruntime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * موتور تبدیل متن به گفتار فارسی با استفاده از ONNX Runtime
 * بر اساس مدل Coqui-TTS VITS
 */
class PersianTTSEngine(private val context: Context) {
    
    private var ortSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null
    private var isInitialized = false
    
    companion object {
        private const val MODEL_NAME = "persian_tts_model.onnx"
        private const val SAMPLE_RATE = 22050
        
        // نقشه حروف فارسی به ID
        private val PERSIAN_CHAR_MAP = mapOf(
            ' ' to 0, 'آ' to 1, 'ا' to 2, 'ب' to 3, 'پ' to 4, 'ت' to 5, 'ث' to 6,
            'ج' to 7, 'چ' to 8, 'ح' to 9, 'خ' to 10, 'د' to 11, 'ذ' to 12, 'ر' to 13,
            'ز' to 14, 'ژ' to 15, 'س' to 16, 'ش' to 17, 'ص' to 18, 'ض' to 19, 'ط' to 20,
            'ظ' to 21, 'ع' to 22, 'غ' to 23, 'ف' to 24, 'ق' to 25, 'ک' to 26, 'گ' to 27,
            'ل' to 28, 'م' to 29, 'ن' to 30, 'و' to 31, 'ه' to 32, 'ی' to 33,
            '0' to 34, '1' to 35, '2' to 36, '3' to 37, '4' to 38, '5' to 39,
            '6' to 40, '7' to 41, '8' to 42, '9' to 43, '،' to 44, '.' to 45
        )
    }
    
    /**
     * مقداردهی اولیه موتور TTS
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()
            
            // بارگذاری مدل از assets
            val modelBytes = context.assets.open(MODEL_NAME).use { it.readBytes() }
            
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            sessionOptions.setIntraOpNumThreads(4)
            
            ortSession = ortEnvironment?.createSession(modelBytes, sessionOptions)
            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * تبدیل متن فارسی به گفتار
     */
    suspend fun synthesize(text: String, speed: Float = 1.0f): Boolean = withContext(Dispatchers.IO) {
        if (!isInitialized || ortSession == null) {
            return@withContext false
        }
        
        try {
            // تبدیل متن به توکن‌ها
            val tokens = textToTokens(text)
            
            // آماده‌سازی ورودی‌های مدل
            val textIds = tokens.map { it.toLong() }.toLongArray()
            val textLength = longArrayOf(textIds.size.toLong())
            val scales = floatArrayOf(speed, 0.667f, 0.8f) // noise_scale, length_scale, noise_w
            
            // ایجاد تنسورها
            val textTensor = OnnxTensor.createTensor(
                ortEnvironment,
                LongBuffer.wrap(textIds),
                longArrayOf(1, textIds.size.toLong())
            )
            
            val lengthTensor = OnnxTensor.createTensor(
                ortEnvironment,
                LongBuffer.wrap(textLength),
                longArrayOf(1)
            )
            
            val scalesTensor = OnnxTensor.createTensor(
                ortEnvironment,
                FloatBuffer.wrap(scales),
                longArrayOf(3)
            )
            
            // اجرای مدل
            val inputs = mapOf(
                "input" to textTensor,
                "input_lengths" to lengthTensor,
                "scales" to scalesTensor
            )
            
            val outputs = ortSession?.run(inputs)
            
            // دریافت خروجی صدا
            val audioTensor = outputs?.get(0) as? OnnxTensor
            val audioData = audioTensor?.floatBuffer?.array()
            
            // پخش صدا
            if (audioData != null) {
                playAudio(audioData)
            }
            
            // آزادسازی منابع
            textTensor.close()
            lengthTensor.close()
            scalesTensor.close()
            outputs?.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * تبدیل متن به توکن‌های عددی
     */
    private fun textToTokens(text: String): List<Int> {
        val tokens = mutableListOf<Int>()
        tokens.add(0) // SOS token
        
        text.forEach { char ->
            val token = PERSIAN_CHAR_MAP[char] ?: PERSIAN_CHAR_MAP[' ']
            token?.let { tokens.add(it) }
        }
        
        tokens.add(0) // EOS token
        return tokens
    }
    
    /**
     * پخش صدای تولید شده
     */
    private fun playAudio(audioData: FloatArray) {
        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        audioTrack.play()
        audioTrack.write(audioData, 0, audioData.size, AudioTrack.WRITE_BLOCKING)
        audioTrack.stop()
        audioTrack.release()
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        ortSession?.close()
        ortSession = null
        isInitialized = false
    }
    
    /**
     * تولید متن‌های هشدار فارسی
     */
    object AlertMessages {
        fun speedCameraAlert(distance: Int, speedLimit: Int): String {
            return when {
                distance > 1000 -> "توجه، در یک کیلومتر جلوتر دوربین کنترل سرعت با محدودیت $speedLimit کیلومتر بر ساعت"
                distance > 500 -> "توجه، در ${distance} متر جلوتر دوربین کنترل سرعت، سرعت مجاز $speedLimit"
                distance > 200 -> "هشدار، دوربین کنترل سرعت در ${distance} متری، سرعت خود را کاهش دهید"
                else -> "دوربین کنترل سرعت، سرعت مجاز $speedLimit کیلومتر"
            }
        }
        
        fun speedBumpAlert(distance: Int): String {
            return when {
                distance > 200 -> "توجه، در ${distance} متر جلوتر سرعت‌گیر"
                distance > 100 -> "هشدار، سرعت‌گیر در ${distance} متری"
                else -> "سرعت‌گیر، سرعت خود را کاهش دهید"
            }
        }
        
        fun speedLimitAlert(currentSpeed: Int, speedLimit: Int): String {
            val excess = currentSpeed - speedLimit
            return "هشدار، سرعت شما $currentSpeed است. سرعت مجاز $speedLimit. لطفا $excess کیلومتر کاهش دهید"
        }
        
        fun turnAlert(direction: String, distance: Int, streetName: String?): String {
            val distanceText = when {
                distance > 1000 -> "در یک کیلومتر"
                distance > 500 -> "در ${distance} متر"
                else -> "در ${distance} متر"
            }
            
            val street = if (streetName.isNullOrEmpty()) "" else " به سمت $streetName"
            return "$distanceText $direction بپیچید$street"
        }
        
        fun destinationAlert(distance: Int): String {
            return when {
                distance > 1000 -> "در یک کیلومتر به مقصد می‌رسید"
                distance > 500 -> "در ${distance} متر به مقصد می‌رسید"
                else -> "شما به مقصد رسیده‌اید"
            }
        }
        
        fun recalculatingRoute(): String {
            return "در حال محاسبه مجدد مسیر"
        }
        
        fun offRoute(): String {
            return "شما از مسیر خارج شده‌اید. در حال محاسبه مسیر جدید"
        }
    }
}
