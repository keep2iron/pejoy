package io.github.keep2iron.pejoy.utilities

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat

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