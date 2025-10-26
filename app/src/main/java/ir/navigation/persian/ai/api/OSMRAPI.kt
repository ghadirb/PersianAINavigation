package ir.navigation.persian.ai.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * API برای مسیریابی با OSRM
 */
class OSMRAPI {
    
    data class RouteInfo(
        val distance: Double, // متر
        val duration: Double, // ثانیه
        val geometry: String, // polyline
        val steps: List<Step>
    )
    
    data class Step(
        val instruction: String,
        val distance: Double,
        val duration: Double,
        val maneuver: String, // turn-left, turn-right, etc.
        val location: Pair<Double, Double>
    )
    
    /**
     * دریافت چند مسیر مختلف (alternatives)
     */
    suspend fun getRoutes(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        alternatives: Int = 3
    ): List<RouteInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "$startLon,$startLat;$endLon,$endLat?" +
                    "alternatives=$alternatives&steps=true&geometries=geojson&overview=full"
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            val routes = mutableListOf<RouteInfo>()
            val routesArray = json.getJSONArray("routes")
            
            for (i in 0 until routesArray.length()) {
                val route = routesArray.getJSONObject(i)
                val legs = route.getJSONArray("legs")
                val steps = mutableListOf<Step>()
                
                // استخراج دستورالعمل‌ها
                for (j in 0 until legs.length()) {
                    val leg = legs.getJSONObject(j)
                    val stepsArray = leg.getJSONArray("steps")
                    
                    for (k in 0 until stepsArray.length()) {
                        val step = stepsArray.getJSONObject(k)
                        val maneuver = step.getJSONObject("maneuver")
                        val location = maneuver.getJSONArray("location")
                        
                        steps.add(
                            Step(
                                instruction = translateInstruction(maneuver.optString("type", "")),
                                distance = step.getDouble("distance"),
                                duration = step.getDouble("duration"),
                                maneuver = maneuver.optString("type", ""),
                                location = Pair(location.getDouble(1), location.getDouble(0))
                            )
                        )
                    }
                }
                
                routes.add(
                    RouteInfo(
                        distance = route.getDouble("distance"),
                        duration = route.getDouble("duration"),
                        geometry = route.getJSONObject("geometry").toString(),
                        steps = steps
                    )
                )
            }
            
            routes
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * ترجمه دستورالعمل‌ها به فارسی
     */
    private fun translateInstruction(type: String): String {
        return when (type) {
            "turn-right" -> "به راست بپیچید"
            "turn-left" -> "به چپ بپیچید"
            "turn-sharp-right" -> "به راست تند بپیچید"
            "turn-sharp-left" -> "به چپ تند بپیچید"
            "turn-slight-right" -> "کمی به راست بپیچید"
            "turn-slight-left" -> "کمی به چپ بپیچید"
            "continue" -> "مستقیم بروید"
            "arrive" -> "به مقصد رسیدید"
            "depart" -> "شروع به حرکت کنید"
            "roundabout" -> "وارد میدان شوید"
            else -> "به مسیر ادامه دهید"
        }
    }
}
