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
package io.github.keep2iron.pejoy.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection
import io.github.keep2iron.pejoy.ui.AlbumModel
import io.github.keep2iron.pejoy.ui.view.CheckView
import io.github.keep2iron.pejoy.ui.view.MediaGrid
import io.github.keep2iron.pejoy.utilities.getThemeDrawable

class AlbumMediaAdapter(
    context: Context,
    private val mSelectedCollection: SelectedItemCollection,
    private val mRecyclerView: RecyclerView,
    private val model: AlbumModel
) : RecyclerViewCursorAdapter(context, null), MediaGrid.OnMediaGridClickListener {

    private var currentShowItemPosition = 0
    private val selectionSpec = SelectionSpec.instance

    private val placeholder by lazy {
        getThemeDrawable(context, R.attr.pejoy_item_placeholder)
    }

    /**
     * 添加的集合的状态监听
     * @params Item -> 当前改变的item
     * @params Boolean 是否添加 true添加 false删除
     */
    private var onChangeSelectionItemListener: ((Item, Boolean) -> Unit)? = null


//    /**
//     * 点击相册item,返回上一个item
//     */
//    var onClickAlbumPreItemListener: ((Item?) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.pejoy_item_grid_album

    override fun render(holder: RecyclerView.ViewHolder, cursor: Cursor?, position: Int) {
        val mediaGrid = holder.itemView as MediaGrid
        val context = holder.itemView.context.applicationContext

        val item = Item.valueOf(cursor!!)
        mediaGrid.preBindMedia(
            MediaGrid.PreBindInfo(
                getImageResize(context),
                placeholder,
                mSelectionSpec.countable,
                holder,
                position
            )
        )
        mediaGrid.bindMedia(item)
        setCheckStatus(item, mediaGrid)

        mediaGrid.setOnMediaGridClickListener(this)

        setMediaGridCheckEnabled(item, mediaGrid)
    }

    private fun setMediaGridCheckEnabled(item: Item, mediaGrid: MediaGrid) {
        val selected = model.selectedItemCollection.isSelected(item)
        val dotSelectMore = model.selectedItemCollection.maxSelectableReached() && !selected
        val isDifferentType =
            (mSelectedCollection.collectionType == SelectedItemCollection.COLLECTION_IMAGE && item.isVideo) or
                    (mSelectedCollection.collectionType == SelectedItemCollection.COLLECTION_VIDEO && item.isImage)

        if (dotSelectMore || isDifferentType) {
            mediaGrid.foreground = ColorDrawable(Color.parseColor("#99ffffff"))
            mediaGrid.setCheckEnabled(false)
        } else {
            mediaGrid.foreground = null
            mediaGrid.setCheckEnabled(true)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            if (!isDataValid(cursor)) {
                throw IllegalStateException("Cannot bind view holder when cursor is in invalid state.")
            }
            if (!cursor!!.moveToPosition(position)) {
                throw IllegalStateException(
                    "Could not move cursor to position " + position
                            + " when trying to bind view holder"
                )
            }
            val gridView = holder.itemView as MediaGrid
            val item = Item.valueOf(cursor!!)

//            if (currentShowItem.id == item.id) {
//                gridView.setMediaSelected(true)
//            } else {
//                gridView.setMediaSelected(false)
//            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int, cursor: Cursor?): Int {
        return 0
    }

    private val mSelectionSpec: SelectionSpec = SelectionSpec.instance
    private var mImageResize: Int = 0

    private fun setCheckStatus(item: Item, mediaGrid: MediaGrid) {
        if (mSelectionSpec.countable) {
            val checkedNum = mSelectedCollection.checkedNumOf(item)
            if (checkedNum > 0) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setCheckedNum(checkedNum)
                }
            }
        } else {
            val selected = mSelectedCollection.isSelected(item)
            if (selected) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setChecked(true)
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setChecked(false)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setChecked(false)
                }
            }
        }
    }

    override fun swapCursor(newCursor: Cursor?) {
        currentShowItemPosition = 0
        super.swapCursor(newCursor)
    }

    override fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder, position: Int) {
        val gridView = holder.itemView as MediaGrid

        val selected = model.selectedItemCollection.isSelected(item)
        if (selected) {
            model.selectedItemCollection.remove(item)
            onChangeSelectionItemListener?.invoke(item, false)
            refreshSelection()
        } else {
            if (!model.selectedItemCollection.maxSelectableReached() && assertAddSelection(context, item)) {
                model.selectedItemCollection.add(item)
                onChangeSelectionItemListener?.invoke(item, true)
                refreshSelection()
            }
        }

        setCheckStatus(item, gridView)
    }

    override fun onThumbnailClicked(thumbnail: View, item: Item, holder: RecyclerView.ViewHolder, position: Int) {
        if (currentShowItemPosition != position) {
            notifyItemChanged(currentShowItemPosition, 0)
            currentShowItemPosition = position
            notifyItemChanged(position, 0)
        }
    }

    private fun assertAddSelection(context: Context, item: Item): Boolean {
        val cause = mSelectedCollection.isAcceptable(item)
        IncapableCause.handleCause(context, cause)
        return cause == null
    }

    private fun refreshSelection() {
        val layoutManager = mRecyclerView.layoutManager as GridLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        if (first == -1 || last == -1) {
            return
        }
        for (i in first..last) {
            cursor!!.moveToPosition(i)
            val item = Item.valueOf(cursor!!)
            val holder = mRecyclerView.findViewHolderForAdapterPosition(i)
            val mediaGrid = holder?.itemView as MediaGrid

            setMediaGridCheckEnabled(item, mediaGrid)

            if (selectionSpec.countable) {
                val checkedNum = mSelectedCollection.checkedNumOf(item)
                mediaGrid.setCheckedNum(checkedNum)
            }
        }
    }

    private fun getImageResize(context: Context): Int {
        if (mImageResize == 0) {
            val gridLayoutManager = mRecyclerView.layoutManager as GridLayoutManager
            val spanCount = gridLayoutManager.spanCount
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
                R.dimen.pejoy_media_grid_spacing
            ) * (spanCount - 1)
            mImageResize = availableWidth / spanCount
            mImageResize = (mImageResize * mSelectionSpec.thumbnailScale).toInt()
        }
        return mImageResize
    }


}