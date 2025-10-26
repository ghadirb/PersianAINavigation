package ir.navigation.persian.ai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import ir.navigation.persian.ai.R
import ir.navigation.persian.ai.model.*
import ir.navigation.persian.ai.tts.PersianTTSEngine
import ir.navigation.persian.ai.tts.VoiceAlertManager
import ir.navigation.persian.ai.ml.RouteLearningEngine
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * سرویس پیش‌زمینه برای مسیریابی و ردیابی موقعیت
 */
class NavigationService : Service() {
    
    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var ttsEngine: PersianTTSEngine
    private lateinit var voiceAlertManager: VoiceAlertManager
    private lateinit var routeLearningEngine: RouteLearningEngine
    
    private var currentLocation: Location? = null
    private var routePoints = mutableListOf<RoutePoint>()
    private var speedCameras = mutableListOf<SpeedCamera>()
    private var isNavigating = false
    private var currentSpeedLimit = 0
    private var routeStartTime: Long = 0
    
    private var navigationListener: NavigationListener? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "navigation_channel"
        private const val LOCATION_UPDATE_INTERVAL = 1000L // 1 ثانیه
        private const val CAMERA_ALERT_DISTANCE = 500 // متر
        private const val SPEED_BUMP_ALERT_DISTANCE = 200 // متر
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): NavigationService = this@NavigationService
    }
    
    override fun onCreate() {
        super.onCreate()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ttsEngine = PersianTTSEngine(this)
        voiceAlertManager = VoiceAlertManager(this)
        routeLearningEngine = RouteLearningEngine(this)
        
        serviceScope.launch {
            ttsEngine.initialize()
            voiceAlertManager.initialize()
            routeLearningEngine.initialize()
        }
        
        createNotificationChannel()
        setupLocationCallback()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("در حال مسیریابی...")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    
    /**
     * شروع مسیریابی
     */
    fun startNavigation(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        cameras: List<SpeedCamera>
    ) {
        if (isNavigating) return
        
        isNavigating = true
        speedCameras.clear()
        speedCameras.addAll(cameras)
        routePoints.clear()
        routeStartTime = System.currentTimeMillis()
        
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL
            ).apply {
                setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL / 2)
                setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
            }.build()
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            
            updateNotification("مسیریابی فعال است")
            
            serviceScope.launch {
                // پیش‌بینی بهترین مسیر با استفاده از ML
                val prediction = routeLearningEngine.predictBestRoute(
                    startLat, startLon, endLat, endLon,
                    getCurrentHour(), getCurrentDayOfWeek()
                )
                
                prediction?.let {
                    navigationListener?.onRoutePredicted(it)
                }
            }
            
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * توقف مسیریابی
     */
    fun stopNavigation() {
        if (!isNavigating) return
        
        isNavigating = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        // ذخیره داده‌های مسیر برای یادگیری
        serviceScope.launch {
            saveRouteData()
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    /**
     * تنظیم callback برای به‌روزرسانی‌های مسیریابی
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation = location
                    
                    // افزودن نقطه به مسیر
                    val routePoint = RoutePoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speed = location.speed * 3.6f, // m/s به km/h
                        timestamp = System.currentTimeMillis()
                    )
                    routePoints.add(routePoint)
                    
                    // بررسی سرعت
                    checkSpeed(location)
                    
                    // بررسی دوربین‌ها و سرعت‌گیرها
                    checkSpeedCameras(location)
                    
                    // اطلاع‌رسانی به listener
                    navigationListener?.onLocationUpdate(location)
                }
            }
        }
    }
    
    /**
     * بررسی سرعت و هشدار در صورت تخطی
     */
    private fun checkSpeed(location: Location) {
        if (currentSpeedLimit == 0) return
        
        val currentSpeed = (location.speed * 3.6).toInt() // km/h
        
        if (currentSpeed > currentSpeedLimit + 10) {
            serviceScope.launch {
                val message = PersianTTSEngine.AlertMessages.speedLimitAlert(
                    currentSpeed, currentSpeedLimit
                )
                ttsEngine.synthesize(message, 1.2f)
                
                navigationListener?.onSpeedLimitExceeded(currentSpeed, currentSpeedLimit)
            }
        }
    }
    
    /**
     * بررسی نزدیکی به دوربین یا سرعت‌گیر
     */
    private fun checkSpeedCameras(location: Location) {
        speedCameras.forEach { camera ->
            val distance = calculateDistance(
                location.latitude, location.longitude,
                camera.latitude, camera.longitude
            )
            
            val distanceMeters = (distance * 1000).toInt()
            
            when (camera.type) {
                CameraType.FIXED_CAMERA, CameraType.AVERAGE_SPEED_CAMERA -> {
                    if (distanceMeters in 1..CAMERA_ALERT_DISTANCE) {
                        serviceScope.launch {
                            val message = PersianTTSEngine.AlertMessages.speedCameraAlert(
                                distanceMeters, camera.speedLimit
                            )
                            ttsEngine.synthesize(message)
                            
                            navigationListener?.onCameraAlert(camera, distanceMeters)
                        }
                        
                        // حذف دوربین از لیست تا دوباره هشدار نده
                        speedCameras.remove(camera)
                    }
                }
                
                CameraType.SPEED_BUMP -> {
                    if (distanceMeters in 1..SPEED_BUMP_ALERT_DISTANCE) {
                        serviceScope.launch {
                            val message = PersianTTSEngine.AlertMessages.speedBumpAlert(distanceMeters)
                            ttsEngine.synthesize(message)
                            
                            navigationListener?.onSpeedBumpAlert(distanceMeters)
                        }
                        
                        speedCameras.remove(camera)
                    }
                }
                
                else -> {}
            }
        }
    }
    
    /**
     * ذخیره داده‌های مسیر
     */
    private suspend fun saveRouteData() {
        if (routePoints.size < 2) return
        
        val firstPoint = routePoints.first()
        val lastPoint = routePoints.last()
        
        val routeData = RouteData(
            id = "route_${System.currentTimeMillis()}",
            startLat = firstPoint.latitude,
            startLon = firstPoint.longitude,
            endLat = lastPoint.latitude,
            endLon = lastPoint.longitude,
            routePoints = routePoints.toList(),
            trafficData = calculateTrafficData(),
            timeTaken = System.currentTimeMillis() - routeStartTime,
            distance = calculateTotalDistance(),
            dayOfWeek = getCurrentDayOfWeek(),
            hourOfDay = getCurrentHour(),
            userSelected = true
        )
        
        routeLearningEngine.addRouteData(routeData)
    }
    
    /**
     * محاسبه داده‌های ترافیک
     */
    private fun calculateTrafficData(): TrafficData {
        val avgSpeed = if (routePoints.isNotEmpty()) {
            routePoints.map { it.speed }.average().toFloat()
        } else {
            0f
        }
        
        val congestionLevel = when {
            avgSpeed > 60 -> CongestionLevel.FREE_FLOW
            avgSpeed > 40 -> CongestionLevel.LIGHT
            avgSpeed > 25 -> CongestionLevel.MODERATE
            avgSpeed > 15 -> CongestionLevel.HEAVY
            else -> CongestionLevel.SEVERE
        }
        
        return TrafficData(avgSpeed, congestionLevel)
    }
    
    /**
     * محاسبه مسافت کل مسیر
     */
    private fun calculateTotalDistance(): Double {
        var totalDistance = 0.0
        
        for (i in 1 until routePoints.size) {
            val prev = routePoints[i - 1]
            val curr = routePoints[i]
            
            totalDistance += calculateDistance(
                prev.latitude, prev.longitude,
                curr.latitude, curr.longitude
            )
        }
        
        return totalDistance * 1000 // کیلومتر به متر
    }
    
    /**
     * محاسبه فاصله بین دو نقطه (Haversine)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // کیلومتر
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * تنظیم listener برای رویدادهای مسیریابی
     */
    fun setNavigationListener(listener: NavigationListener) {
        this.navigationListener = listener
    }
    
    private fun getCurrentHour(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    
    private fun getCurrentDayOfWeek(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    }
    
    /**
     * ایجاد کانال نوتیفیکیشن
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مسیریابی",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نوتیفیکیشن‌های مسیریابی"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * ایجاد نوتیفیکیشن
     */
    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مسیریاب هوشمند فارسی")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_navigation)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    /**
     * به‌روزرسانی نوتیفیکیشن
     */
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        ttsEngine.release()
        voiceAlertManager.release()
        routeLearningEngine.release()
    }
    
    /**
     * رابط برای دریافت رویدادهای مسیریابی
     */
    interface NavigationListener {
        fun onLocationUpdate(location: Location)
        fun onCameraAlert(camera: SpeedCamera, distance: Int)
        fun onSpeedBumpAlert(distance: Int)
        fun onSpeedLimitExceeded(currentSpeed: Int, speedLimit: Int)
        fun onRoutePredicted(prediction: RoutePrediction)
    }
}
