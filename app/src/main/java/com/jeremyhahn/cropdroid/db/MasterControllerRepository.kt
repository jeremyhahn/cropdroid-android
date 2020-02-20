package com.jeremyhahn.cropdroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jeremyhahn.cropdroid.DATABASE_NAME
import com.jeremyhahn.cropdroid.model.MasterController

private const val DATABASE_VERSION = 2
private const val TABLE_MASTER_CONTROLLERS = "master_controllers"
private const val KEY_ID = "id"
private const val KEY_NAME = "name"
private const val KEY_HOSTNAME = "hostname"
private const val KEY_TOKEN = "token"

class MasterControllerRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var context : Context? = null
    var controllerCount = 0

    init {
        this.context = context
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createMasterControllerTableSql =
            ("CREATE TABLE " + TABLE_MASTER_CONTROLLERS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_NAME + " TEXT,"
                    + KEY_HOSTNAME + " TEXT" + ","
                    + KEY_TOKEN + " TEXT)")
        db.execSQL(createMasterControllerTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MASTER_CONTROLLERS")
        onCreate(db)
    }

    fun drop() {
        context!!.deleteDatabase(DATABASE_NAME)
    }

    fun getCount() : Int {
        val countQuery = "SELECT  * FROM $TABLE_MASTER_CONTROLLERS"
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.rawQuery(countQuery, null)
        cursor.close()
        var count = cursor.getCount()
        db.close()
        return count
    }

    fun getLastInsertedId(db: SQLiteDatabase) : Int {
        val query = "SELECT  last_insert_rowid()"
        val cursor: Cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        var id = cursor.getInt(0)
        cursor.close()
        return id
    }

    fun addController(controller: MasterController) : MasterController {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_NAME, controller.name)
        values.put(KEY_HOSTNAME, controller.hostname)
        values.put(KEY_TOKEN, controller.token)
        db.insert(TABLE_MASTER_CONTROLLERS, null, values)
        controller.id = getLastInsertedId(db)
        db.close()
        return controller
    }

    fun getController(id: Int): MasterController {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_MASTER_CONTROLLERS,
            arrayOf(
                KEY_ID,
                KEY_NAME,
                KEY_HOSTNAME,
                KEY_TOKEN
            ),
            "$KEY_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        var controller  = MasterController(
            cursor.getString(0).toInt(),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3)
        )
        db.close()
        return controller
    }

    fun getControllerByHostname(hostname: String): MasterController? {
        var controller : MasterController? = null
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_MASTER_CONTROLLERS,
            arrayOf(
                KEY_ID,
                KEY_NAME,
                KEY_HOSTNAME,
                KEY_TOKEN
            ),
            "$KEY_HOSTNAME=?",
            arrayOf(hostname),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        if(cursor.count > 0) {
            controller = MasterController(
                cursor.getString(0).toInt(),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3)
            )
        }
        db.close()
        return controller
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
                        cursor.getString(2),
                        cursor.getString(3)
                    ))
                } while (cursor.moveToNext())
            }
            db.close()
            return controllerList
        }

    fun updateController(controller: MasterController): Int {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_NAME, controller.name)
        values.put(KEY_HOSTNAME, controller.hostname)
        values.put(KEY_TOKEN, controller.token)
        var response = db.update(
            TABLE_MASTER_CONTROLLERS,
            values,
            "$KEY_ID = ?",
            arrayOf<String>(java.lang.String.valueOf(controller.id))
        )
        db.close()
        return response
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