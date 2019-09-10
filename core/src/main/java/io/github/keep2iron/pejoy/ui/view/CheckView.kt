/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import io.github.keep2iron.pejoy.R

class CheckView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) :
  View(context, attrs, defStyleAttr) {
  private var mCountable: Boolean = false
  private var mChecked: Boolean = false
  private var mCheckedNum: Int = 0
  private var mStrokePaint: Paint? = null
  private var mBackgroundPaint: Paint? = null
  private var mTextPaint: TextPaint? = null
  private var mShadowPaint: Paint? = null
  private val mCheckDrawable: Drawable
  private var mDensity: Float = 0.toFloat()
  private var mEnabled = true

  // rect for drawing checked number or mark
  private val checkRect: Rect by lazy {
    val rectPadding = (SIZE * mDensity / 2 - CONTENT_SIZE * mDensity / 2).toInt()
    return@lazy Rect(
      rectPadding,
      rectPadding,
      (SIZE * mDensity - rectPadding).toInt(),
      (SIZE * mDensity - rectPadding).toInt()
    )
  }

  init {
    mDensity = context.resources.displayMetrics.density

    mStrokePaint = Paint()
    mStrokePaint!!.isAntiAlias = true
    mStrokePaint!!.style = Paint.Style.STROKE
//        mStrokePaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    mStrokePaint!!.strokeWidth = STROKE_WIDTH * mDensity
    val ta = getContext().theme.obtainStyledAttributes(
      intArrayOf(R.attr.pejoy_item_checkCircle_borderColor)
    )
    val defaultColor = ResourcesCompat.getColor(
      resources, R.color.pejoy_light_item_checkCircle_borderColor,
      getContext().theme
    )
    val color = ta.getColor(0, defaultColor)
    ta.recycle()
    mStrokePaint!!.color = color

    mCheckDrawable = ResourcesCompat.getDrawable(
      context.resources,
      R.drawable.pejoy_ic_check_white, context.theme
    )!!
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    // fixed size 48dp x 48dp
    val sizeSpec = MeasureSpec.makeMeasureSpec((SIZE * mDensity).toInt(), MeasureSpec.EXACTLY)
    super.onMeasure(sizeSpec, sizeSpec)
  }

  fun setChecked(checked: Boolean) {
    if (mCountable) {
      throw IllegalStateException("CheckView is countable, call setCheckedNum() instead.")
    }
    mChecked = checked
    invalidate()
  }

  fun setCountable(countable: Boolean) {
    mCountable = countable
  }

  fun setCheckedNum(checkedNum: Int) {
    if (!mCountable) {
      throw IllegalStateException("CheckView is not countable, call setChecked() instead.")
    }
    if (checkedNum != UNCHECKED && checkedNum <= 0) {
      throw IllegalArgumentException("checked num can't be negative.")
    }
    mCheckedNum = checkedNum
    invalidate()
  }

  override fun setEnabled(enabled: Boolean) {
    if (mEnabled != enabled) {
      mEnabled = enabled
      invalidate()
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // draw outer and inner shadow
    initShadowPaint()
    canvas.drawCircle(
      SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
      (STROKE_RADIUS + STROKE_WIDTH / 2 + SHADOW_WIDTH) * mDensity, mShadowPaint!!
    )

    // draw white stroke
    canvas.drawCircle(
      SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
      STROKE_RADIUS * mDensity, mStrokePaint!!
    )

    // draw content
    if (mCountable) {
      if (mCheckedNum != UNCHECKED) {
        initBackgroundPaint()
        canvas.drawCircle(
          SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
          BG_RADIUS * mDensity, mBackgroundPaint!!
        )
        initTextPaint()
        val text = mCheckedNum.toString()
        val baseX = (width - mTextPaint!!.measureText(text)).toInt() / 2
        val baseY = (height.toFloat() - mTextPaint!!.descent() - mTextPaint!!.ascent()).toInt() / 2
        canvas.drawText(text, baseX.toFloat(), baseY.toFloat(), mTextPaint!!)
      }
    } else {
      if (mChecked) {
        initBackgroundPaint()
        canvas.drawCircle(
          SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
          BG_RADIUS * mDensity, mBackgroundPaint!!
        )

        mCheckDrawable.bounds = checkRect
        mCheckDrawable.draw(canvas)
      }
    }

    // enable hint
    alpha = if (mEnabled) 1.0f else 0.5f
  }

  private fun initShadowPaint() {
    if (mShadowPaint == null) {
      mShadowPaint = Paint()
      mShadowPaint!!.isAntiAlias = true
      // all in dp
      val outerRadius = STROKE_RADIUS + STROKE_WIDTH / 2
      val innerRadius = outerRadius - STROKE_WIDTH
      val gradientRadius = outerRadius + SHADOW_WIDTH
      val stop0 = (innerRadius - SHADOW_WIDTH) / gradientRadius
      val stop1 = innerRadius / gradientRadius
      val stop2 = outerRadius / gradientRadius
      val stop3 = 1.0f
      mShadowPaint!!.shader = RadialGradient(
        SIZE.toFloat() * mDensity / 2,
        SIZE.toFloat() * mDensity / 2,
        gradientRadius * mDensity,
        intArrayOf(
          Color.parseColor("#00000000"),
          Color.parseColor("#00000000"),
          Color.parseColor("#0D000000"),
          Color.parseColor("#0D000000"),
          Color.parseColor("#00000000")
        ),
        floatArrayOf(0f, stop0, stop1, stop2, stop3),
        Shader.TileMode.CLAMP
      )
    }
  }

  private fun initBackgroundPaint() {
    if (mBackgroundPaint == null) {
      mBackgroundPaint = Paint()
      mBackgroundPaint!!.isAntiAlias = true
      mBackgroundPaint!!.style = Paint.Style.FILL
      val ta = context.theme
        .obtainStyledAttributes(intArrayOf(R.attr.pejoy_item_checkCircle_backgroundColor))
//      val defaultColor = ResourcesCompat.getColor(
//        resources, R.color.pejoy_light_item_checkCircle_backgroundColor,
//        context.theme
//      )
      val color = ta.getColor(0, Color.WHITE)
      ta.recycle()
      mBackgroundPaint!!.color = color
    }
  }

  private fun initTextPaint() {
    if (mTextPaint == null) {
      mTextPaint = TextPaint()
      mTextPaint!!.isAntiAlias = true
      mTextPaint!!.color = Color.WHITE
      mTextPaint!!.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      mTextPaint!!.textSize = 12.0f * mDensity
    }
  }

  companion object {

    const val UNCHECKED = Integer.MIN_VALUE
    private const val STROKE_WIDTH = 3f // dp
    private const val SHADOW_WIDTH = 6.0f // dp
    private const val SIZE = 48 // dp
    private const val STROKE_RADIUS = 11.5f // dp
    private const val BG_RADIUS = 11.0f // dp
    private const val CONTENT_SIZE = 16 // dp
  }
}