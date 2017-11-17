package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.reflect.KClass

open class SimpleDatabase<M: Any> (
        private val modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int
):
        SQLiteOpenHelper(
        context,
        databaseName,
        null,
        version
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.createTableIfNotExist(modelClass)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //
        // This will throw exception if you upgrade version of database but not
        // override onUpgrade
    }

    fun insert(value: M): Boolean {
        return writableDatabase.insert(
                modelClass.tableName(),
                null,
                value.toContentValues()
        ) > 0
    }

    fun find(
            where: String? = null,
            orderBy: String? = null,
            limit: String? = null
    ): List<M> {
        return readableDatabase.query(
                modelClass.tableName(),
                null,
                where,
                null,
                null,
                null,
                orderBy,
                limit
        ).extractAll(modelClass, true)
    }

    fun update(
            value: M,
            where: String? = value.getPrimaryFieldsSearchQuery(),
            onConflict: Int = SQLiteDatabase.CONFLICT_REPLACE
    ): Boolean {
        return writableDatabase.updateWithOnConflict(
                modelClass.tableName(),
                value.toContentValues(),
                where,
                null,
                onConflict
        ) > 0
    }

    fun remove(where: String? = null) {
        writableDatabase.delete(
                modelClass.tableName(),
                where,
                null
        )
    }

    fun remove(value: M) {
        writableDatabase.delete(
                modelClass.tableName(),
                value.getPrimaryFieldsSearchQuery(),
                null
        )
    }
}
