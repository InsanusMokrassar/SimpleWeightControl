package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.getDateString
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.calculateAverage
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.AbstractViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import kotlin.collections.ArrayList

class WeightDateHolderAdapter(
        inflater: LayoutInflater,
        container: ViewGroup
) : AbstractViewHolder<Pair<Long, List<WeightData>>>({
    val view = inflater.inflate(R.layout.item_weight_date, container, false)
    view.findViewById<RecyclerView>(R.id.weightsItemRecyclerView).setHasFixedSize(true)
    view
}) {
    private var currentPair: Pair<Long, List<WeightData>>? = null
        set(value) {
            field = value
            currentList.clear()
            value ?. second ?.let { currentList.addAll(it) }
        }
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

    override fun refreshItem(item: Pair<Long, List<WeightData>>) {
        currentPair = item
        if (currentList.isNotEmpty()) {
            currentPair ?.let {
                itemView.findViewById<TextView>(R.id.weightDateTextView).text = getDateString(
                        it.first
                )

                itemView.findViewById<TextView>(R.id.averageWeightTextView).text = String.format(
                        "%.1f",
                        it.second.calculateAverage()
                )
            }
        }
        adapter.notifyDataSetChanged()
    }

}