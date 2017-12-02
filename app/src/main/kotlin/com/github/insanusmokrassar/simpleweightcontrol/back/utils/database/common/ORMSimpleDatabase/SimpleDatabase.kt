package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ORMSimpleDatabase

import android.content.Context
import android.net.Uri
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.*
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ContentProvider.providerUri
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

fun buildLimit(offset: Int? = null, limit: Int = 10): String {
    return offset ?. let {
        "$offset,$limit"
    } ?: limit.toString()
}

open class SimpleDatabase<M: Any> (
        private val modelClass: KClass<M>,
        context: Context,
        private val databaseName: String,
        private val version: Int,
        private val defaultOrderBy: String? = null
) {
    private val contextReference = WeakReference(context)

    fun insert(value: M): Boolean {
        return contextReference.get() ?.let {
            it.contentResolver.insert(
                    buildProviderUri(it),
                    value.toContentValues()
            ) != null
        } ?: throw IllegalArgumentException("Context was destroyed")
    }

    fun find(
            where: String? = null,
            orderBy: String? = defaultOrderBy,
            offset: Int? = null,
            size: Int = 20
    ): List<M> {
        return contextReference.get() ?.let {
            val list = it.contentResolver.query(
                    buildProviderUri(it),
                    null,
                    where,
                    null,
                    orderBy
            ).extractAll(modelClass, true)
            offset?.let {
                list.subList(it, it + size)
            } ?: list
        } ?: throw IllegalArgumentException("Context was destroyed")
    }

    fun find(value: M): M? =
            find(value.getPrimaryFieldsSearchQuery()).firstOrNull()

    fun findPage(page: Int, size: Int, orderBy: String? = defaultOrderBy): List<M> =
            find(page * size,size, orderBy)

    fun find(page: Int, size: Int, orderBy: String? = defaultOrderBy): List<M> =
            find(null, orderBy, page * size, size)

    fun update(
            value: M,
            where: String? = value.getPrimaryFieldsSearchQuery()
    ): Boolean {
        return contextReference.get() ?.let {
            it.contentResolver.update(
                    buildProviderUri(it),
                    value.toContentValues(),
                    where,
                    null
            ) > 0
        } ?: throw IllegalArgumentException("Context was destroyed")
    }
    fun remove(where: String? = null): Boolean = remove(find(where))

    fun remove(vararg elements: M): Boolean = remove(listOf(*elements))

    fun remove(elements: Iterable<M>): Boolean {
        return contextReference.get() ?.let {
            it.contentResolver.delete(
                    buildProviderUri(it),
                    elements.getPrimaryFieldsSearchQuery(),
                    null
            ) > 0
        } ?: throw IllegalArgumentException("Context was destroyed")
    }

    fun size(where: String? = null): Long {
        return contextReference.get() ?.let {
            it.contentResolver.query(
                    buildProviderUri(it),
                    modelClass.getPrimaryFields().map { it.name }.toTypedArray(),
                    where,
                    null,
                    null
            ).count.toLong()
        } ?: throw IllegalArgumentException("Context was destroyed")
    }

    private fun buildProviderUri(c: Context): Uri =
            providerUri(
                    c.getString(R.string.contentProviderAuthority),
                    databaseName,
                    modelClass.tableName(),
                    version
            )
}
