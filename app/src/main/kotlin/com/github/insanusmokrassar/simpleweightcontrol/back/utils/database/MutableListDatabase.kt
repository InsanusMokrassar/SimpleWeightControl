package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import android.util.Log
import kotlin.reflect.KClass

open class MutableListDatabase<M: Any> (
        modelClass: KClass<M>,
        context: Context,
        databaseName: String,
        version: Int,
        defaultOrderBy: String? = null
) : SimpleDatabase<M>(modelClass, context, databaseName, version, defaultOrderBy), MutableList<M> {
    override val size: Int
        get() = size().toInt()

    override fun contains(element: M): Boolean = find(element) != null

    override fun containsAll(elements: Collection<M>): Boolean =
            find(elements.getPrimaryFieldsSearchQuery()).size == elements.size

    override fun get(index: Int): M =
            findPage(index, 1, defaultOrderBy).firstOrNull() ?: throw IndexOutOfBoundsException("Index: $index, db size: $size")

    override fun indexOf(element: M): Int {
        forEachIndexed { index, m -> if (m == element) return index }
        throw IllegalArgumentException("Object was not found in the database")
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<M> = MutableDatabaseIterator(this)

    override fun lastIndexOf(element: M): Int = indexOf(element)

    override fun add(element: M): Boolean = insert(element)

    override fun add(index: Int, element: M) {
        writableDatabase.beginTransaction()
        try {
            val after = find(index, size - index, defaultOrderBy)
            removeAll(after)
            mutableListOf(element).plus(after).forEach {
                insert(it)
            }
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(MutableListDatabase::class.java.simpleName, e.message, e)
        }
        writableDatabase.endTransaction()
    }

    override fun addAll(index: Int, elements: Collection<M>): Boolean {
        writableDatabase.beginTransaction()
        try {
            val after = find(index, size - index, defaultOrderBy)
            removeAll(after)
            elements.plus(after).forEach {
                insert(it)
            }
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(MutableListDatabase::class.java.simpleName, e.message, e)
            writableDatabase.endTransaction()
            return false
        }
        writableDatabase.endTransaction()
        return true
    }

    override fun addAll(elements: Collection<M>): Boolean {
        writableDatabase.beginTransaction()
        try {
            elements.forEach {
                insert(it)
            }
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(MutableListDatabase::class.java.simpleName, e.message, e)
            writableDatabase.endTransaction()
            return false
        }
        writableDatabase.endTransaction()
        return true
    }

    override fun clear() {
        remove()
    }

    override fun listIterator(): MutableListIterator<M> = MutableDatabaseListIterator(this)

    override fun listIterator(index: Int): MutableListIterator<M> {
        val listIterator = listIterator()
        while (listIterator.hasNext() && listIterator.nextIndex() != index + 1) {
            listIterator.next()
        }
        return listIterator
    }

    override fun removeAll(elements: Collection<M>): Boolean =
            remove(elements.getPrimaryFieldsSearchQuery())

    override fun removeAt(index: Int): M {
        val toDelete = get(index)
        remove(toDelete)
        return toDelete
    }

    override fun retainAll(elements: Collection<M>): Boolean {
        return removeAll(
                this.filter {
                    !elements.contains(it)
                }
        )
    }

    override fun set(index: Int, element: M): M {
        val old = get(index)
        return if (update(element, old.getPrimaryFieldsSearchQuery())) {
            element
        } else {
            old
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<M> =
            find(orderBy = defaultOrderBy, limit = buildLimit(fromIndex, toIndex - fromIndex)).toMutableList()

}

private open class MutableDatabaseIterator<T: Any>(
        protected val dbList: MutableListDatabase<T>,
        protected val pageSize: Int = 20
): MutableIterator<T> {

    protected var currentPage = -1
    protected var currentList = ArrayList<T>(pageSize)
    protected var currentObject: T? = null

    override fun hasNext(): Boolean {
        return if (currentList.isNotEmpty()) {
            true
        } else {
            currentPage++
            refillList()
            currentList.isNotEmpty()
        }
    }

    override fun next(): T {
        currentObject = currentList.removeAt(0)
        return currentObject!!
    }

    override fun remove() {
        currentObject ?. let {
            dbList.remove(it)
        }
    }

    protected fun refillList() {
        currentList.clear()
        currentList.addAll(dbList.findPage(currentPage, pageSize))
    }
}

private class MutableDatabaseListIterator<T: Any>(
        dbList: MutableListDatabase<T>,
        pageSize: Int = 20
): MutableListIterator<T>, MutableDatabaseIterator<T>(dbList, pageSize) {
    private val index: Int
        get() = currentPage * pageSize + (pageSize - currentList.size)
    private var previous: T? = null

    override fun next(): T {
        previous = currentObject
        return super.next()
    }

    override fun hasPrevious(): Boolean = previous != null

    override fun nextIndex(): Int = index + 1

    override fun previous(): T = previous!!

    override fun previousIndex(): Int = index - 1

    override fun add(element: T) {
        val currentSize = currentList.size
        dbList.add(index, element)
        refillList()
        while (currentList.size > currentSize) {
            currentList.removeAt(0)
        }
    }

    override fun set(element: T) {
        dbList[index] = element
        currentObject = element
    }
}
