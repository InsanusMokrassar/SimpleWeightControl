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

fun Uri.center(): String = pathSegments[0]
fun Uri.database(): String = pathSegments[1]
fun Uri.modelFullClasspath(): String = pathSegments[2]
fun Uri.version(): Int = pathSegments[3].toInt()
fun Uri.rowId(): String? =
        try {
            pathSegments[4]
        } catch (e: IndexOutOfBoundsException) {
            null
        }

fun providerUri(
        center: String,
        database: String,
        modelClass: String?,
        version: Int = 1,
        rowId: Long? = null
): Uri = Uri.parse("content://$center/$database/$modelClass/$version${rowId ?.let { "/$it" }}")

class CommonSQLiteContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        Log.i(TAG(), "Created")
        return true
    }

    private val openHelpers = HashMap<KClass<*>, KClassSQLiteOpenHelper<*>>()

    private fun getHelper(
            uri: Uri
    ): KClassSQLiteOpenHelper<*> {
        val modelClass = Class.forName(uri.modelFullClasspath()).kotlin
        val databaseName = uri.database()
        val version = uri.version()
        return openHelpers[modelClass] ?: {
            openHelpers.put(
                    modelClass,
                    KClassSQLiteOpenHelper(
                            modelClass,
                            context,
                            databaseName,
                            version
                    )
            )
            getHelper(uri)
        }()
    }

    override fun insert(uri: Uri, cv: ContentValues): Uri =
            providerUri(
                    uri.center(),
                    uri.database(),
                    uri.modelFullClasspath(),
                    uri.version(),
                    getHelper(uri).insert(cv)
            )

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
        return super.query(uri, projection, queryArgs, cancellationSignal)
    }

    override fun update(
            uri: Uri,
            cv: ContentValues,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int = getHelper(uri).update(cv, selection, selectionArgs)

    override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int = getHelper(uri).remove(selection, selectionArgs)

    override fun getType(uri: Uri): String =
            "vnd.android.cursor.${uri.rowId() ?.let { "item" } ?: "dir" }/${uri.database()}.${uri.modelFullClasspath()}"

}
