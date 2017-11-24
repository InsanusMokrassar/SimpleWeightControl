package com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists

import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.extractDay
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData

class WeightsDaysList(
        private val db: WeightHelper,
        private val firstDay: Long = 0,
        private val lastDay: Long = Long.MAX_VALUE
): List<List<WeightData>> {
    private val cache = ArrayList<Long>(db.getDays().filter { it in firstDay..lastDay })

    init {
        db.databaseObserver.subscribe {
            synchronized(cache, {
                cache.clear()
                cache.addAll(db.getDays().filter { it in firstDay..lastDay })
            })
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

    override fun containsAll(elements: Collection<List<WeightData>>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
