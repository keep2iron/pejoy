package io.github.keep2iron.pejoy.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.ui.PreviewItemFragment
import java.util.ArrayList

class PreviewFragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    private val items = ArrayList<Item>()

    override fun getItem(position: Int): Fragment = PreviewItemFragment.newInstance(items[position])

    override fun getCount(): Int = items.size

    fun getMediaItem(position: Int) = items[position]

    fun addAll(items: List<Item>) {
        this.items.addAll(items)
    }
}