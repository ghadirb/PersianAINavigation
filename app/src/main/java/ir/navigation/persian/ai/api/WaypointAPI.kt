package ir.navigation.persian.ai.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * API برای مسیریابی با نقاط میانی (Waypoints)
 */
class WaypointAPI {
    
    data class MultiRouteInfo(
        val totalDistance: Double, // متر
        val totalDuration: Double, // ثانیه
        val legs: List<RouteLeg>, // هر بخش از مسیر
        val geometry: String
    )
    
    data class RouteLeg(
        val distance: Double,
        val duration: Double,
        val startLocation: Pair<Double, Double>,
        val endLocation: Pair<Double, Double>,
        val summary: String
    )
    
    /**
     * مسیریابی با نقاط میانی
     * @param waypoints لیست نقاط (مبدا، میانی‌ها، مقصد)
     */
    suspend fun getRouteWithWaypoints(
        waypoints: List<Pair<Double, Double>>
    ): MultiRouteInfo? = withContext(Dispatchers.IO) {
        try {
            if (waypoints.size < 2) return@withContext null
            
            // ساخت URL با نقاط میانی
            val coordinates = waypoints.joinToString(";") { "${it.second},${it.first}" }
            val url = "https://router.project-osrm.org/route/v1/driving/$coordinates?" +
                    "steps=true&geometries=geojson&overview=full"
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            if (json.getString("code") != "Ok") return@withContext null
            
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null
            
            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val routeLegs = mutableListOf<RouteLeg>()
            
            for (i in 0 until legs.length()) {
                val leg = legs.getJSONObject(i)
                routeLegs.add(
                    RouteLeg(
                        distance = leg.getDouble("distance"),
                        duration = leg.getDouble("duration"),
                        startLocation = waypoints[i],
                        endLocation = waypoints[i + 1],
                        summary = leg.optString("summary", "بخش ${i + 1}")
                    )
                )
            }
            
            MultiRouteInfo(
                totalDistance = route.getDouble("distance"),
                totalDuration = route.getDouble("duration"),
                legs = routeLegs,
                geometry = route.getJSONObject("geometry").toString()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * اضافه کردن نقطه میانی به مسیر موجود
     */
    suspend fun addWaypoint(
        currentRoute: List<Pair<Double, Double>>,
        newWaypoint: Pair<Double, Double>,
        insertIndex: Int? = null
    ): MultiRouteInfo? {
        val waypoints = currentRoute.toMutableList()
        
        if (insertIndex != null && insertIndex in 1 until waypoints.size) {
            waypoints.add(insertIndex, newWaypoint)
        } else {
            // پیدا کردن بهترین جای درج (نزدیک‌ترین نقطه در مسیر)
            val bestIndex = findBestInsertionPoint(waypoints, newWaypoint)
            waypoints.add(bestIndex, newWaypoint)
        }
        
        return getRouteWithWaypoints(waypoints)
    }
    
    /**
     * پیدا کردن بهترین نقطه برای درج waypoint
     */
    private fun findBestInsertionPoint(
        route: List<Pair<Double, Double>>,
        waypoint: Pair<Double, Double>
    ): Int {
        var minDetour = Double.MAX_VALUE
        var bestIndex = 1
        
        for (i in 1 until route.size) {
            val prev = route[i - 1]
            val next = route[i]
            
            // محاسبه انحراف
            val directDist = calculateDistance(prev.first, prev.second, next.first, next.second)
            val withWaypoint = calculateDistance(prev.first, prev.second, waypoint.first, waypoint.second) +
                              calculateDistance(waypoint.first, waypoint.second, next.first, next.second)
            val detour = withWaypoint - directDist
            
            if (detour < minDetour) {
                minDetour = detour
                bestIndex = i
            }
        }
        
        return bestIndex
    }
    
    /**
     * بهینه‌سازی ترتیب نقاط میانی (حل مسئله فروشنده دوره‌گرد ساده)
     */
    suspend fun optimizeWaypoints(
        start: Pair<Double, Double>,
        waypoints: List<Pair<Double, Double>>,
        end: Pair<Double, Double>
    ): List<Pair<Double, Double>> {
        if (waypoints.size <= 1) {
            return listOf(start) + waypoints + listOf(end)
        }
        
        // الگوریتم نزدیک‌ترین همسایه
        val remaining = waypoints.toMutableList()
        val ordered = mutableListOf(start)
        var current = start
        
        while (remaining.isNotEmpty()) {
            val nearest = remaining.minByOrNull { 
                calculateDistance(current.first, current.second, it.first, it.second)
            }!!
            
            ordered.add(nearest)
            remaining.remove(nearest)
            current = nearest
        }
        
        ordered.add(end)
        return ordered
    }
    
    /**
     * محاسبه فاصله (متر)
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
