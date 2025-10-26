package ir.navigation.persian.ai.ml

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.navigation.persian.ai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.*

/**
 * موتور یادگیری مسیر با استفاده از TensorFlow Lite
 * این موتور از داده‌های مسیرهای قبلی کاربران برای پیشنهاد بهترین مسیر استفاده می‌کند
 */
class RouteLearningEngine(private val context: Context) {
    
    private var tfliteInterpreter: Interpreter? = null
    private val gson = Gson()
    private val routeHistory = mutableListOf<RouteData>()
    
    companion object {
        private const val TAG = "RouteLearningEngine"
        private const val MODEL_FILE = "route_predictor_model.tflite"
        private const val MAX_HISTORY_SIZE = 1000
        private const val FEATURE_SIZE = 12 // تعداد ویژگی‌های ورودی
        private const val EARTH_RADIUS_KM = 6371.0
    }
    
    /**
     * مقداردهی اولیه موتور یادگیری
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)
            options.setUseNNAPI(true) // استفاده از Neural Networks API اندروید
            
            tfliteInterpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Route Learning Engine initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Route Learning Engine", e)
            false
        }
    }
    
    /**
     * بارگذاری فایل مدل TFLite
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * افزودن داده مسیر جدید به تاریخچه
     */
    suspend fun addRouteData(routeData: RouteData) = withContext(Dispatchers.IO) {
        routeHistory.add(routeData)
        
        // حذف داده‌های قدیمی
        if (routeHistory.size > MAX_HISTORY_SIZE) {
            routeHistory.removeAt(0)
        }
        
        Log.d(TAG, "Route data added. History size: ${routeHistory.size}")
    }
    
