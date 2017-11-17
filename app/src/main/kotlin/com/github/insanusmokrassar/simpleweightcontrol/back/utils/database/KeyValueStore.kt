package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.IObjectK.exceptions.ReadException
import com.github.insanusmokrassar.IObjectK.interfaces.IObject
import com.github.insanusmokrassar.simpleweightcontrol.R

private val cache = HashMap<String, MutableMap<String, KeyValueStore>>()

fun Context.keyValueStore(
        name: String = getString(R.string.standardSharedPreferencesName)
): IObject<String> {
    val className = this::class.java.simpleName
    return if (cache[className] ?.get(name) == null) {
        cache.put(
                className,
                mutableMapOf(
                        Pair(
                                name,
                                KeyValueStore(this, name)
                        )
                )
        )
        keyValueStore(name)
    } else {
        cache[className]!![name]!!
    }
}

class KeyValueStore internal constructor (
        c: Context,
        preferencesName: String) : IObject<String> {
    private val sharedPreferences =
            c.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    private val cachedData = HashMap<String, String>()

    private val syncObject = Object()

    override fun put(key: String, value: String) {
        synchronized(syncObject, {
            sharedPreferences.edit()
                    .putString(key, value)
                    .apply()
            cachedData.put(key, value)
        })
    }

    override fun <T : String> get(key: String): T {
        synchronized(syncObject, {
            if (!cachedData.containsKey(key)) {
                val value = sharedPreferences.getString(key, null) ?: throw ReadException("Value is absent")
                cachedData.put(key, value)
            }
            return cachedData[key]!! as T
        })
    }

    override fun keys(): Set<String> {
        synchronized(syncObject, {
            return sharedPreferences.all.keys
        })
    }

    override fun putAll(toPutMap: Map<String, String>) {
        synchronized(syncObject, {
            val editor = sharedPreferences.edit()
            toPutMap.forEach {
                editor.putString(it.key, it.value)
                cachedData.put(it.key, it.value)
            }
            editor.apply()
        })
    }

    override fun remove(key: String) {
        synchronized(syncObject, {
            sharedPreferences.edit()
                    .remove(key)
                    .apply()
            cachedData.remove(key)
        })
    }
}
