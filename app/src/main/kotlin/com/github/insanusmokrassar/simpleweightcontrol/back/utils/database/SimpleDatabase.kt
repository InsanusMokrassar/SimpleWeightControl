package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.reflect.KClass

fun buildLimit(offset: Int? = null, limit: Int = 10): String {
    return offset ?. let {
        "$offset,$limit"
    } ?: limit.toString()
}

open class SimpleDatabase<M: Any> (
        protected val modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int,
        protected val defaultOrderBy: String? = null
): SQLiteOpenHelper(context, databaseName, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.createTableIfNotExist(modelClass)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //
        // This will throw exception if you upgrade version of database but not
        // override onUpgrade
    }

    open fun insert(value: M): Boolean {
        return writableDatabase.insert(
                modelClass.tableName(),
                null,
                value.toContentValues()
        ) > 0
    }

    open fun find(
            where: String? = null,
            orderBy: String? = defaultOrderBy,
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

    open fun find(
            value: M
    ): M? = find(value.getPrimaryFieldsSearchQuery()).firstOrNull()

    open fun findPage(
            page: Int, size: Int, orderBy: String? = defaultOrderBy
    ): List<M> = find(page * size,size, orderBy)

    open fun find(
            offset: Int, size: Int, orderBy: String? = defaultOrderBy
    ): List<M> = find(orderBy = orderBy, limit = buildLimit(offset, size))

    open fun update(
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

    open fun remove(where: String? = null): Boolean {
        return writableDatabase.delete(
                modelClass.tableName(),
                where,
                null
        ) > 0
    }

    open fun remove(element: M): Boolean {
        return writableDatabase.delete(
                modelClass.tableName(),
                element.getPrimaryFieldsSearchQuery(),
                null
        ) > 0
    }

    open fun size(where: String? = null): Long {
        return DatabaseUtils.queryNumEntries(
                readableDatabase,
                modelClass.tableName(),
                where,
                null
        )
    }
}
