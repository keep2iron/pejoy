package io.github.keep2iron.pejoy.utilities

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.v4.content.res.ResourcesCompat
import io.github.keep2iron.pejoy.R

fun getThemeColor(context: Context, @AttrRes attr: Int, @ColorRes defaultColor: Int): Int {
    val ta = context.theme.obtainStyledAttributes(intArrayOf(attr))
    val color = ta.getColor(
        0, ResourcesCompat.getColor(
            context.resources, defaultColor,
            context.theme
        )
    )
    ta.recycle()
    return color
}

fun getThemeDrawable(context: Context, @AttrRes attr: Int): Drawable? {
    val ta = context.theme.obtainStyledAttributes(intArrayOf(attr))
    val drawable = ta.getDrawable(0)
    ta.recycle()
    return drawable
}