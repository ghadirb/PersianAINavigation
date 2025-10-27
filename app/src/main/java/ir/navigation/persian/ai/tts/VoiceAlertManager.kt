package ir.navigation.persian.ai.tts

import android.content.Context
import android.util.Log
import ir.navigation.persian.ai.model.CameraType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * مدیریت تمام هشدارهای صوتی فارسی
 * پشتیبانی از حالت آفلاین (TTS) و آنلاین (API)
 */
class VoiceAlertManager(private val context: Context) {
    
    private val ttsEngine = PersianTTSEngine(context)
    private var isInitialized = false
    private var isMuted = false
    
    companion object {
        private const val TAG = "VoiceAlertManager"
    }
    
    /**
     * مقداردهی اولیه
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = ttsEngine.initialize()
            Log.d(TAG, "Voice Alert Manager initialized: $isInitialized")
        }
    }
    
    /**
     * هشدار دوربین کنترل سرعت
     */
    fun alertSpeedCamera(distance: Int, speedLimit: Int, cameraType: CameraType) {
        if (isMuted || !isInitialized) return
        
        val message = when (cameraType) {
            CameraType.FIXED_CAMERA -> {
                when {
                    distance > 1000 -> "توجه، یک کیلومتر جلوتر دوربین ثابت کنترل سرعت با محدودیت $speedLimit کیلومتر بر ساعت"
                    distance > 500 -> "در ${distance} متر جلوتر دوربین ثابت کنترل سرعت قرار دارد. سرعت مجاز $speedLimit"
                    distance > 200 -> "هشدار! دوربین ثابت در ${distance} متری. لطفا سرعت خود را به $speedLimit کاهش دهید"
                    else -> "دوربین کنترل سرعت. سرعت مجاز $speedLimit کیلومتر"
                }
            }
            
            CameraType.MOBILE_CAMERA -> {
                when {
                    distance > 500 -> "احتمال وجود دوربین سیار در ${distance} متری. سرعت مجاز $speedLimit"
                    distance > 200 -> "هشدار! دوربین سیار احتمالی در ${distance} متر جلوتر"
                    else -> "دوربین سیار. سرعت مجاز $speedLimit"
                }
            }
            
            CameraType.AVERAGE_SPEED_CAMERA -> {
                when {
                    distance > 1000 -> "ورود به محدوده دوربین میانگین سرعت. سرعت مجاز $speedLimit"
                    distance > 500 -> "در ${distance} متر وارد محدوده کنترل میانگین سرعت می‌شوید"
                    else -> "توجه! شما در محدوده کنترل میانگین سرعت هستید. سرعت مجاز $speedLimit"
                }
            }
            
            CameraType.TRAFFIC_LIGHT -> {
                when {
                    distance > 200 -> "چراغ راهنمایی با دوربین در ${distance} متری"
                    else -> "چراغ قرمز را رعایت کنید. دوربین فعال است"
                }
            }
            
            else -> "دوربین کنترل سرعت در ${distance} متری. سرعت مجاز $speedLimit"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار سرعت‌گیر
     */
    fun alertSpeedBump(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 300 -> "توجه! سرعت‌گیر در ${distance} متر جلوتر"
            distance > 150 -> "هشدار! سرعت‌گیر در ${distance} متری. لطفا سرعت خود را کاهش دهید"
            distance > 50 -> "سرعت‌گیر نزدیک است! سرعت را کم کنید"
            else -> "سرعت‌گیر! سرعت خود را به شدت کاهش دهید"
        }
        
        playAlert(message, speed = 1.2f)
    }
    
    /**
     * هشدار تخطی از سرعت مجاز
     */
    fun alertSpeedLimitViolation(currentSpeed: Int, speedLimit: Int) {
        if (isMuted || !isInitialized) return
        
        val excess = currentSpeed - speedLimit
        
        val message = when {
            excess > 30 -> "هشدار جدی! سرعت شما $currentSpeed است. سرعت مجاز $speedLimit. لطفا فوراً سرعت را کاهش دهید"
            excess > 20 -> "هشدار! سرعت $currentSpeed، سرعت مجاز $speedLimit. $excess کیلومتر کاهش دهید"
            excess > 10 -> "توجه! سرعت شما $excess کیلومتر بیش از حد مجاز است"
            else -> "سرعت شما $currentSpeed است. سرعت مجاز $speedLimit"
        }
        
        playAlert(message, speed = 1.3f)
    }
    
    /**
     * هشدار پیچ خطرناک
     */
    fun alertDangerousTurn(distance: Int, turnDirection: String) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 500 -> "توجه! پیچ خطرناک به سمت $turnDirection در ${distance} متر جلوتر"
            distance > 200 -> "هشدار! پیچ تند $turnDirection در ${distance} متری. سرعت را کاهش دهید"
            distance > 50 -> "پیچ خطرناک! به سمت $turnDirection بپیچید و سرعت را کم کنید"
            else -> "پیچ تند! با احتیاط به سمت $turnDirection بپیچید"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار ورود ممنوع
     */
    fun alertNoEntry(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 300 -> "توجه! در ${distance} متر جلوتر ورود ممنوع است. مسیر خود را تغییر دهید"
            distance > 100 -> "هشدار! ورود به این مسیر ممنوع است. سریعاً مسیر را تغییر دهید"
            else -> "خطر! ورود ممنوع. بلافاصله مسیر خود را عوض کنید"
        }
        
        playAlert(message, speed = 1.4f)
    }
    
