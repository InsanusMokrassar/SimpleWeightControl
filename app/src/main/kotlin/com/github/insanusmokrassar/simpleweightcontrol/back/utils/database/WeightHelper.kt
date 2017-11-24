package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.MutableListDatabase
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.util.*
import kotlin.collections.HashSet

private val millisInDay: Long = 48*60*60*1000

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
): MutableListDatabase<WeightData>(
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
        val days = HashSet<Long>()
        forEach {
            days.add(extractDay(it.date))
        }
        return days
    }
}

fun extractDay(date: Long): Long = date - (date % millisInDay)
