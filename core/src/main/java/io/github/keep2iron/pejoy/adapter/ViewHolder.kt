package io.github.keep2iron.pejoy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal class ViewHolder(
  resId: Int,
  viewParent: ViewGroup
) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(viewParent.context).inflate(
            resId,
            viewParent,
            false
        )
    )