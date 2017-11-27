package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

fun buildLimit(offset: Int? = null, limit: Int = 10): String {
    return offset ?. let {
        "$offset,$limit"
    } ?: limit.toString()
}

open class SimpleDatabase<M: Any> (
        private val modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int,
        private val defaultOrderBy: String? = null
): SQLiteOpenHelper(context, databaseName, null, version) {

    private val subject = PublishSubject.create<List<M>>()

    val commonObserver: Observable<List<M>>
        get() = subject.debounce(200L, TimeUnit.MILLISECONDS)

    val removeObserver = commonObserver.filter {
        it.firstOrNull() ?.let {
            find(it) == null
        } ?: false
    }

    val upsertObserver = commonObserver.filter {
        it.firstOrNull() ?.let {
            find(it) != null
        } ?: false
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTableIfNotExist(modelClass)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //
        // This will throw exception if you upgrade version of database but not
        // override onUpgrade
    }

    open fun insert(value: M): Boolean {
        val success = writableDatabase.insert(
                modelClass.tableName(),
                null,
                value.toContentValues()
        ) > 0
        if (success) {
            subject.onNext(listOf(value))
        }
        return success
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
        val success = writableDatabase.updateWithOnConflict(
                modelClass.tableName(),
                value.toContentValues(),
                where,
                null,
                onConflict
        ) > 0
        if (success) {
            subject.onNext(listOf(value))
        }
        return success
    }

    open fun remove(where: String? = null): Boolean = remove(find(where))

    open fun remove(vararg elements: M): Boolean = remove(listOf(*elements))

    open fun remove(elements: Iterable<M>): Boolean {
        val success = writableDatabase.delete(
                modelClass.tableName(),
                elements.getPrimaryFieldsSearchQuery(),
                null
        ) > 0
        if (success) {
            subject.onNext(elements.toList())
        }
        return success
    }

    open fun size(where: String? = null): Long {
        return DatabaseUtils.queryNumEntries(
                readableDatabase,
                modelClass.tableName(),
                where,
                null
        )
    }

    private val transactionSync = Object()
    fun beginTransaction() {
        while(writableDatabase.inTransaction()) {
            synchronized(transactionSync, { transactionSync.wait() })
        }
        writableDatabase.beginTransaction()
    }

    fun abortTransaction() {
        writableDatabase.endTransaction()
        synchronized(transactionSync, { transactionSync.notify() })
    }

    fun acceptTransaction() {
        writableDatabase.setTransactionSuccessful()
        writableDatabase.endTransaction()
        synchronized(transactionSync, { transactionSync.notify() })
    }
}
