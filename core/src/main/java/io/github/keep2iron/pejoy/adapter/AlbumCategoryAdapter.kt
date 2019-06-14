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
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import java.io.File
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec

/**
 * 相册分类
 */
class AlbumCategoryAdapter : CursorAdapter {
    var onItemClickListener: ((Album) -> Unit)? = null
    private val mPlaceholder: Drawable

    constructor(context: Context, c: Cursor?, autoRequery: Boolean) : super(context, c, autoRequery) {
        mPlaceholder = ColorDrawable(Color.WHITE)
    }

    constructor(context: Context, c: Cursor?, flags: Int) : super(context, c, flags) {
        mPlaceholder = ColorDrawable(Color.WHITE)
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.pejoy_item_album_category, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val album = Album.valueOf(cursor)
//        (view.findViewById<View>(R.id.album_name) as TextView).text = album.getDisplayName(context)
//        (view.findViewById<View>(R.id.album_media_count) as TextView).text = album.count.toString()
//
//        view.setOnClickListener {
//            onItemClickListener?.invoke(album)
//        }
//        // do not need to load animated Gif
//        SelectionSpec.getInstance().imageEngine.loadThumbnail(
//            context, context.resources.getDimensionPixelSize(
//                R.dimen.pejoy_media_grid_size
//            ), mPlaceholder, view.findViewById(R.id.album_cover), Uri.fromFile(File(album.coverPath))
//        )
    }
}