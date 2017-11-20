package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class RecyclerViewAdapter<T>(
        private val viewHolderFactory: (
                parent: ViewGroup,
                viewType: Int,
                adapter: RecyclerViewAdapter<T>
        ) -> AbstractViewHolder<T>,
        private val viewTypeFactory: (
                index: Int,
                current: T
        ) -> Int = { _, _ -> 0 },
        private val data: MutableList<T> = ArrayList()
): RecyclerView.Adapter<AbstractViewHolder<T>>() {
    var emptyView: View? = null
        set(value) {
            field = value
            checkEmpty()
        }

    init {
        registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        super.onItemRangeChanged(positionStart, itemCount)
                        checkEmpty()
                    }

                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                        super.onItemRangeChanged(positionStart, itemCount, payload)
                        checkEmpty()
                    }

                    override fun onChanged() {
                        super.onChanged()
                        checkEmpty()
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        super.onItemRangeRemoved(positionStart, itemCount)
                        checkEmpty()
                    }

                    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                        checkEmpty()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        checkEmpty()
                    }
                }
        )
        checkEmpty()
    }

    override fun getItemViewType(position: Int): Int
            = viewTypeFactory(position, data[position])

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: AbstractViewHolder<T>, position: Int) {
        holder.refreshItem(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder<T> =
            viewHolderFactory(parent!!, viewType, this)

    fun addItems(vararg items: T) {
        data.addAll(items)
        notifyItemRangeChanged(data.size - items.size, data.size)
    }

    fun removeItem(i: Int) {
        data.removeAt(i)
        notifyItemRangeChanged(i, data.size)
    }

    fun removeItem(vararg items: T) {
        items.forEach {
            data.remove(it)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    private fun checkEmpty() {
        emptyView ?. let {
            if (data.isEmpty()) {
                launch(UI) {
                    it.visibility = View.VISIBLE
                }
            } else {
                launch(UI) {
                    it.visibility = View.GONE
                }
            }
        }
    }
}
