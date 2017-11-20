package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog
import kotlinx.coroutines.experimental.async
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.*

class WeightViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup,
        adapter: RecyclerViewAdapter<WeightData>
): AbstractViewHolder<WeightData>({
    inflater.inflate(android.R.layout.simple_list_item_2, container, false)
}) {
    private var currentItem: WeightData? = null
    private val adapterWeakReference = WeakReference<RecyclerViewAdapter<WeightData>>(adapter)

    override fun refreshItem(item: WeightData) {
        currentItem ?: {
            itemView.setOnClickListener {
                val context = it.context
                context.createEditWeightDialog(
                        currentItem,
                        {
                            refreshItem(it)
                            async {
                                WeightHelper(context).update(it)
                            }
                        },
                        {
                            WeightHelper(context).remove(it)
                            adapterWeakReference.get() ?. let {
                                it.notifyDataSetChanged()
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
