package io.github.keep2iron.pejoy.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ViewHolder(resId: Int, viewParent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(viewParent.context.applicationContext).inflate(
            resId,
            viewParent,
            false
        )
    )