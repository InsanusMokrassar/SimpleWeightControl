package com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists

import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.extractDay
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData

class WeightsDaysList(
        private val db: WeightHelper,
        private val firstDay: Long = 0,
        private val lastDay: Long = Long.MAX_VALUE
): List<List<WeightData>> {
    private val cache = ArrayList<Long>()

    private val updateCache = {
        cache.clear()
        cache.addAll(db.getDays().filter { it in firstDay..lastDay })
    }

    init {
        updateCache()
        db.databaseObserver.subscribe {
            synchronized(this, updateCache)
        }
    }

    override val size: Int
        get() = cache.size

    override fun contains(element: List<WeightData>): Boolean {
        val index = indexOf(element)
        return if (index == -1) {
            false
        } else {
            get(index).containsAll(element)
        }
    }

    override fun containsAll(elements: Collection<List<WeightData>>): Boolean =
            elements.firstOrNull { !contains(it) } == null

    override fun get(index: Int): List<WeightData> = db.getByDay(cache[index])

    override fun indexOf(element: List<WeightData>): Int {
        var day: Long? = null
        element.forEach {
            weightData ->
            day ?.let {
                if (extractDay(weightData.date) != it) {
                    throw IllegalArgumentException("All data must be related to one day")
                }
            } ?: {
                day = extractDay(weightData.date)
            }()
        }
        day ?.let {
            return cache.indexOf(it)
        }
        return -1
    }

    override fun isEmpty(): Boolean = cache.isEmpty()

    override fun iterator(): Iterator<List<WeightData>> =
            WeightsDaysListIterator(db, cache)

    override fun lastIndexOf(element: List<WeightData>): Int =
            indexOf(element)

    override fun listIterator(): ListIterator<List<WeightData>> =
            WeightsDaysListIterator(db, cache)

    override fun listIterator(index: Int): ListIterator<List<WeightData>> =
            WeightsDaysListIterator(db, cache.subList(index, cache.size))

    override fun subList(fromIndex: Int, toIndex: Int): List<List<WeightData>> =
            WeightsDaysList(db, cache[fromIndex], cache[toIndex - 1])
}

private class WeightsDaysListIterator(
        private val db: WeightHelper,
        private val cache: List<Long>
): ListIterator<List<WeightData>> {
    private var index = -1

    private var current: List<WeightData>? = null
    private var previous: List<WeightData>? = null

    override fun hasNext(): Boolean = cache.size > index + 1

    override fun hasPrevious(): Boolean = previous == null

    override fun next(): List<WeightData> {
        index++
        previous = current
        current = db.getByDay(cache[index])
        current ?.let {
            return it
        }
        throw IndexOutOfBoundsException("Have no next list of weights")
    }

    override fun nextIndex(): Int = index + 1

    override fun previous(): List<WeightData> {
        previous ?.let {
            return it
        }
        throw IndexOutOfBoundsException("Have now previous list of weights")
    }

    override fun previousIndex(): Int = index - 1
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
        val currentDate = extractDay(first().date)
        if (currentDate != date) {
            date ?.let {
                throw IllegalArgumentException("All objects from list must have one day")
            }
            date = currentDate
        }
    }
    return date!!
}
