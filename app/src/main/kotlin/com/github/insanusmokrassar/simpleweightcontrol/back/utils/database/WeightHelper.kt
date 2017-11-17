package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData

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
    fun lastWeights(
            page: Int = 0,
            size: Int = 20
    ): List<WeightData> = find(
            null,
            "date DESC",
            "${page * size},$size"
    )
}
