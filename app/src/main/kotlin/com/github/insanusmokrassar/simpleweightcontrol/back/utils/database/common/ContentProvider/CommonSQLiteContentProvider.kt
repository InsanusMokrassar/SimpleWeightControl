package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ContentProvider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.util.Log
import kotlin.reflect.KClass
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.TAG

fun Uri.center(): String = authority
fun Uri.database(): String = pathSegments[0]
fun Uri.modelClass(): KClass<*> = Class.forName(pathSegments[1]).kotlin
fun Uri.version(): Int = pathSegments[2].toInt()
fun Uri.rowId(): String? =
        try {
            val rowId = pathSegments[3]
            if (rowId != "null" && rowId.toLong() > 0) {
                rowId
            } else {
                null
            }
        } catch (e: IndexOutOfBoundsException) {
            null
        }

fun providerUri(
        center: String,
        database: String,
        modelClass: KClass<*>,
        version: Int = 1,
        rowId: Long? = null
): Uri = Uri.parse(
        "content://$center/$database/${modelClass.java.canonicalName}/$version${rowId ?.let { "/$it" } ?: ""}"
)

class CommonSQLiteContentProvider : ContentProvider() {
    private val openHelpers = HashMap<KClass<*>, KClassSQLiteOpenHelper<*>>()

    override fun onCreate(): Boolean {
        Log.i(TAG(), "Created")
        return true
    }

    private fun getHelper(
            uri: Uri
    ): KClassSQLiteOpenHelper<*> {
        val modelClass = uri.modelClass()
        return openHelpers[modelClass] ?: {
            openHelpers.put(
                    modelClass,
                    KClassSQLiteOpenHelper(
                            modelClass,
                            context,
                            uri.database(),
                            uri.version()
                    )
            )
            getHelper(uri)
        }()
    }

    override fun insert(uri: Uri, cv: ContentValues): Uri {
        val uriRow = providerUri(
                uri.center(),
                uri.database(),
                uri.modelClass(),
                uri.version(),
                getHelper(uri).insert(cv)
        )
        uri.rowId() ?.let {
            notifyDataChanged(uri)
        }
        return uriRow
    }

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            orderBy: String?
    ): Cursor =
            getHelper(uri).find(
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            queryArgs: Bundle?,
            cancellationSignal: CancellationSignal?
    ): Cursor {
        queryArgs ?.let {
            return getHelper(uri).find(
                    projection,
                    it.getString(ContentResolver.QUERY_ARG_SQL_SELECTION),
                    it.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS),
                    null,
                    null,
                    it.getString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER)
            )
        }
        return getHelper(uri).find(projection)
    }

    override fun update(
            uri: Uri,
            cv: ContentValues,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int {
        val result = getHelper(uri).update(cv, selection, selectionArgs)
        if (result > 0) {
            notifyDataChanged(uri)
        }
        return result
    }

    override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int {
        val result = getHelper(uri).remove(selection, selectionArgs)
        if (result > 0) {
            notifyDataChanged(uri)
        }
        return result
    }

    override fun getType(uri: Uri): String =
            "vnd.android.cursor.${
            uri.rowId() ?.let {
                "item"
            } ?: "dir"}/${uri.database()}.${uri.modelClass()}.${uri.version()}"

    private fun notifyDataChanged(uri: Uri) {
        context.contentResolver.notifyChange(uri, null)
    }
}
