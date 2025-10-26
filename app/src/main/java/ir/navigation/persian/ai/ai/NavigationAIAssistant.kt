package ir.navigation.persian.ai.ai

import android.content.Context
import android.util.Log
import ir.navigation.persian.ai.crypto.KeyManager
import ir.navigation.persian.ai.service.NavigationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * دستیار هوش مصنوعی برای کنترل مسیریاب
 */
class NavigationAIAssistant(
    private val context: Context,
    private val keyManager: KeyManager
) {
    
    companion object {
        private const val TAG = "NavigationAI"
        private const val MAX_HISTORY = 10
    }
    
    private val chatHistory = mutableListOf<ChatMessage>()
    private var navigationService: NavigationService? = null
    
    data class ChatMessage(
        val role: String, // "user" or "assistant"
        val content: String
    )
    
    /**
     * تنظیم سرویس مسیریابی برای کنترل
     */
    fun setNavigationService(service: NavigationService) {
        this.navigationService = service
    }
    
    /**
     * ارسال پیام به AI و دریافت پاسخ
     */
    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = keyManager.getCurrentKey()
            if (apiKey == null) {
                return@withContext "لطفا ابتدا با وارد کردن رمز، کلیدهای API را فعال کنید."
            }
            
            // افزودن پیام کاربر به تاریخچه
            chatHistory.add(ChatMessage("user", userMessage))
            
            // ایجاد system prompt برای مسیریابی
            val systemPrompt = """
                شما یک دستیار هوشمند فارسی برای مسیریاب هستید. 
                
                قابلیت‌های شما:
                1. پاسخ به سوالات درباره مسیریابی
                2. کنترل مسیریاب (شروع، توقف، تغییر مسیر)
                3. اطلاع‌رسانی درباره دوربین‌ها و سرعت‌گیرها
                4. پیشنهاد بهترین مسیر
                5. گزارش وضعیت ترافیک
                
                دستورات قابل اجرا:
                - "START_NAVIGATION:{lat},{lon},{destLat},{destLon}" - شروع مسیریابی
                - "STOP_NAVIGATION" - توقف مسیریابی
                - "GET_LOCATION" - دریافت موقعیت فعلی
                - "ADD_CAMERA:{lat},{lon},{type},{speedLimit}" - افزودن دوربین
                - "REPORT_TRAFFIC:{level}" - گزارش ترافیک
                
                همیشه به فارسی پاسخ دهید و در صورت نیاز به اجرای دستور، آن را در انتهای پاسخ با فرمت [COMMAND:...] بنویسید.
            """.trimIndent()
            
            // ساخت پیام‌ها برای API
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                
                // افزودن تاریخچه چت
                chatHistory.takeLast(MAX_HISTORY).forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
            }
            
            // تعیین API endpoint بر اساس نوع کلید
            val (apiUrl, modelName) = when {
                apiKey.startsWith("sk-proj-") || apiKey.startsWith("sk-") -> 
                    Pair("https://api.openai.com/v1/chat/completions", "gpt-3.5-turbo")
                apiKey.startsWith("sk-or-v1-") -> 
                    Pair("https://openrouter.ai/api/v1/chat/completions", "openai/gpt-3.5-turbo")
                else -> return@withContext "نوع کلید API پشتیبانی نمی‌شود"
            }
            
            // ساخت request body
            val requestBody = JSONObject().apply {
                put("model", modelName)
                put("messages", messages)
                put("temperature", 0.7)
                put("max_tokens", 500)
            }
            
            // ارسال request
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.doOutput = true
            
            // نوشتن body
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()
            
            // خواندن response
            val responseCode = connection.responseCode
            
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                
                val jsonResponse = JSONObject(response)
                val assistantMessage = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                
                // افزودن پاسخ به تاریخچه
                chatHistory.add(ChatMessage("assistant", assistantMessage))
                
                // اجرای دستورات در صورت وجود
                executeCommands(assistantMessage)
                
                assistantMessage
                
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText()
                connection.disconnect()
                
                Log.e(TAG, "API Error: $responseCode - $errorStream")
                
                // در صورت خطای 401 (Unauthorized)، تعویض کلید
                if (responseCode == 401) {
                    if (keyManager.switchToNextKey()) {
                        return@withContext "کلید قبلی نامعتبر بود. کلید جدید فعال شد. لطفا دوباره امتحان کنید."
                    } else {
                        return@withContext "تمام کلیدها نامعتبر هستند. لطفا کلیدها را بروزرسانی کنید."
                    }
                }
                
                "متاسفانه خطایی رخ داد. لطفا دوباره تلاش کنید."
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            "خطا در ارتباط با سرور: ${e.message}"
        }
    }
    
    /**
     * اجرای دستورات استخراج شده از پاسخ AI
     */
    private suspend fun executeCommands(response: String) {
        val commandRegex = """\[COMMAND:(.*?)\]""".toRegex()
        val commands = commandRegex.findAll(response).map { it.groupValues[1] }
        
        commands.forEach { command ->
            try {
                when {
                    command.startsWith("START_NAVIGATION:") -> {
                        val parts = command.substringAfter(":").split(",")
                        if (parts.size == 4) {
                            val startLat = parts[0].toDouble()
                            val startLon = parts[1].toDouble()
                            val endLat = parts[2].toDouble()
                            val endLon = parts[3].toDouble()
                            
                            withContext(Dispatchers.Main) {
                                navigationService?.startNavigation(
                                    startLat, startLon, endLat, endLon, emptyList()
                                )
                            }
                            
                            Log.d(TAG, "Started navigation from ($startLat,$startLon) to ($endLat,$endLon)")
                        }
                    }
                    
                    command == "STOP_NAVIGATION" -> {
                        withContext(Dispatchers.Main) {
                            navigationService?.stopNavigation()
                        }
                        Log.d(TAG, "Stopped navigation")
                    }
                    
                    command == "GET_LOCATION" -> {
                        // موقعیت فعلی را دریافت کنید
                        Log.d(TAG, "Get current location")
                    }
                    
                    command.startsWith("ADD_CAMERA:") -> {
                        val parts = command.substringAfter(":").split(",")
                        if (parts.size >= 3) {
                            Log.d(TAG, "Add camera at ${parts[0]},${parts[1]}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command: $command", e)
            }
        }
    }
    
    /**
     * پاک کردن تاریخچه چت
     */
    fun clearHistory() {
        chatHistory.clear()
        Log.d(TAG, "Chat history cleared")
    }
    
    /**
     * دریافت تاریخچه چت
     */
    fun getHistory(): List<ChatMessage> = chatHistory.toList()
}
