package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.text.DateFormat
import java.util.*

class WeightViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
): AbstractViewHolder<WeightData>({
    inflater.inflate(android.R.layout.simple_list_item_2, container, false)
}) {
    override fun refreshItem(item: WeightData) {
        itemView.findViewById<TextView>(android.R.id.text1).text = item.weight.toString()
        itemView.findViewById<TextView>(android.R.id.text2).text = DateFormat.getDateTimeInstance().format(Date(item.date))
    }
}
