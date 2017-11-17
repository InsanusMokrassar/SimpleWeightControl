package com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class AbstractViewHolder<in T>(viewCreator: () -> View) : RecyclerView.ViewHolder(viewCreator()) {
    abstract fun refreshItem(item: T)
}
