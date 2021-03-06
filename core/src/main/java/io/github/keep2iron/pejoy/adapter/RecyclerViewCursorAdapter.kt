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
import androidx.recyclerview.widget.RecyclerView

/**
 * @author keep2iron
 */
abstract class RecyclerViewCursorAdapter constructor(
  val context: Context,
  protected var cursor: Cursor?
) :
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
  abstract fun render(
    holder: RecyclerView.ViewHolder,
    cursor: Cursor,
    position: Int
  )

  init {
    this.setHasStableIds(true)
    swapCursor(cursor)
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    check(isDataValid(cursor)) { "Cannot bind view holder when cursor is in invalid state." }

    check(cursor!!.moveToPosition(position)) { "Could not move cursor to position $position when trying to bind view holder" }

    render(holder, cursor!!, position)
  }

  override fun getItemViewType(position: Int): Int {
    check(cursor!!.moveToPosition(position)) { "Could not move cursor to position $position when trying to get item view type." }
    return getItemViewType(position, cursor!!)
  }

  protected abstract fun getItemViewType(
    position: Int,
    cursor: Cursor
  ): Int

  override fun getItemCount(): Int {
    return if (isDataValid(cursor)) {
      cursor?.count ?: 0
    } else {
      0
    }
  }

  override fun getItemId(position: Int): Long {
    check(isDataValid(cursor)) { "Cannot lookup item id when cursor is in invalid state." }

    cursor?.let { cursor ->
      check(cursor.moveToPosition(position)) { "Could not move cursor to position $position when trying to get an item id" }
    }

    return cursor!!.getLong(mRowIDColumn)
  }

  fun swapCursor(newCursor: Cursor?) {
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