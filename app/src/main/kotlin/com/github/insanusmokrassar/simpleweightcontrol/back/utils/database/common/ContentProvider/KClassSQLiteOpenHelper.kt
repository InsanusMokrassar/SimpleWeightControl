package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ContentProvider

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.createTableIfNotExist
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.tableName
import kotlin.reflect.KClass

internal class KClassSQLiteOpenHelper<M: Any> (
        val modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int
): SQLiteOpenHelper(context, databaseName, null, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.createTableIfNotExist(modelClass)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, newVersion: Int) {
        TODO()
    }

    fun insert(cv: ContentValues): Long {
        return writableDatabase.insert(
                modelClass.tableName(),
                null,
                cv
        )
    }

    fun find(
            columns: Array<out String>? = null,
            where: String? = null,
            selectionArgs: Array<String>? = null,
            groupBy: String? = null,
            having: String? = null,
            orderBy: String? = null,
            limit: String? = null
    ): Cursor = readableDatabase.query(
            modelClass.tableName(),
            columns,
            where,
            selectionArgs,
            groupBy,
            having,
            orderBy,
            limit
    )

    fun update(
            cv: ContentValues,
            where: String?,
            whereArgs: Array<String>?,
            onConflict: Int = SQLiteDatabase.CONFLICT_REPLACE
    ): Int = writableDatabase.updateWithOnConflict(
            modelClass.tableName(),
            cv,
            where,
            whereArgs,
            onConflict
    )
    fun remove(
            where: String? = null,
            whereArgs: Array<String>? = null
    ): Int = writableDatabase.delete(
            modelClass.tableName(),
            where,
            whereArgs
    )

    private val transactionSync = Object()
    fun beginTransaction() {
        synchronized(transactionSync, {
            while(writableDatabase.inTransaction()) {
                transactionSync.wait()
            }
            writableDatabase.beginTransaction()
        })
    }

    fun abortTransaction() {
        synchronized(transactionSync, {
            writableDatabase.endTransaction()
            transactionSync.notify()
        })
    }

    fun acceptTransaction() {
        synchronized(transactionSync, {
            writableDatabase.setTransactionSuccessful()
            writableDatabase.endTransaction()
            transactionSync.notify()
        })
    }
}
