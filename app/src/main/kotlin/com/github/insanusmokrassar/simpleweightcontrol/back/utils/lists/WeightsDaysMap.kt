package com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists

import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ORMSimpleDatabase.SimpleDatabase
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.extractDay
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.millisInDay
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.util.LinkedHashSet

private fun SimpleDatabase<WeightData>.getByDay(date: Long): List<WeightData> {
    val day = extractDay(date)
    return find("(date >= $day) AND (date <= ${day + millisInDay})")
}

private fun SimpleDatabase<WeightData>.getDays(): Set<Long> {
    val days = LinkedHashSet<Long>()
    find().forEach {
        days.add(extractDay(it.date))
    }
    return days
}

class WeightsDaysMap : Map<Long, List<WeightData>>, HashMap<Long, List<WeightData>>() {
    fun refresh(weights: List<WeightData>) {
        clear()
        weights.forEach {
            val date = extractDay(it.date)
            val list = get(date) ?. plus(it) ?: arrayListOf(it)
            set(date, list)
        }
    }

    fun pairs(): List<Pair<Long, List<WeightData>>> = map { Pair(it.key, it.value) }
}

fun List<WeightData>.calculateAverage(): Float {

    var average = 0F
    forEach {
        average += it.weight
    }
    average /= size

    return average
}

fun List<WeightData>.getDate(): Long {
    var date: Long? = null
    forEach {
        val currentDate = extractDay(it.date)
        if (currentDate != date) {
            date ?.let {
                throw IllegalArgumentException("All objects from list must have one day")
            }
            date = currentDate
        }
    }
    return date!!
}
