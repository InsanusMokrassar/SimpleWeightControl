package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

//Temporary added as singleton, but must be extend to cache storage in some future update
private var db: WeightHelper? = null

fun Context.weightsDatabase(): WeightHelper {
    return db ?. let {
        it
    } ?: {
        db = WeightHelper(this)
        weightsDatabase()
    }()
}

class WeightHelper internal constructor(
        c: Context
): SimpleDatabase<WeightData>(
        WeightData::class,
        c,
        c.getString(R.string.standardDatabaseName),
        1
) {
    private val upsertSubject = PublishSubject.create<WeightData>()
    val upsertObservable: Observable<WeightData>
        get() = upsertSubject

    private val removeSubject = PublishSubject.create<WeightData>()
    val removeObservable: Observable<WeightData>
        get() = removeSubject

    fun lastWeights(
            page: Int = 0,
            size: Int = 20
    ): List<WeightData> = find(
            null,
            "date DESC",
            "${page * size},$size"
    )

    override fun insert(value: WeightData): Boolean {
        if (super.insert(value)) {
            upsertSubject.onNext(value)
            return true
        }
        return false
    }

    override fun update(value: WeightData, where: String?, onConflict: Int): Boolean {
        if (super.update(value, where, onConflict)) {
            upsertSubject.onNext(value)
            return true
        }
        return false
    }

    override fun remove(value: WeightData) {
        super.remove(value)
        removeSubject.onNext(value)
    }
}
