package ir.navigation.persian.ai.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.net.URLEncoder

/**
 * API برای جستجوی مکان‌های مفید در مسیر
 * مانند پمپ بنزین، رستوران، بیمارستان و...
 */
class PlacesAPI {
    
    data class Place(
        val name: String,
        val lat: Double,
        val lon: Double,
        val type: PlaceType,
        val distance: Double, // متر از موقعیت فعلی
        val address: String
    )
    
    enum class PlaceType(val displayName: String, val icon: String) {
        GAS_STATION("پمپ بنزین", "⛽"),
        RESTAURANT("رستوران", "🍽️"),
        CAFE("کافه", "☕"),
        HOSPITAL("بیمارستان", "🏥"),
        PHARMACY("داروخانه", "💊"),
        PARKING("پارکینگ", "🅿️"),
        HOTEL("هتل", "🏨"),
        ATM("خودپرداز", "🏧"),
        MOSQUE("مسجد", "🕌"),
        POLICE("پلیس", "👮"),
        MECHANIC("تعمیرگاه", "🔧"),
        REST_AREA("استراحتگاه", "🛏️")
    }
    
    /**
     * جستجوی مکان‌های نزدیک با Nominatim
     */
    suspend fun searchNearby(
        lat: Double,
        lon: Double,
        type: PlaceType,
        radius: Double = 5.0 // کیلومتر
    ): List<Place> = withContext(Dispatchers.IO) {
        try {
            val amenity = when (type) {
                PlaceType.GAS_STATION -> "fuel"
                PlaceType.RESTAURANT -> "restaurant"
                PlaceType.CAFE -> "cafe"
                PlaceType.HOSPITAL -> "hospital"
                PlaceType.PHARMACY -> "pharmacy"
                PlaceType.PARKING -> "parking"
                PlaceType.HOTEL -> "hotel"
                PlaceType.ATM -> "atm"
                PlaceType.MOSQUE -> "place_of_worship"
                PlaceType.POLICE -> "police"
                PlaceType.MECHANIC -> "car_repair"
                PlaceType.REST_AREA -> "rest_area"
            }
            
            // استفاده از Overpass API برای جستجوی دقیق‌تر
            val query = "[out:json];node[\"amenity\"=\"$amenity\"](around:${radius * 1000},$lat,$lon);out;"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://overpass-api.de/api/interpreter?data=$encodedQuery"
            
            val response = URL(url).readText()
            val json = org.json.JSONObject(response)
            val elements = json.getJSONArray("elements")
            
            val places = mutableListOf<Place>()
            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject("tags")
                val placeLat = element.getDouble("lat")
                val placeLon = element.getDouble("lon")
                
                places.add(
                    Place(
                        name = tags?.optString("name", type.displayName) ?: type.displayName,
                        lat = placeLat,
                        lon = placeLon,
                        type = type,
                        distance = calculateDistance(lat, lon, placeLat, placeLon),
                        address = tags?.optString("addr:full", "آدرس نامشخص") ?: "آدرس نامشخص"
                    )
                )
            }
            
            // مرتب‌سازی بر اساس فاصله
            places.sortedBy { it.distance }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * جستجوی مکان‌های روی مسیر
     * @param routePoints نقاط مسیر
     * @param type نوع مکان
     * @param maxDetour حداکثر انحراف از مسیر (کیلومتر)
     */
    suspend fun searchAlongRoute(
        routePoints: List<Pair<Double, Double>>,
        type: PlaceType,
        maxDetour: Double = 2.0
    ): List<Place> = withContext(Dispatchers.IO) {
        val allPlaces = mutableListOf<Place>()
        
        // جستجو در چند نقطه کلیدی مسیر
        val samplePoints = routePoints.filterIndexed { index, _ -> 
            index % (routePoints.size / 5).coerceAtLeast(1) == 0 
        }
        
        for (point in samplePoints) {
            val places = searchNearby(point.first, point.second, type, maxDetour)
            allPlaces.addAll(places)
        }
        
        // حذف تکراری‌ها
        allPlaces.distinctBy { "${it.lat},${it.lon}" }
    }
    
    /**
     * پیشنهاد مکان‌های مفید برای توقف
     */
    suspend fun suggestStops(
        currentLat: Double,
        currentLon: Double,
        destinationLat: Double,
        destinationLon: Double
    ): Map<PlaceType, List<Place>> = withContext(Dispatchers.IO) {
        val distance = calculateDistance(currentLat, currentLon, destinationLat, destinationLon)
        val suggestions = mutableMapOf<PlaceType, List<Place>>()
        
        // اگر مسیر بیش از 50 کیلومتر است، پمپ بنزین پیشنهاد بده
        if (distance > 50000) {
            suggestions[PlaceType.GAS_STATION] = searchNearby(currentLat, currentLon, PlaceType.GAS_STATION)
        }
        
        // اگر مسیر بیش از 100 کیلومتر است، رستوران پیشنهاد بده
        if (distance > 100000) {
            suggestions[PlaceType.RESTAURANT] = searchNearby(currentLat, currentLon, PlaceType.RESTAURANT)
            suggestions[PlaceType.REST_AREA] = searchNearby(currentLat, currentLon, PlaceType.REST_AREA)
        }
        
        suggestions
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
