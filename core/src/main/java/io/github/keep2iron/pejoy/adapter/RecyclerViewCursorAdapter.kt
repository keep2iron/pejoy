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
import android.provider.MediaStore
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * @author keep2iron
 */
abstract class RecyclerViewCursorAdapter constructor(val context: Context, protected var cursor: Cursor?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mRowIDColumn: Int = 0

//    /**
//     * 获取布局的layout id
//     *
//     * @return layout id
//     */
//    @LayoutRes
//    abstract fun getLayoutId(): Int

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return ViewHolder(getLayoutId(), parent)
//    }


    /**
     * @param holder
     * @param position
     */
    abstract fun render(holder: RecyclerView.ViewHolder, cursor: Cursor?, position: Int)


    init {
        this.setHasStableIds(true)
        swapCursor(cursor)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isDataValid(cursor)) {
            throw IllegalStateException("Cannot bind view holder when cursor is in invalid state.")
        }
        if (!cursor!!.moveToPosition(position)) {
            throw IllegalStateException(
                "Could not move cursor to position " + position
                        + " when trying to bind view holder"
            )
        }

        render(holder, cursor, position)
    }

    override fun getItemViewType(position: Int): Int {
        if (!cursor!!.moveToPosition(position)) {
            throw IllegalStateException(
                "Could not move cursor to position " + position
                        + " when trying to get item view type."
            )
        }
        return getItemViewType(position, cursor)
    }

    protected abstract fun getItemViewType(position: Int, cursor: Cursor?): Int

    override fun getItemCount(): Int {
        return if (isDataValid(cursor)) {
            cursor?.count ?: 0
        } else {
            0
        }
    }

    override fun getItemId(position: Int): Long {
        if (!isDataValid(cursor)) {
            throw IllegalStateException("Cannot lookup item id when cursor is in invalid state.")
        }

        cursor?.let { cursor ->
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException(
                    "Could not move cursor to position " + position
                            + " when trying to get an item id"
                )
            }
        }

        return cursor!!.getLong(mRowIDColumn)
    }

    open fun swapCursor(newCursor: Cursor?) {
        if (newCursor === cursor) {
            return
        }

        if (newCursor != null) {
            cursor = newCursor
            mRowIDColumn = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, itemCount)
            mRowIDColumn = -1
            cursor = null
        }
    }

    protected fun isDataValid(cursor: Cursor?): Boolean {
        return cursor != null && !cursor.isClosed
    }
}