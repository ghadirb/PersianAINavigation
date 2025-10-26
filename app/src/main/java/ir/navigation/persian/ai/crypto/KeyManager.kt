package ir.navigation.persian.ai.crypto

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * مدیریت کلیدهای API رمزشده
 */
class KeyManager(private val context: Context) {
    
    companion object {
        private const val TAG = "KeyManager"
        
        // لینک مستقیم دانلود فایل کلیدهای رمزشده از Google Drive
        private const val ENCRYPTED_KEYS_URL = 
            "https://drive.google.com/uc?export=download&id=17iwkjyGcxJeDgwQWEcsOdfbOxOah_0u0"
        
        private const val ENCRYPTED_KEYS_FILE = "encrypted_keys.b64"
        private const val DECRYPTED_KEYS_FILE = "decrypted_keys.txt"
        
        private const val PBKDF2_ITERATIONS = 20000
        private const val GCM_TAG_LENGTH = 128
    }
    
    private val encryptedFile: File
        get() = File(context.filesDir, ENCRYPTED_KEYS_FILE)
    
    private val decryptedFile: File
        get() = File(context.filesDir, DECRYPTED_KEYS_FILE)
    
    private var apiKeys = mutableListOf<String>()
    private var currentKeyIndex = 0
    private var isUnlocked = false
    
    /**
     * دانلود فایل کلیدهای رمزشده از Google Drive
     */
    suspend fun downloadEncryptedKeys(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading encrypted keys from Google Drive...")
            
            val connection = URL(ENCRYPTED_KEYS_URL).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            encryptedFile.writeBytes(bytes)
            
            Log.d(TAG, "Encrypted keys downloaded successfully (${bytes.size} bytes)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download encrypted keys", e)
            false
        }
    }
    
    /**
     * رمزگشایی کلیدها با پسورد
     */
    suspend fun unlockKeys(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!encryptedFile.exists()) {
                Log.w(TAG, "Encrypted file not found, trying to download...")
                if (!downloadEncryptedKeys()) {
                    return@withContext false
                }
            }
            
            // خواندن فایل Base64
            val base64Data = encryptedFile.readText()
            val encryptedData = Base64.decode(base64Data, Base64.DEFAULT)
            
            // استخراج salt, IV و ciphertext
            if (encryptedData.size < 28) { // 16 salt + 12 IV
                Log.e(TAG, "Invalid encrypted data size")
                return@withContext false
            }
            
            val salt = encryptedData.copyOfRange(0, 16)
            val iv = encryptedData.copyOfRange(16, 28)
            val ciphertext = encryptedData.copyOfRange(28, encryptedData.size)
            
            // ساخت کلید از پسورد
            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secretKey = keyFactory.generateSecret(keySpec)
            val key = SecretKeySpec(secretKey.encoded, "AES")
            
            // رمزگشایی با AES-GCM
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            
            val decryptedBytes = cipher.doFinal(ciphertext)
            val decryptedText = String(decryptedBytes, Charsets.UTF_8)
            
            // ذخیره کلیدهای رمزگشایی شده
            decryptedFile.writeText(decryptedText)
            
            // پارس کردن کلیدها (هر خط یک کلید)
            apiKeys.clear()
            apiKeys.addAll(
                decryptedText.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            )
            
            isUnlocked = true
            currentKeyIndex = 0
            
            Log.d(TAG, "Keys unlocked successfully. Total keys: ${apiKeys.size}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlock keys", e)
            isUnlocked = false
            false
        }
    }
    
    /**
     * دریافت کلید فعال
     */
    fun getCurrentKey(): String? {
        if (!isUnlocked || apiKeys.isEmpty()) {
            Log.w(TAG, "Keys not unlocked or empty")
            return null
        }
        return apiKeys.getOrNull(currentKeyIndex)
    }
    
    /**
     * تعویض به کلید بعدی (اگر کلید فعلی خراب شد)
     */
    fun switchToNextKey(): Boolean {
        if (!isUnlocked || apiKeys.isEmpty()) return false
        
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        Log.d(TAG, "Switched to key #$currentKeyIndex")
        return true
    }
    
    /**
     * دریافت نام مدل فعال
     */
    fun getActiveModelName(): String {
        if (!isUnlocked) return "قفل شده"
        
        val key = getCurrentKey() ?: return "کلید نامعتبر"
        
        return when {
            key.startsWith("sk-proj-") -> "OpenAI GPT-4"
            key.startsWith("sk-or-v1-") -> "OpenRouter"
            key.startsWith("sk-") && key.length < 50 -> "OpenAI Legacy"
            key.length == 32 && !key.contains("-") -> "Gemini"
            else -> "مدل ناشناس"
        }
    }
    
    /**
     * دریافت تعداد کلیدها
     */
    fun getKeyCount(): Int = apiKeys.size
    
    /**
     * دریافت ایندکس کلید فعال
     */
    fun getCurrentKeyIndex(): Int = currentKeyIndex
    
    /**
     * بررسی وضعیت قفل
     */
    fun isLocked(): Boolean = !isUnlocked
    
    /**
     * قفل کردن دوباره
     */
    fun lock() {
        isUnlocked = false
        apiKeys.clear()
        currentKeyIndex = 0
        
        // حذف فایل رمزگشایی شده
        if (decryptedFile.exists()) {
            decryptedFile.delete()
        }
        
        Log.d(TAG, "Keys locked")
    }
    
    /**
     * به‌روزرسانی کلیدها از سرور
     */
    suspend fun refreshKeys(): Boolean {
        lock()
        return downloadEncryptedKeys()
    }
    
    /**
     * بررسی اعتبار کلید فعلی
     */
    suspend fun validateCurrentKey(): Boolean = withContext(Dispatchers.IO) {
        val key = getCurrentKey() ?: return@withContext false
        
        try {
            // تست ساده با API
            val url = when {
                key.startsWith("sk-proj-") || key.startsWith("sk-") -> 
                    "https://api.openai.com/v1/models"
                key.startsWith("sk-or-v1-") -> 
                    "https://openrouter.ai/api/v1/models"
                else -> return@withContext false
            }
            
            val connection = URL(url).openConnection()
            connection.setRequestProperty("Authorization", "Bearer $key")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = (connection as java.net.HttpURLConnection).responseCode
            connection.disconnect()
            
            responseCode == 200
        } catch (e: Exception) {
            Log.e(TAG, "Key validation failed", e)
            false
        }
    }
}
