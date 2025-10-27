package ir.navigation.persian.ai.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * دیتابیس برای ذخیره مکان‌ها
 */
class SavedPlaceDatabase(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "saved_places.db"
        private const val DATABASE_VERSION = 1
        
        private const val TABLE_PLACES = "places"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_LAT = "latitude"
        private const val COL_LON = "longitude"
        private const val COL_CATEGORY = "category"
        private const val COL_TIMESTAMP = "timestamp"
    }
    
    data class SavedPlace(
        val id: Long = 0,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val category: String = "other",
        val timestamp: Long = System.currentTimeMillis()
    )
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PLACES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_LAT REAL NOT NULL,
                $COL_LON REAL NOT NULL,
                $COL_CATEGORY TEXT DEFAULT 'other',
                $COL_TIMESTAMP INTEGER
            )
        """.trimIndent()
        
        db.execSQL(createTable)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLACES")
        onCreate(db)
    }
    
    /**
     * ذخیره یک مکان جدید
     */
    fun savePlace(place: SavedPlace): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, place.name)
            put(COL_LAT, place.latitude)
            put(COL_LON, place.longitude)
            put(COL_CATEGORY, place.category)
            put(COL_TIMESTAMP, place.timestamp)
        }
        return db.insert(TABLE_PLACES, null, values)
    }
    
    /**
     * دریافت تمام مکان‌های ذخیره شده
     */
    fun getAllPlaces(): List<SavedPlace> {
        val places = mutableListOf<SavedPlace>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PLACES,
            null,
            null,
            null,
            null,
            null,
            "$COL_TIMESTAMP DESC"
        )
        
        with(cursor) {
            while (moveToNext()) {
                places.add(
                    SavedPlace(
                        id = getLong(getColumnIndexOrThrow(COL_ID)),
                        name = getString(getColumnIndexOrThrow(COL_NAME)),
                        latitude = getDouble(getColumnIndexOrThrow(COL_LAT)),
                        longitude = getDouble(getColumnIndexOrThrow(COL_LON)),
                        category = getString(getColumnIndexOrThrow(COL_CATEGORY)),
                        timestamp = getLong(getColumnIndexOrThrow(COL_TIMESTAMP))
                    )
                )
            }
        }
        cursor.close()
        
        return places
    }
    
    /**
     * حذف یک مکان
     */
    fun deletePlace(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLACES, "$COL_ID = ?", arrayOf(id.toString()))
    }
    
    /**
     * افزودن مکان (متد کوتاه برای سازگاری)
     */
    fun addPlace(name: String, latitude: Double, longitude: Double, category: String = "other"): Long {
        return savePlace(SavedPlace(
            name = name,
            latitude = latitude,
            longitude = longitude,
            category = category
        ))
    }
}
