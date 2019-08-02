/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec

/**
 * Load all albums (grouped by bucket_id) into a single cursor.
 */
class AlbumLoader private constructor(
  context: Context,
  selection: String,
  selectionArgs: Array<String>
) :
    CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, BUCKET_ORDER_BY) {

  override fun loadInBackground(): Cursor? {
    val albums = super.loadInBackground()
    val allAlbum = MatrixCursor(COLUMNS)
    var totalCount = 0
    var allAlbumCoverPath = ""
    if (albums != null) {
      while (albums.moveToNext()) {
        totalCount += albums.getInt(albums.getColumnIndex(COLUMN_COUNT))
      }
      if (albums.moveToFirst()) {
        allAlbumCoverPath = albums.getString(albums.getColumnIndex(MediaStore.MediaColumns.DATA))
      }
    }
    allAlbum.addRow(
        arrayOf(
            Album.ALBUM_ID_ALL,
            Album.ALBUM_ID_ALL,
            Album.ALBUM_NAME_ALL,
            allAlbumCoverPath,
            totalCount.toString()
        )
    )

    return MergeCursor(arrayOf(allAlbum, albums))
  }

  override fun onContentChanged() {
    // FIXME a dirty way to fix loading multiple times
  }

  companion object {
    const val COLUMN_COUNT = "count"
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val COLUMNS = arrayOf(
        MediaStore.Files.FileColumns._ID,
        "bucket_id",
        "bucket_display_name",
        MediaStore.MediaColumns.DATA,
        COLUMN_COUNT
    )
    private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        "bucket_id",
        "bucket_display_name",
        MediaStore.MediaColumns.DATA,
        "COUNT(*) AS $COLUMN_COUNT"
    )

    // === params for showSingleMediaType: false ===
    private const val SELECTION = (
        "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + ") GROUP BY (bucket_id")
    private val SELECTION_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )
    // =============================================

    // === params for showSingleMediaType: true ===
    private const val SELECTION_FOR_SINGLE_MEDIA_TYPE = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + ") GROUP BY (bucket_id")

    private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString())
    }
    // =============================================

    private const val BUCKET_ORDER_BY = "datetaken DESC"

    fun newInstance(context: Context): CursorLoader {
      val selection: String
      val selectionArgs: Array<String>
      when {
        SelectionSpec.instance.onlyShowImages() -> {
          selection = SELECTION_FOR_SINGLE_MEDIA_TYPE
          selectionArgs =
            getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        }
        SelectionSpec.instance.onlyShowVideos() -> {
          selection = SELECTION_FOR_SINGLE_MEDIA_TYPE
          selectionArgs =
            getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        }
        else -> {
          selection = SELECTION
          selectionArgs = SELECTION_ARGS
        }
      }
      return AlbumLoader(context, selection, selectionArgs)
    }
  }
}