package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.RecyclerViewAdapter.AbstractStandardViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.getTimeString
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog
import kotlinx.coroutines.experimental.async

class WeightViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
): AbstractStandardViewHolder<WeightData>(
        inflater,
        container,
        R.layout.item_weight
) {
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
                            context.weightHelper().remove(it)
                        }
                ).show()
            }
        }()
        currentItem = item
        itemView.findViewById<TextView>(android.R.id.text1).text = item.weight.toString()
        itemView.findViewById<TextView>(android.R.id.text2).text = getTimeString(item.date)
    }
}
