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
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.LinearLayout
import android.widget.TextView
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.ui.AlbumModel
import io.github.keep2iron.pejoy.ui.view.PejoyCheckRadioView
import io.github.keep2iron.pejoy.utilities.getThemeDrawable
import java.io.File

/**
 * 相册分类
 */
class AlbumCategoryAdapter(
  context: Context,
  c: Cursor?,
  autoRequery: Boolean,
  private val model: AlbumModel
) :
    CursorAdapter(context, c, autoRequery) {

  private val mPlaceholder = getThemeDrawable(context, R.attr.pejoy_item_placeholder)

  override fun newView(
    context: Context,
    cursor: Cursor,
    parent: ViewGroup
  ): View {
    val dp = (context.resources.displayMetrics.density * 80).toInt()
    val parentView =
      LayoutInflater.from(context).inflate(
          R.layout.pejoy_item_album_category, parent, false
      ) as LinearLayout
    val provideImageView = SelectionSpec.instance.imageEngine!!.provideImageView(context)
    parentView.addView(provideImageView, 0, LinearLayout.LayoutParams(dp, dp).apply {
      leftMargin = (context.resources.displayMetrics.density * 16).toInt()
      rightMargin = (context.resources.displayMetrics.density * 16).toInt()
      gravity = Gravity.CENTER_VERTICAL
    })

    return parentView
  }

  override fun bindView(
    view: View,
    context: Context,
    cursor: Cursor
  ) {
    val album = Album.valueOf(cursor)
    (view.findViewById<View>(R.id.tvAlbumTitle) as TextView).text = album.getDisplayName(context)
    (view.findViewById<View>(R.id.tvAlbumCount) as TextView).text = album.count.toString()
    // do not need to load animated Gif
    SelectionSpec.instance.imageEngine!!.loadThumbnail(
        context, context.resources.getDimensionPixelSize(
        R.dimen.pejoy_media_grid_size
    ), mPlaceholder, (view as ViewGroup).getChildAt(0), Uri.fromFile(File(album.coverPath))
    )
    view.findViewById<PejoyCheckRadioView>(R.id.checkRadioView)
        .setChecked(model.currentAlbum.value?.id == album.id)
  }

}