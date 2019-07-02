package io.github.keep2iron.pejoy.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListView
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.adapter.AlbumCategoryAdapter
import io.github.keep2iron.pejoy.utilities.getThemeColor

class AlbumContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
), View.OnClickListener {

    private var hidden: Boolean = true

    private lateinit var listView: ListView

    private lateinit var adapter: AlbumCategoryAdapter

    private lateinit var background: View

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            val listHeight = (measuredHeight * 0.45f).toInt()
            listView.layoutParams.height = listHeight
            listView.translationY = listHeight.toFloat()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        background = View(context)
        background.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        background.setBackgroundColor(Color.parseColor("#7f000000"))
        background.visibility = View.GONE
        background.alpha = 0f
        background.setOnClickListener(this)

        listView = ListView(context)
        listView.overScrollMode = View.OVER_SCROLL_NEVER
        listView.setPadding(
            0,
            (resources.displayMetrics.density * 16).toInt(),
            0,
            0
        )
        listView.setBackgroundResource(R.drawable.pejoy_shape_album_category_background)
        listView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }

        addView(background)
        addView(listView)
    }

    fun setAdapter(adapter: AlbumCategoryAdapter) {
        this.adapter = adapter
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
        listView.onItemClickListener = listener
    }

    fun switch() {
        if (hidden) {
            show()
        } else {
            hidden()
        }
    }

    fun show() {
        if (!hidden) {
            return
        }

        listView.adapter = adapter
        listView.setSelection(0)
        listView.animate()
            .translationY(0f)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        background.animate()
            .alpha(1f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    background.visibility = View.VISIBLE
                }
            })
            .start()
        hidden = false
    }

    fun hidden() {
        if (hidden) {
            return
        }

        listView.animate()
            .translationY(listView.measuredHeight.toFloat())
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        background.animate()
            .alpha(0f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    background.visibility = View.GONE
                    listView.adapter = null
                }
            })
            .start()
        hidden = true
    }

    override fun onClick(view: View) {
        hidden()
    }

}