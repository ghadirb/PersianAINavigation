package ir.navigation.persian.ai.drive

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * مدیریت ارتباط با Google Drive برای ذخیره و دریافت داده‌های یادگیری
 */
class GoogleDriveManager(private val context: Context) {
    
    private var driveService: Drive? = null
    private var googleSignInClient: GoogleSignInClient? = null
    
    companion object {
        private const val TAG = "GoogleDriveManager"
        const val REQUEST_CODE_SIGN_IN = 9001
        
        // شناسه پوشه عمومی در Google Drive
        private const val SHARED_FOLDER_ID = "1bp1Ay9kmK_bjWq_PznRfkPvhhjdhSye1"
        private const val LEARNING_DATA_FILE = "route_learning_data.json"
        private const val CAMERA_DATA_FILE = "speed_cameras_data.json"
    }
    
    /**
     * مقداردهی اولیه Google Sign-In
     */
    fun initializeSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
    }
    
    /**
     * دریافت Intent برای ورود به Google
     */
    fun getSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }
    
    /**
     * راه‌اندازی سرویس Drive پس از ورود
     */
    suspend fun setupDriveService(account: GoogleSignInAccount): Boolean = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            
            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Persian AI Navigation")
                .build()
            
            Log.d(TAG, "Drive service initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Drive service", e)
            false
        }
    }
    
    /**
     * بررسی وضعیت ورود
     */
    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && driveService != null
    }
    
    /**
     * آپلود داده‌های یادگیری به Drive
     */
    suspend fun uploadLearningData(jsonData: String): Boolean = withContext(Dispatchers.IO) {
        if (driveService == null) {
            Log.w(TAG, "Drive service not initialized")
            return@withContext false
        }
        
        try {
            // جستجوی فایل موجود
            val existingFileId = findFileInFolder(LEARNING_DATA_FILE, SHARED_FOLDER_ID)
            
            if (existingFileId != null) {
                // به‌روزرسانی فایل موجود
                updateFile(existingFileId, jsonData)
            } else {
                // ایجاد فایل جدید
                createFile(LEARNING_DATA_FILE, jsonData, SHARED_FOLDER_ID)
            }
            
            Log.d(TAG, "Learning data uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload learning data", e)
            false
        }
    }
    
    /**
     * دانلود داده‌های یادگیری از Drive
     */
    suspend fun downloadLearningData(): String? = withContext(Dispatchers.IO) {
        if (driveService == null) {
            Log.w(TAG, "Drive service not initialized")
            return@withContext null
        }
        
        try {
            val fileId = findFileInFolder(LEARNING_DATA_FILE, SHARED_FOLDER_ID)
            
            if (fileId != null) {
                val outputStream = ByteArrayOutputStream()
                driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(outputStream)
                val data = outputStream.toString("UTF-8")
                
                Log.d(TAG, "Learning data downloaded successfully")
                return@withContext data
            } else {
                Log.w(TAG, "Learning data file not found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download learning data", e)
            null
        }
    }
    
    /**
     * آپلود داده‌های دوربین‌ها به Drive
     */
    suspend fun uploadCameraData(jsonData: String): Boolean = withContext(Dispatchers.IO) {
        if (driveService == null) {
            Log.w(TAG, "Drive service not initialized")
            return@withContext false
        }
        
        try {
            val existingFileId = findFileInFolder(CAMERA_DATA_FILE, SHARED_FOLDER_ID)
            
            if (existingFileId != null) {
                updateFile(existingFileId, jsonData)
            } else {
                createFile(CAMERA_DATA_FILE, jsonData, SHARED_FOLDER_ID)
            }
            
            Log.d(TAG, "Camera data uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload camera data", e)
            false
        }
    }
    
    /**
     * دانلود داده‌های دوربین‌ها از Drive
     */
    suspend fun downloadCameraData(): String? = withContext(Dispatchers.IO) {
        if (driveService == null) {
            Log.w(TAG, "Drive service not initialized")
            return@withContext null
        }
        
        try {
            val fileId = findFileInFolder(CAMERA_DATA_FILE, SHARED_FOLDER_ID)
            
            if (fileId != null) {
                val outputStream = ByteArrayOutputStream()
                driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(outputStream)
                val data = outputStream.toString("UTF-8")
                
                Log.d(TAG, "Camera data downloaded successfully")
                return@withContext data
            } else {
                Log.w(TAG, "Camera data file not found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download camera data", e)
            null
        }
    }
    
    /**
     * جستجوی فایل در پوشه خاص
     */
    private fun findFileInFolder(fileName: String, folderId: String): String? {
        try {
            val query = "name='$fileName' and '$folderId' in parents and trashed=false"
            val result: FileList = driveService?.files()?.list()
                ?.setQ(query)
                ?.setSpaces("drive")
                ?.setFields("files(id, name)")
                ?.execute() ?: return null
            
            return if (result.files.isNotEmpty()) {
                result.files[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding file", e)
            return null
        }
    }
    
    /**
     * ایجاد فایل جدید
     */
    private fun createFile(fileName: String, content: String, folderId: String) {
        val fileMetadata = File()
        fileMetadata.name = fileName
        fileMetadata.parents = listOf(folderId)
        fileMetadata.mimeType = "application/json"
        
        val contentStream = content.byteInputStream()
        
        driveService?.files()?.create(fileMetadata, com.google.api.client.http.InputStreamContent(
            "application/json",
            contentStream
        ))?.setFields("id")?.execute()
    }
    
    /**
     * به‌روزرسانی فایل موجود
     */
    private fun updateFile(fileId: String, content: String) {
        val contentStream = content.byteInputStream()
        
        driveService?.files()?.update(
            fileId,
            null,
            com.google.api.client.http.InputStreamContent("application/json", contentStream)
        )?.execute()
    }
    
    /**
     * خروج از حساب Google
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        googleSignInClient?.signOut()?.await()
        driveService = null
        Log.d(TAG, "Signed out from Google Drive")
    }
    
    /**
     * همگام‌سازی دوطرفه داده‌ها
     */
    suspend fun syncData(localData: String): String? = withContext(Dispatchers.IO) {
        try {
            // دانلود داده‌های سرور
            val remoteData = downloadLearningData()
            
            // ادغام داده‌ها
            val mergedData = mergeJsonData(localData, remoteData)
            
            // آپلود داده‌های ادغام شده
            uploadLearningData(mergedData)
            
            mergedData
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            null
        }
    }
    
    /**
     * ادغام دو مجموعه داده JSON
     */
    private fun mergeJsonData(local: String, remote: String?): String {
        if (remote.isNullOrEmpty()) return local
        
        // این تابع باید منطق ادغام هوشمندانه را پیاده کند
        // در اینجا یک پیاده‌سازی ساده ارائه شده است
        
        return try {
            val gson = com.google.gson.Gson()
            val localList = gson.fromJson(local, List::class.java) as? List<Map<String, Any>> ?: emptyList()
            val remoteList = gson.fromJson(remote, List::class.java) as? List<Map<String, Any>> ?: emptyList()
            
            val mergedMap = mutableMapOf<String, Map<String, Any>>()
            
            localList.forEach { item ->
                val id = item["id"] as? String ?: return@forEach
                mergedMap[id] = item
            }
            
            remoteList.forEach { item ->
                val id = item["id"] as? String ?: return@forEach
                val timestamp = item["timestamp"] as? Double ?: 0.0
                val existingTimestamp = (mergedMap[id]?.get("timestamp") as? Double) ?: 0.0
                
                if (timestamp > existingTimestamp) {
                    mergedMap[id] = item
                }
            }
            
            gson.toJson(mergedMap.values.toList())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge data", e)
            local
        }
    }
}
