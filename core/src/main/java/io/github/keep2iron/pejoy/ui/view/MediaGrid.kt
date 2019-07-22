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
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.utilities.getThemeColor

class MediaGrid @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SquareFrameLayout(
    context,
    attrs,
    defStyleAttr
), View.OnClickListener {

    private lateinit var mThumbnail: View
    private lateinit var mCheckView: CheckView
    private lateinit var mGifTag: ImageView
    private lateinit var mVideoDuration: TextView

    lateinit var media: Item
    private lateinit var mPreBindInfo: PreBindInfo
    private var mListener: OnMediaGridClickListener? = null
    //    private var selected = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.pejoy_widget_media_grid, this, true)

        mThumbnail = SelectionSpec.instance.requireImageEngine().provideImageView(context)
        mThumbnail.layoutParams =
            RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        addView(mThumbnail, 0)

        mCheckView = findViewById(R.id.check_view)
        mGifTag = findViewById(R.id.gif)
        mVideoDuration = findViewById(R.id.video_duration)

        mThumbnail.setOnClickListener(this)
        mCheckView.setOnClickListener(this)

        setWillNotDraw(true)

        paint.color = getThemeColor(
            context,
            R.attr.pejoy_item_checkCircle_borderColor,
            R.color.pejoy_dracula_item_checkCircle_borderColor
        )
        paint.style = Paint.Style.STROKE
    }

    override fun onClick(v: View) {
        if (mListener != null) {
            if (v === mThumbnail) {
                mListener!!.onThumbnailClicked(mThumbnail, media, mPreBindInfo.mViewHolder, mPreBindInfo.mPosition)
            } else if (v === mCheckView) {
                mListener!!.onCheckViewClicked(mCheckView, media, mPreBindInfo.mViewHolder, mPreBindInfo.mPosition)
            }
        }
    }

    fun preBindMedia(info: PreBindInfo) {
        mPreBindInfo = info
    }

    fun bindMedia(item: Item) {
        media = item
        setGifTag()
        initCheckView()
        setImage()
        setVideoDuration()
    }

    private fun setGifTag() {
        mGifTag.visibility = if (media.isGif) View.VISIBLE else View.GONE
    }

    private fun initCheckView() {
        mCheckView.setCountable(mPreBindInfo.mCheckViewCountable)
    }

    fun setCheckEnabled(enabled: Boolean) {
        mCheckView.isEnabled = enabled
    }

    fun setCheckedNum(checkedNum: Int) {
        mCheckView.setCheckedNum(checkedNum)
    }

    fun setChecked(checked: Boolean) {
        mCheckView.setChecked(checked)
    }

    fun setMediaSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    private fun setImage() {
        if (media.isGif) {
            SelectionSpec.instance.requireImageEngine().loadGifThumbnail(
                context, mPreBindInfo.mResize,
                mPreBindInfo.mPlaceholder, mThumbnail, media.contentUri
            )
        } else {
            SelectionSpec.instance.requireImageEngine().loadThumbnail(
                context, mPreBindInfo.mResize,
                mPreBindInfo.mPlaceholder,
                mThumbnail,
                media.contentUri
            )
        }
    }

    private fun setVideoDuration() {
        if (media.isVideo) {
            mVideoDuration.visibility = View.VISIBLE
            mVideoDuration.text = DateUtils.formatElapsedTime(media.duration / 1000)
        } else {
            mVideoDuration.visibility = View.GONE
        }
    }

    fun setOnMediaGridClickListener(listener: OnMediaGridClickListener) {
        mListener = listener
    }

    fun removeOnMediaGridClickListener() {
        mListener = null
    }

    interface OnMediaGridClickListener {

        fun onThumbnailClicked(thumbnail: View, item: Item, holder: RecyclerView.ViewHolder, position: Int)

        fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder, position: Int)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (isSelected) {
            paint.strokeWidth = 3 * resources.displayMetrics.density
            canvas.drawRect(
                paint.strokeWidth / 2,
                paint.strokeWidth / 2,
                width - paint.strokeWidth / 2,
                height - paint.strokeWidth / 2, paint
            )
        }
    }

    class PreBindInfo(
        internal var mResize: Int,
        internal var mPlaceholder: Drawable?,
        internal var mCheckViewCountable: Boolean,
        internal var mViewHolder: RecyclerView.ViewHolder,
        internal var mPosition: Int
    )

}