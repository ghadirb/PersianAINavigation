package ir.navigation.persian.ai.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.net.URLEncoder

/**
 * API برای جستجوی مکان با Nominatim (OpenStreetMap)
 */
class NominatimAPI {
    
    data class SearchResult(
        val displayName: String,
        val lat: Double,
        val lon: Double,
        val type: String,
        val importance: Double
    )
    
    suspend fun search(query: String, limit: Int = 5): List<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=$limit&accept-language=fa"
            
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                results.add(
                    SearchResult(
                        displayName = item.getString("display_name"),
                        lat = item.getDouble("lat"),
                        lon = item.getDouble("lon"),
                        type = item.optString("type", "unknown"),
                        importance = item.optDouble("importance", 0.0)
                    )
                )
            }
            
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}
