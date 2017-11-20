package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightsDatabase
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog
import kotlinx.coroutines.experimental.async
import java.text.DateFormat
import java.util.*

class WeightViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
): AbstractViewHolder<WeightData>({
    inflater.inflate(android.R.layout.simple_list_item_2, container, false)
}) {

    var currentItem: WeightData? = null

    override fun refreshItem(item: WeightData) {
        currentItem ?: {
            itemView.setOnClickListener {
                val context = it.context
                context.createEditWeightDialog(
                        currentItem,
                        {
                            refreshItem(it)
                            async {
                                context.weightsDatabase().update(
                                        it
                                )
                            }
                        }
                ).show()
            }
        }()
        currentItem = item
        itemView.findViewById<TextView>(android.R.id.text1).text = item.weight.toString()
        itemView.findViewById<TextView>(android.R.id.text2).text = DateFormat.getDateTimeInstance().format(Date(item.date))
    }
}
