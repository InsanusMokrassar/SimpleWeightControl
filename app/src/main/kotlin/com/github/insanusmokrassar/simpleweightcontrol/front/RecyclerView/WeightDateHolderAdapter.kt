package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.calculateAverage
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.getDate
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeightDateHolderAdapter(
        inflater: LayoutInflater,
        container: ViewGroup
) : AbstractViewHolder<List<WeightData>>({
    val view = inflater.inflate(R.layout.item_weight_date, container, false)
    view.findViewById<RecyclerView>(R.id.weightsItemRecyclerView).setHasFixedSize(true)
    view
}) {
    private val currentList = ArrayList<WeightData>()
    private val adapter = RecyclerViewAdapter(
            {
                parent: ViewGroup,
                _: Int,
                _: RecyclerViewAdapter<WeightData> ->
                WeightViewHolder(
                        inflater,
                        parent
                )
            },
            currentList
    )

    init {
        itemView.findViewById<RecyclerView>(R.id.weightsItemRecyclerView).adapter = adapter
    }

    override fun refreshItem(item: List<WeightData>) {
        currentList.clear()
        currentList.addAll(item)
        if (currentList.isNotEmpty()) {
            itemView.findViewById<TextView>(R.id.weightDateTextView).text = DateFormat.getDateInstance().format(
                Date(item.getDate())
            )


            itemView.findViewById<TextView>(R.id.averageWeightTextView).text = String.format(
                    "%.1f",
                    currentList.calculateAverage()
            )
        }
        adapter.notifyDataSetChanged()
    }

}