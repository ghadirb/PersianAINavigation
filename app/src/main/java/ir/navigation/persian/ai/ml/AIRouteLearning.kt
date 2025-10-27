package ir.navigation.persian.ai.ml

import android.content.Context
import android.location.Location
import ir.navigation.persian.ai.api.OSMRAPI
import ir.navigation.persian.ai.drive.GoogleDriveUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * سیستم یادگیری هوشمند مسیر با AI
 */
class AIRouteLearning(private val context: Context) {
    
    private val driveUploader = GoogleDriveUploader(context)
    private val routeHistory = mutableListOf<RouteRecord>()
    
    data class RouteRecord(
        val startLat: Double,
        val startLon: Double,
        val endLat: Double,
        val endLon: Double,
        val distance: Double,
        val duration: Double,
        val timestamp: Long,
        val avgSpeed: Double,
        val quality: Float, // 0-1 (based on user behavior)
        val waypoints: List<Location>
    )
    
    /**
     * شروع ضبط مسیر
     */
    fun startRecording(startLat: Double, startLon: Double, endLat: Double, endLon: Double) {
        currentRecording = RouteRecording(
            startLat, startLon, endLat, endLon,
            System.currentTimeMillis(),
            mutableListOf()
        )
    }
    
    private var currentRecording: RouteRecording? = null
    
    data class RouteRecording(
        val startLat: Double,
        val startLon: Double,
        val endLat: Double,
        val endLon: Double,
        val startTime: Long,
        val waypoints: MutableList<Location>
    )
    
    /**
     * اضافه کردن نقطه به مسیر در حال ضبط
     */
    fun addWaypoint(location: Location) {
        currentRecording?.waypoints?.add(location)
    }
    
    /**
     * پایان ضبط و ارزیابی کیفیت
     */
    fun finishRecording(userRating: Float = 0.8f) {
        currentRecording?.let { recording ->
            val endTime = System.currentTimeMillis()
            val duration = (endTime - recording.startTime) / 1000.0 // seconds
            
            // Calculate distance
            var totalDistance = 0.0
            for (i in 0 until recording.waypoints.size - 1) {
                val loc1 = recording.waypoints[i]
                val loc2 = recording.waypoints[i + 1]
                totalDistance += calculateDistance(loc1, loc2)
            }
            
            // Calculate average speed
            val avgSpeed = if (duration > 0) (totalDistance / duration) * 3.6 else 0.0 // km/h
            
            // Create route record
            val route = RouteRecord(
                recording.startLat,
                recording.startLon,
                recording.endLat,
                recording.endLon,
                totalDistance,
                duration,
                recording.startTime,
                avgSpeed,
                userRating,
                recording.waypoints
            )
            
            routeHistory.add(route)
            
            // Upload to Google Drive if quality is good
            if (userRating >= 0.7f) {
                uploadRouteToCloud(route)
            }
            
            currentRecording = null
        }
    }
    
    /**
     * آپلود مسیر به Google Drive
     */
    private fun uploadRouteToCloud(route: RouteRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("startLat", route.startLat)
                    put("startLon", route.startLon)
                    put("endLat", route.endLat)
                    put("endLon", route.endLon)
                    put("distance", route.distance)
                    put("duration", route.duration)
                    put("avgSpeed", route.avgSpeed)
                    put("quality", route.quality)
                    put("timestamp", route.timestamp)
                    
                    val waypointsArray = JSONArray()
                    route.waypoints.forEach { loc ->
                        waypointsArray.put(JSONObject().apply {
                            put("lat", loc.latitude)
                            put("lon", loc.longitude)
                            put("speed", loc.speed)
                            put("time", loc.time)
                        })
                    }
                    put("waypoints", waypointsArray)
                }
                
                val fileName = "route_${route.timestamp}.json"
                driveUploader.uploadRouteData(json.toString(), fileName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * دانلود و یادگیری از مسیرهای دیگران
     */
    suspend fun learnFromSharedRoutes(): Int {
        val sharedRoutes = driveUploader.downloadSharedRoutes()
        var learnedCount = 0
        
        sharedRoutes.forEach { routeJson ->
            try {
                val json = JSONObject(routeJson)
                val waypoints = mutableListOf<Location>()
                val waypointsArray = json.getJSONArray("waypoints")
                
                for (i in 0 until waypointsArray.length()) {
                    val wp = waypointsArray.getJSONObject(i)
                    val loc = Location("").apply {
                        latitude = wp.getDouble("lat")
                        longitude = wp.getDouble("lon")
                        speed = wp.getDouble("speed").toFloat()
                        time = wp.getLong("time")
                    }
                    waypoints.add(loc)
                }
                
                val route = RouteRecord(
                    json.getDouble("startLat"),
                    json.getDouble("startLon"),
                    json.getDouble("endLat"),
                    json.getDouble("endLon"),
                    json.getDouble("distance"),
                    json.getDouble("duration"),
                    json.getLong("timestamp"),
                    json.getDouble("avgSpeed"),
                    json.getDouble("quality").toFloat(),
                    waypoints
                )
                
                routeHistory.add(route)
                learnedCount++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return learnedCount
    }
    
    /**
     * پیشنهاد بهترین مسیر بر اساس یادگیری
     */
    fun suggestBestRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double): RouteRecord? {
        // Find similar routes
        val similarRoutes = routeHistory.filter { route ->
            val startDist = calculateDistance(startLat, startLon, route.startLat, route.startLon)
            val endDist = calculateDistance(endLat, endLon, route.endLat, route.endLon)
            startDist < 1000 && endDist < 1000 // Within 1km
        }
        
        // Return best quality route
        return similarRoutes.maxByOrNull { it.quality }
    }
    
    /**
     * محاسبه فاصله بین دو نقطه
     */
    private fun calculateDistance(loc1: Location, loc2: Location): Double {
        return loc1.distanceTo(loc2).toDouble()
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val loc1 = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        val loc2 = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        return loc1.distanceTo(loc2).toDouble()
    }
    
    /**
     * دریافت آمار یادگیری
     */
    fun getStats(): Map<String, Any> {
        return mapOf(
            "totalRoutes" to routeHistory.size,
            "avgQuality" to (routeHistory.map { it.quality }.average()),
            "totalDistance" to routeHistory.sumOf { it.distance },
            "avgSpeed" to (routeHistory.map { it.avgSpeed }.average())
        )
    }
}
