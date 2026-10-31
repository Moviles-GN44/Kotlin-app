package com.uniandesfood.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import org.json.JSONObject

/**
 * Sprint 3 - Local Storage Strategy:
 * SQLite database managing offline persistence and eventual connectivity sync queue.
 */
class LocalDatabaseManager(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "uniandes_food.db"
        private const val DB_VERSION = 1

        private const val TABLE_RESTAURANTS = "cached_restaurants"
        private const val TABLE_OFFLINE_QUEUE = "offline_sync_queue"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_RESTAURANTS (
                id TEXT PRIMARY KEY,
                name TEXT,
                category TEXT,
                buildingTag TEXT,
                walkDistancesJson TEXT,
                averagePriceCOP INTEGER,
                isVegan INTEGER,
                isGlutenFree INTEGER,
                rating REAL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_OFFLINE_QUEUE (
                actionId TEXT PRIMARY KEY,
                actionType TEXT,
                payloadJson TEXT,
                timestamp INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESTAURANTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_OFFLINE_QUEUE")
        onCreate(db)
    }

    // 1. Guardar lista de restaurantes en SQLite
    fun saveRestaurants(restaurants: List<Restaurant>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (r in restaurants) {
                val cv = ContentValues().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("category", r.category)
                    put("buildingTag", r.buildingTag)
                    put("walkDistancesJson", JSONObject(r.walkDistancesFromBuilding as Map<*, *>).toString())
                    put("averagePriceCOP", r.averagePriceCOP)
                    put("isVegan", if (r.isVeganFriendly) 1 else 0)
                    put("isGlutenFree", if (r.isGlutenFreeFriendly) 1 else 0)
                    put("rating", r.rating)
                }
                db.insertWithOnConflict(TABLE_RESTAURANTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // 2. Leer restaurantes de SQLite (Modo Offline)
    fun getCachedRestaurants(): List<Restaurant> {
        val list = mutableListOf<Restaurant>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RESTAURANTS", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val distancesJson = c.getString(c.getColumnIndexOrThrow("walkDistancesJson"))
                val walkMap = mutableMapOf<String, Int>()
                val jsonObj = JSONObject(distancesJson)
                jsonObj.keys().forEach { key -> walkMap[key] = jsonObj.getInt(key) }

                list.add(
                    Restaurant(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        name = c.getString(c.getColumnIndexOrThrow("name")),
                        category = c.getString(c.getColumnIndexOrThrow("category")),
                        buildingTag = c.getString(c.getColumnIndexOrThrow("buildingTag")),
                        walkDistancesFromBuilding = walkMap,
                        averagePriceCOP = c.getInt(c.getColumnIndexOrThrow("averagePriceCOP")),
                        isVeganFriendly = c.getInt(c.getColumnIndexOrThrow("isVegan")) == 1,
                        isGlutenFreeFriendly = c.getInt(c.getColumnIndexOrThrow("isGlutenFree")) == 1,
                        rating = c.getDouble(c.getColumnIndexOrThrow("rating"))
                    )
                )
            }
        }
        return list
    }

    // 3. Encolar acción para conectividad eventual (Outbox Pattern)
    fun enqueueOfflineAction(actionId: String, actionType: String, payload: Map<String, Any>) {
        val cv = ContentValues().apply {
            put("actionId", actionId)
            put("actionType", actionType)
            put("payloadJson", JSONObject(payload).toString())
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(TABLE_OFFLINE_QUEUE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // 4. Obtener acciones pendientes de sincronización
    fun getPendingOfflineActions(): List<Pair<String, Pair<String, String>>> {
        val pending = mutableListOf<Pair<String, Pair<String, String>>>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_OFFLINE_QUEUE ORDER BY timestamp ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("actionId"))
                val type = c.getString(c.getColumnIndexOrThrow("actionType"))
                val payload = c.getString(c.getColumnIndexOrThrow("payloadJson"))
                pending.add(id to (type to payload))
            }
        }
        return pending
    }

    fun removeOfflineAction(actionId: String) {
        writableDatabase.delete(TABLE_OFFLINE_QUEUE, "actionId = ?", arrayOf(actionId))
    }

    // 5. Estrategia Preferences / DataStore (5 pts)
    fun saveLastBuilding(context: Context, building: String) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putString("last_building", building).apply()
    }

    fun getLastBuilding(context: Context): String {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("last_building", "ML") ?: "ML"
    }

    // 6. Estrategia Archivos Locales en almacenamiento interno (5 pts)
    fun saveBackupFile(context: Context, json: String) {
        try {
            context.openFileOutput("restaurants_backup.json", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (_: Exception) {}
    }
}