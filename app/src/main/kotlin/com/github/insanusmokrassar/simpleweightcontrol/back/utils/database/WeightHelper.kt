package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import android.util.Log
import com.github.insanusmokrassar.androidutils.back.utils.database.SimpleORM.ContentProvider.CommonSQLiteContentObserver
import com.github.insanusmokrassar.androidutils.back.utils.database.SimpleORM.ORMSimpleDatabase.SimpleDatabase
import com.github.insanusmokrassar.androidutils.common.extensions.TAG
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val millisInHour: Long = 60L * 60L * 1000L
val millisInDay: Long = 24L * millisInHour

private var weightHelper: WeightHelper? = null

fun Context.weightHelper(): WeightHelper {
    return weightHelper ?.let {
        it
    } ?: {
        weightHelper = WeightHelper(this)
        weightHelper()
    }()
}

class WeightHelper internal constructor(
        c: Context
): SimpleDatabase<WeightData>(
        WeightData::class,
        c,
        c.getString(R.string.standardDatabaseName),
        1,
        "date DESC"
) {

    private val subject = PublishSubject.create<WeightHelper>()

    val observable: Observable<WeightHelper> = subject.debounce(
            200L,
            TimeUnit.MILLISECONDS
    )

    private val providerObserver = CommonSQLiteContentObserver {
        if (this.providerUri == it) {
            subject.onNext(this)
        }
    }

    init {
        c.contentResolver.registerContentObserver(
                providerUri,
                false,
                providerObserver
        )

        observable.subscribe {
            Log.i(this.TAG(), "Changed: $it")
        }
    }
}


private val offsetInMillis = Calendar.getInstance().timeZone.rawOffset * millisInHour
//TODO: HELP ME TO FIX IT
fun extractDay(date: Long): Long =
        dateFormatInstance.parse(getDateString(date)).time

private val dateFormatInstance = DateFormat.getDateInstance()
private val dateFormatShortInstance = DateFormat.getDateInstance(DateFormat.SHORT)
private val timeFormatInstance = DateFormat.getTimeInstance(DateFormat.SHORT)

fun getDateString(date: Long): String {
    return dateFormatInstance.format(
            Date(date)
    )
}

fun getShortDateString(date: Long): String {
    return dateFormatShortInstance.format(
            Date(date)
    )
}

fun getTimeString(date: Long): String {
    return timeFormatInstance.format(
            Date(date)
    )
}