    /**
     * هشدار راه بندان یا گذرگاه
     */
    fun alertRailwayCrossing(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 300 -> "راه آهن در ${distance} متر جلوتر. با احتیاط عبور کنید"
            distance > 100 -> "هشدار! راه بندان در ${distance} متری"
            else -> "راه آهن! با دقت عبور کنید"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار منطقه مدرسه
     */
    fun alertSchoolZone(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 200 -> "ورود به منطقه مدرسه. سرعت مجاز ۳۰ کیلومتر"
            else -> "منطقه مدرسه. به کودکان توجه کنید"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار کار راهداری
     */
    fun alertRoadWork(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 500 -> "کار راهداری در ${distance} متر جلوتر. با احتیاط رانندگی کنید"
            distance > 200 -> "هشدار! کار راهداری در ${distance} متری"
            else -> "کار راهداری. سرعت را کاهش دهید"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار تصادف یا حادثه
     */
    fun alertAccident(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 1000 -> "گزارش تصادف در یک کیلومتر جلوتر. با احتیاط رانندگی کنید"
            distance > 500 -> "حادثه در ${distance} متر جلوتر. سرعت را کم کنید"
            distance > 200 -> "هشدار! حادثه در ${distance} متری"
            else -> "حادثه در پیش رو. کاملاً سرعت را کاهش دهید"
        }
        
        playAlert(message, speed = 1.1f)
    }
    
    /**
     * هشدار ترافیک سنگین
     */
    fun alertHeavyTraffic(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 1000 -> "ترافیک سنگین در یک کیلومتر جلوتر"
            distance > 500 -> "ورود به ترافیک سنگین در ${distance} متری"
            else -> "ترافیک سنگین. صبور باشید"
        }
        
        playAlert(message)
    }
    
    /**
     * دستورات پیچ برای مسیریابی
     */
    fun alertTurn(direction: String, distance: Int, streetName: String? = null) {
        if (isMuted || !isInitialized) return
        
        val distanceText = when {
            distance > 1000 -> "در یک کیلومتر"
            distance > 500 -> "در ${distance} متر"
            distance > 200 -> "در ${distance} متر"
            distance > 50 -> "در ${distance} متر"
            else -> "اکنون"
        }
        
        val street = if (streetName.isNullOrEmpty()) "" else " به سمت $streetName"
        val message = "$distanceText به $direction بپیچید$street"
        
        playAlert(message)
    }
    
    /**
     * هشدار نزدیک شدن به مقصد
     */
    fun alertApproachingDestination(distance: Int) {
        if (isMuted || !isInitialized) return
        
        val message = when {
            distance > 1000 -> "در یک کیلومتر به مقصد می‌رسید"
            distance > 500 -> "در ${distance} متر به مقصد خواهید رسید"
            distance > 100 -> "نزدیک مقصد هستید. ${distance} متر مانده"
            else -> "شما به مقصد رسیده‌اید"
        }
        
        playAlert(message)
    }
    
    /**
     * هشدار خروج از مسیر
     */
    fun alertOffRoute() {
        if (isMuted || !isInitialized) return
        playAlert("شما از مسیر خارج شده‌اید. در حال محاسبه مسیر جدید", speed = 1.2f)
    }
    
    /**
     * هشدار محاسبه مجدد مسیر
     */
    fun alertRecalculating() {
        if (isMuted || !isInitialized) return
        playAlert("در حال محاسبه مسیر جدید")
    }
    
    /**
     * پخش هشدار صوتی
     */
    private fun playAlert(message: String, speed: Float = 1.0f) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                Log.d(TAG, "Playing alert: $message")
                ttsEngine.synthesize(message, speed)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing alert", e)
            }
        }
    }
    
    /**
     * پخش هشدار عمومی (public)
     */
    fun playAlert(message: String) {
        playAlert(message, 1.0f)
    }
    
    /**
     * قطع/وصل صدا
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        Log.d(TAG, "Voice alerts ${if (muted) "muted" else "unmuted"}")
    }
    
    /**
     * بررسی وضعیت صدا
     */
    fun isMuted(): Boolean = isMuted
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        ttsEngine.release()
        isInitialized = false
    }
}
