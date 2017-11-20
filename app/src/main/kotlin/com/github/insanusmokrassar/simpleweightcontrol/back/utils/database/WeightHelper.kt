package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

////Temporary added as singleton, but must be extend to cache storage in some future update
//private var db: WeightHelper? = null
//
//fun Context.weightsDatabase(): WeightHelper {
//    return db ?. let {
//        it
//    } ?: {
//        db = WeightHelper(this)
//        weightsDatabase()
//    }()
//}

class WeightHelper internal constructor(
        c: Context
): MutableListDatabase<WeightData>(
        WeightData::class,
        c,
        c.getString(R.string.standardDatabaseName),
        1,
        "date DESC"
)