    /**
     * پیش‌بینی بهترین مسیر بر اساس داده‌های یادگیری
     */
    suspend fun predictBestRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        currentHour: Int,
        dayOfWeek: Int
    ): RoutePrediction? = withContext(Dispatchers.IO) {
        
        if (tfliteInterpreter == null) {
            Log.w(TAG, "Model not initialized")
            return@withContext null
        }
        
        try {
            // استخراج ویژگی‌ها
            val features = extractFeatures(
                startLat, startLon, endLat, endLon,
                currentHour, dayOfWeek
            )
            
            // تبدیل به ByteBuffer
            val inputBuffer = ByteBuffer.allocateDirect(FEATURE_SIZE * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            features.forEach { inputBuffer.putFloat(it) }
            inputBuffer.rewind()
            
            // خروجی مدل: [route_score, estimated_time, congestion_level]
            val outputBuffer = ByteBuffer.allocateDirect(3 * 4)
            outputBuffer.order(ByteOrder.nativeOrder())
            
            // اجرای مدل
            tfliteInterpreter?.run(inputBuffer, outputBuffer)
            
            outputBuffer.rewind()
            val routeScore = outputBuffer.float
            val estimatedTime = outputBuffer.float.toLong()
            val congestionLevel = outputBuffer.float
            
            // یافتن مسیرهای مشابه در تاریخچه
            val similarRoutes = findSimilarRoutes(startLat, startLon, endLat, endLon, currentHour, dayOfWeek)
            
            // تولید پیشنهاد مسیر
            val predictedRoutes = generatePredictedRoutes(
                similarRoutes, routeScore, estimatedTime, congestionLevel
            )
            
            RoutePrediction(
                routes = predictedRoutes,
                confidence = routeScore
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Prediction failed", e)
            null
        }
    }
    
    /**
     * استخراج ویژگی‌های ورودی برای مدل
     */
    private fun extractFeatures(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        hour: Int,
        dayOfWeek: Int
    ): FloatArray {
        val distance = calculateDistance(startLat, startLon, endLat, endLon)
        val bearing = calculateBearing(startLat, startLon, endLat, endLon)
        
        // محاسبه آمار مسیرهای مشابه
        val similarStats = calculateSimilarRouteStats(startLat, startLon, endLat, endLon)
        
        return floatArrayOf(
            startLat.toFloat(),
            startLon.toFloat(),
            endLat.toFloat(),
            endLon.toFloat(),
            distance.toFloat(),
            bearing.toFloat(),
            hour.toFloat() / 24f,
            dayOfWeek.toFloat() / 7f,
            similarStats.avgSpeed,
            similarStats.avgTime,
            similarStats.trafficDensity,
            similarStats.userPreference
        )
    }
    
    /**
     * یافتن مسیرهای مشابه در تاریخچه
     */
    private fun findSimilarRoutes(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        hour: Int,
        dayOfWeek: Int
    ): List<RouteData> {
        val threshold = 1.0 // کیلومتر
        
        return routeHistory.filter { route ->
            val startDist = calculateDistance(startLat, startLon, route.startLat, route.startLon)
            val endDist = calculateDistance(endLat, endLon, route.endLat, route.endLon)
            val hourDiff = abs(route.hourOfDay - hour)
            val dayMatch = route.dayOfWeek == dayOfWeek
            
            startDist < threshold && endDist < threshold && hourDiff <= 2 && dayMatch
        }.sortedByDescending { it.timestamp }
            .take(10)
    }
    
    /**
     * محاسبه آمار مسیرهای مشابه
     */
    private fun calculateSimilarRouteStats(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): RouteStats {
        val similar = routeHistory.filter { route ->
            val startDist = calculateDistance(startLat, startLon, route.startLat, route.startLon)
            val endDist = calculateDistance(endLat, endLon, route.endLat, route.endLon)
            startDist < 1.0 && endDist < 1.0
        }
        
        if (similar.isEmpty()) {
            return RouteStats(0f, 0f, 0f, 0f)
        }
        
        val avgSpeed = similar.map { it.trafficData.averageSpeed }.average().toFloat()
        val avgTime = similar.map { it.timeTaken }.average().toFloat() / 60000f // دقیقه
        val trafficDensity = similar.count { 
            it.trafficData.congestionLevel in listOf(CongestionLevel.HEAVY, CongestionLevel.SEVERE)
        }.toFloat() / similar.size
        val userPreference = similar.count { it.userSelected }.toFloat() / similar.size
        
        return RouteStats(avgSpeed, avgTime, trafficDensity, userPreference)
    }
    
    /**
     * تولید مسیرهای پیشنهادی
     */
    private fun generatePredictedRoutes(
        similarRoutes: List<RouteData>,
        baseScore: Float,
        estimatedTime: Long,
        congestionLevel: Float
    ): List<PredictedRoute> {
        val predictions = mutableListOf<PredictedRoute>()
        
        // مسیر پیشنهادی بر اساس سریع‌ترین زمان
        if (similarRoutes.isNotEmpty()) {
            val fastestRoute = similarRoutes.minByOrNull { it.timeTaken }
            fastestRoute?.let {
                predictions.add(
                    PredictedRoute(
                        routePoints = it.routePoints,
                        estimatedTime = it.timeTaken,
                        score = baseScore * 0.9f,
                        reason = "سریع‌ترین مسیر بر اساس تجربه کاربران"
                    )
                )
            }
        }
        
        // مسیر پیشنهادی بر اساس کمترین ترافیک
        val leastTrafficRoute = similarRoutes
            .filter { it.trafficData.congestionLevel in listOf(CongestionLevel.FREE_FLOW, CongestionLevel.LIGHT) }
            .minByOrNull { it.trafficData.congestionLevel.ordinal }
        
        leastTrafficRoute?.let {
            predictions.add(
                PredictedRoute(
                    routePoints = it.routePoints,
                    estimatedTime = it.timeTaken,
                    score = baseScore * 0.85f,
                    reason = "مسیر با کمترین ترافیک"
                )
            )
        }
        
        // مسیر پیشنهادی بر اساس انتخاب کاربران
        val mostPreferredRoute = similarRoutes
            .filter { it.userSelected }
            .maxByOrNull { route -> 
                similarRoutes.count { it.userSelected && isSimilarRoute(it, route) }
            }
        
        mostPreferredRoute?.let {
            predictions.add(
                PredictedRoute(
                    routePoints = it.routePoints,
                    estimatedTime = it.timeTaken,
                    score = baseScore * 0.95f,
                    reason = "محبوب‌ترین مسیر میان کاربران"
                )
            )
        }
        
        return predictions.distinctBy { it.routePoints.hashCode() }
    }
    
    /**
     * بررسی شباهت دو مسیر
     */
    private fun isSimilarRoute(route1: RouteData, route2: RouteData): Boolean {
        if (route1.routePoints.size != route2.routePoints.size) return false
        
        var similarPoints = 0
        val threshold = 0.1 // کیلومتر
        
        route1.routePoints.forEachIndexed { index, point1 ->
            val point2 = route2.routePoints[index]
            val dist = calculateDistance(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude
            )
            if (dist < threshold) similarPoints++
        }
        
        return (similarPoints.toFloat() / route1.routePoints.size) > 0.8
    }
    
    /**
     * محاسبه فاصله بین دو نقطه (Haversine formula)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
    
    /**
     * محاسبه جهت بین دو نقطه
     */
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        
        return Math.toDegrees(atan2(y, x))
    }
    
    /**
     * صادر کردن داده‌های یادگیری به JSON
     */
    fun exportLearningData(): String {
        return gson.toJson(routeHistory)
    }
    
    /**
     * وارد کردن داده‌های یادگیری از JSON
     */
    suspend fun importLearningData(jsonData: String) = withContext(Dispatchers.IO) {
        try {
            val type = object : TypeToken<List<RouteData>>() {}.type
            val importedData: List<RouteData> = gson.fromJson(jsonData, type)
            routeHistory.addAll(importedData)
            
            // حذف تکراری‌ها
            val uniqueRoutes = routeHistory.distinctBy { it.id }
            routeHistory.clear()
            routeHistory.addAll(uniqueRoutes.takeLast(MAX_HISTORY_SIZE))
            
            Log.d(TAG, "Imported ${importedData.size} routes. Total: ${routeHistory.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import learning data", e)
        }
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        tfliteInterpreter?.close()
        tfliteInterpreter = null
    }
    
    private data class RouteStats(
        val avgSpeed: Float,
        val avgTime: Float,
        val trafficDensity: Float,
        val userPreference: Float
    )
}
