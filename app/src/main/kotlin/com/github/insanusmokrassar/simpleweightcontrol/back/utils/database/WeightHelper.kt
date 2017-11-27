package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.MutableListDatabase
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.SimpleDatabase
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
    fun getByDay(date: Long): List<WeightData> {
        val day = extractDay(date)
        return find("(date >= $day) AND (date < ${day + millisInDay})")
    }

    fun getDays(): Set<Long> {
        val days = LinkedHashSet<Long>()
        find().forEach {
            days.add(extractDay(it.date))
        }
        return days
    }
}

//TODO: HELP ME TO FIX IT
fun extractDay(date: Long): Long =
        dateFormatInstance.parse(getDateString(date)).time

private val dateFormatInstance = DateFormat.getDateInstance()
private val timeFormatInstance = SimpleDateFormat("HH:mm")

fun getDateString(date: Long): String {
    return dateFormatInstance.format(
            Date(date)
    )
}

fun getTimeString(date: Long): String {
    return timeFormatInstance.format(
            Date(date)
    )
}
