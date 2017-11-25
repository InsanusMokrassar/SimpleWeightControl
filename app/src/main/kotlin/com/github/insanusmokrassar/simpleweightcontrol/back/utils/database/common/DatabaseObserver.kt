package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common

import android.os.FileObserver
import android.util.Log
import kotlinx.coroutines.experimental.async

class DatabaseObserver<T: Any>(
        private val db: SimpleDatabase<T>,
        filePath: String = db.readableDatabase.path
): FileObserver(filePath, MODIFY) {
    private val subscribers = HashSet<(SimpleDatabase<T>) -> Unit>()

    override fun onEvent(event: Int, path: String?) {
        async {
            subscribers.forEach {
                try {
                    it(db)
                } catch (e: Throwable) {
                    Log.e(DatabaseObserver::class.java.simpleName, "Can not notify subscriber: $it")
                }
            }
        }
    }

    fun subscribe(subscriber: (SimpleDatabase<T>) -> Unit) {
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: (SimpleDatabase<T>) -> Unit) {
        subscribers.remove(subscriber)
    }
}
