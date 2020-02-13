package com.jeremyhahn.cropdroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jeremyhahn.cropdroid.DATABASE_NAME
import com.jeremyhahn.cropdroid.model.MasterController

private const val DATABASE_VERSION = 1
private const val TABLE_MASTER_CONTROLLERS = "master_controllers"
private const val KEY_ID = "id"
private const val KEY_NAME = "name"
private const val KEY_HOSTNAME = "hostname"

class MasterControllerRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var controllerCount = 0

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_MasterControllerS_TABLE =
            ("CREATE TABLE " + TABLE_MASTER_CONTROLLERS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                    + KEY_HOSTNAME + " TEXT" + ")")
        db.execSQL(CREATE_MasterControllerS_TABLE)
    }

    // Upgrading database
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) { // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MASTER_CONTROLLERS")
        // Create tables again
        onCreate(db)
    }

    fun getCount(db: SQLiteDatabase) : Int {
        val countQuery = "SELECT  * FROM $TABLE_MASTER_CONTROLLERS"
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.rawQuery(countQuery, null)
        cursor.close()
        return cursor.getCount()
    }

    fun addController(controller: MasterController) {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_NAME, controller.name)
        values.put(KEY_HOSTNAME, controller.hostname)
        db.insert(TABLE_MASTER_CONTROLLERS, null, values)
        db.close()
    }

    fun getController(id: Int): MasterController {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_MASTER_CONTROLLERS,
            arrayOf(
                KEY_ID,
                KEY_NAME,
                KEY_HOSTNAME
            ),
            "$KEY_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        if (cursor != null) cursor.moveToFirst()
        return MasterController(
            cursor.getString(0).toInt(),
            cursor.getString(1),
            cursor.getString(2)
        )
    }

    fun getControllerByHostname(hostname: String): MasterController {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_MASTER_CONTROLLERS,
            arrayOf(
                KEY_ID,
                KEY_NAME,
                KEY_HOSTNAME
            ),
            "$KEY_HOSTNAME=?",
            arrayOf(hostname),
            null,
            null,
            null,
            null
        )
        if (cursor != null) cursor.moveToFirst()
        return MasterController(
            cursor.getString(0).toInt(),
            cursor.getString(1),
            cursor.getString(2)
        )
    }

    val allControllers: ArrayList<MasterController>
        get() {
            var controllerList: ArrayList<MasterController> = ArrayList<MasterController>()
            val selectQuery = "SELECT  * FROM $TABLE_MASTER_CONTROLLERS"
            val db: SQLiteDatabase = this.getWritableDatabase()
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    controllerList.add(MasterController(
                        cursor.getString(0).toInt(),
                        cursor.getString(1),
                        cursor.getString(2)
                    ))
                } while (cursor.moveToNext())
            }
            return controllerList
        }

    fun updateController(controller: MasterController): Int {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_NAME, controller.name)
        values.put(KEY_HOSTNAME, controller.hostname)
        return db.update(
            TABLE_MASTER_CONTROLLERS,
            values,
            "$KEY_ID = ?",
            arrayOf<String>(java.lang.String.valueOf(controller.id))
        )
    }

    fun deleteController(controller: MasterController) {
        val db: SQLiteDatabase = this.getWritableDatabase()
        db.delete(
            TABLE_MASTER_CONTROLLERS,
            "$KEY_ID = ?",
            arrayOf<String>(java.lang.String.valueOf(controller.id))
        )
        db.close()
    }
}