package com.uniandesfood.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class MyReview(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long,
    val isSent: Boolean
)

class MyReviewsDatabase(context: Context) : SQLiteOpenHelper(context, "my_reviews.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE my_reviews (" +
                "id TEXT PRIMARY KEY, " +
                "restaurantId TEXT, " +
                "restaurantName TEXT, " +
                "rating INTEGER, " +
                "comment TEXT, " +
                "timestamp INTEGER, " +
                "isSent INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS my_reviews")
        onCreate(db)
    }
}

object MyReviewsStore {

    private var database: MyReviewsDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            database = MyReviewsDatabase(context.applicationContext)
        }
    }

    fun save(review: MyReview) {
        val db = database?.writableDatabase ?: return
        val values = ContentValues()
        values.put("id", review.id)
        values.put("restaurantId", review.restaurantId)
        values.put("restaurantName", review.restaurantName)
        values.put("rating", review.rating)
        values.put("comment", review.comment)
        values.put("timestamp", review.timestamp)
        values.put("isSent", if (review.isSent) 1 else 0)
        db.insertWithOnConflict("my_reviews", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun markAsSent(id: String) {
        val db = database?.writableDatabase ?: return
        val values = ContentValues()
        values.put("isSent", 1)
        db.update("my_reviews", values, "id = ?", arrayOf(id))
    }

    fun getAll(): List<MyReview> {
        val db = database?.readableDatabase ?: return emptyList()
        val list = mutableListOf<MyReview>()
        val cursor = db.rawQuery("SELECT * FROM my_reviews ORDER BY timestamp DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MyReview(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        restaurantId = it.getString(it.getColumnIndexOrThrow("restaurantId")),
                        restaurantName = it.getString(it.getColumnIndexOrThrow("restaurantName")),
                        rating = it.getInt(it.getColumnIndexOrThrow("rating")),
                        comment = it.getString(it.getColumnIndexOrThrow("comment")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        isSent = it.getInt(it.getColumnIndexOrThrow("isSent")) == 1
                    )
                )
            }
        }
        return list
    }
}