package com.jeremyhahn.cropdroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jeremyhahn.cropdroid.Constants.Companion.DATABASE_NAME
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.utils.JsonWebToken

private const val DATABASE_VERSION = 1
private const val TABLE_SERVERS = "servers"
private const val KEY_HOSTNAME = "hostname"
private const val KEY_SECURE = "secure"
private const val KEY_TOKEN = "token"

class MasterControllerRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var context : Context? = null

    init {
        this.context = context
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createMasterControllerTableSql =
            ("CREATE TABLE " + TABLE_SERVERS + "("
                    + KEY_HOSTNAME + " VARCHAR(255) PRIMARY KEY" + ","
                    + KEY_SECURE + " INT" + ","
                    + KEY_TOKEN + " TEXT)")
        db.execSQL(createMasterControllerTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SERVERS")
        onCreate(db)
    }

    fun drop() {
        context!!.deleteDatabase(DATABASE_NAME)
    }

    fun getCount() : Int {
        val countQuery = "SELECT  * FROM $TABLE_SERVERS"
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

    fun create(controller: Connection) : Connection {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_HOSTNAME, controller.hostname)
        values.put(KEY_SECURE, controller.secure)
        values.put(KEY_TOKEN, controller.token)
        db.insert(TABLE_SERVERS, null, values)
        //controller.id = getLastInsertedId(db)
        db.close()
        if (!controller.token.isEmpty()) {
            controller.jwt = JsonWebToken(context!!, controller.token)
        }
        return controller
    }

    fun get(hostname: String): Connection {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_SERVERS,
            arrayOf(
                KEY_HOSTNAME,
                KEY_SECURE,
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
        var controller  = Connection(
            cursor.getString(0),
            cursor.getString(1).toInt(),
            cursor.getString(2),
            null)
        db.close()
        if(controller.token != "") {
            controller.jwt = JsonWebToken(context!!, controller.token)
        }
        return controller
    }

    fun getByHostname(hostname: String): Connection? {
        var controller : Connection? = null
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_SERVERS,
            arrayOf(
                KEY_HOSTNAME,
                KEY_SECURE,
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
            controller = Connection(
                cursor.getString(0),
                cursor.getString(1).toInt(),
                cursor.getString(2),
                null)
            if(controller.token != "") {
                controller.jwt = JsonWebToken(context!!, controller.token)
            }
        }
        db.close()
        return controller
    }

    val allControllers: ArrayList<Connection>
        get() {
            var controllerList: ArrayList<Connection> = ArrayList<Connection>()
            val selectQuery = "SELECT  * FROM $TABLE_SERVERS ORDER BY $KEY_HOSTNAME"
            val db: SQLiteDatabase = this.getWritableDatabase()
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val controller = Connection(
                        cursor.getString(0),
                        cursor.getString(1).toInt(),
                        cursor.getString(2),
                        null)
                    if(controller.token != "") {
                        controller.jwt = JsonWebToken(context!!, controller.token)
                    }
                    controllerList.add(controller)
                } while (cursor.moveToNext())
            }
            db.close()
            return controllerList
        }

    fun updateController(controller: Connection): Int {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_HOSTNAME, controller.hostname)
        values.put(KEY_SECURE, controller.secure)
        values.put(KEY_TOKEN, controller.token)
        var response = db.update(
            TABLE_SERVERS,
            values,
            "$KEY_HOSTNAME = ?",
            arrayOf<String>(java.lang.String.valueOf(controller.hostname))
        )
        db.close()
        return response
    }

    fun delete(controller: Connection) {
        val db: SQLiteDatabase = this.getWritableDatabase()
        db.delete(
            TABLE_SERVERS,
            "$KEY_HOSTNAME = ?",
            arrayOf<String>(java.lang.String.valueOf(controller.hostname))
        )
        db.close()
    }
}