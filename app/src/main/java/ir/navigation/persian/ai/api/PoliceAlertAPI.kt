package ir.navigation.persian.ai.api

import ir.navigation.persian.ai.model.PoliceAlert
import ir.navigation.persian.ai.model.PoliceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class PoliceAlertAPI {
    private val activeAlerts = ConcurrentHashMap<String, PoliceAlert>()
    
    suspend fun getAlertsNearby(lat: Double, lon: Double, radius: Double = 10.0): List<PoliceAlert> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        activeAlerts.values.filter { alert ->
            alert.expiresAt > now && calculateDistance(lat, lon, alert.latitude, alert.longitude) < radius * 1000
        }
    }
    
    fun reportPolice(lat: Double, lon: Double, type: PoliceType): PoliceAlert {
        val alert = PoliceAlert(
            id = "police_${System.currentTimeMillis()}",
            latitude = lat,
            longitude = lon,
            type = type
        )
        activeAlerts[alert.id] = alert
        return alert
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
