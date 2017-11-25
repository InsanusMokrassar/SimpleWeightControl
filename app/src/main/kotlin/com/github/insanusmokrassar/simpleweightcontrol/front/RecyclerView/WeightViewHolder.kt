package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog
import kotlinx.coroutines.experimental.async
import java.text.SimpleDateFormat
import java.util.*

class WeightViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
): AbstractViewHolder<WeightData>({
    inflater.inflate(R.layout.item_weight, container, false)
}) {
    private var currentItem: WeightData? = null

    override fun refreshItem(item: WeightData) {
        currentItem ?: {
            itemView.setOnClickListener {
                val context = it.context
                context.createEditWeightDialog(
                        currentItem,
                        {
                            async {
                                currentItem = it
                                currentItem ?. let {
                                    context.weightHelper().update(it)
                                }
                            }
                        },
                        {
                            WeightHelper(context).remove(it)
                        }
                ).show()
            }
        }()
        currentItem = item
        itemView.findViewById<TextView>(android.R.id.text1).text = item.weight.toString()
        itemView.findViewById<TextView>(android.R.id.text2).text = SimpleDateFormat(
                "HH:mm"
        ).format(
                Date(item.date)
        )
    }
}
