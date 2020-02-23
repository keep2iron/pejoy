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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import io.github.keep2iron.pejoy.MimeType
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
  CursorLoader(
    context,
    QUERY_URI,
    if (beforeAndroid10()) PROJECTION else PROJECTION_29,
    selection,
    selectionArgs,
    BUCKET_ORDER_BY
  ) {

  override fun loadInBackground(): Cursor? {
    val albums = super.loadInBackground()
    val allAlbum = MatrixCursor(COLUMNS)

    if (beforeAndroid10()) {
      var totalCount = 0
      var allAlbumCoverUri: Uri? = null
      val otherAlbums = MatrixCursor(COLUMNS)
      if (albums != null) {
        while (albums.moveToNext()) {
          val fileId = albums.getLong(
            albums.getColumnIndex(MediaStore.Files.FileColumns._ID)
          )
          val bucketId = albums.getLong(
            albums.getColumnIndex(COLUMN_BUCKET_ID)
          )
          val bucketDisplayName = albums.getString(
            albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)
          )
          val mimeType = albums.getString(
            albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
          )
          val uri = getUri(albums)
          val count = albums.getInt(albums.getColumnIndex(COLUMN_COUNT))

          otherAlbums.addRow(
            arrayOf(
              fileId.toString(),
              bucketId.toString(), bucketDisplayName, mimeType, uri.toString(),
              count.toString()
            )
          )
          totalCount += count
        }
        if (albums.moveToFirst()) {
          allAlbumCoverUri = getUri(albums)
        }
      }

      allAlbum.addRow(
        arrayOf(
          Album.ALBUM_ID_ALL, Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
          allAlbumCoverUri?.toString(),
          totalCount.toString()
        )
      )

      return MergeCursor(arrayOf(allAlbum, otherAlbums))
    } else {
      var totalCount = 0
      var allAlbumCoverUri: Uri? = null

      // Pseudo GROUP BY
      // Pseudo GROUP BY
      val countMap: MutableMap<Long, Long> = HashMap()
      if (albums != null) {
        while (albums.moveToNext()) {
          val bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID))
          var count = countMap[bucketId]
          if (count == null) {
            count = 1L
          } else {
            count++
          }
          countMap[bucketId] = count
        }
      }

      val otherAlbums = MatrixCursor(COLUMNS)
      if (albums != null) {
        if (albums.moveToFirst()) {
          allAlbumCoverUri = getUri(albums)
          val done: MutableSet<Long> = HashSet()
          do {
            val bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID))
            if (done.contains(bucketId)) {
              continue
            }
            val fileId = albums.getLong(
              albums.getColumnIndex(MediaStore.Files.FileColumns._ID)
            )
            val bucketDisplayName = albums.getString(
              albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)
            )
            val mimeType = albums.getString(
              albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            )
            val uri = getUri(albums)
            val count = countMap[bucketId]!!
            otherAlbums.addRow(
              arrayOf(
                java.lang.Long.toString(fileId),
                java.lang.Long.toString(bucketId),
                bucketDisplayName,
                mimeType,
                uri.toString(), count.toString()
              )
            )
            done.add(bucketId)
            totalCount += count.toInt()
          } while (albums.moveToNext())
        }
      }

      allAlbum.addRow(
        arrayOf(
          Album.ALBUM_ID_ALL,
          Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
          allAlbumCoverUri?.toString(), totalCount.toString()
        )
      )

      return MergeCursor(arrayOf<Cursor>(allAlbum, otherAlbums))
    }
  }

  override fun onContentChanged() {
    // FIXME a dirty way to fix loading multiple times
  }

  companion object {
    private const val COLUMN_BUCKET_ID = "bucket_id"
    private const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"
    const val COLUMN_COUNT = "count"
    const val COLUMN_URI = "uri"
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val COLUMNS = arrayOf(
      MediaStore.Files.FileColumns._ID,
      COLUMN_BUCKET_ID,
      COLUMN_BUCKET_DISPLAY_NAME,
      MediaStore.MediaColumns.MIME_TYPE,
      COLUMN_URI,
      COLUMN_COUNT
    )
    private val PROJECTION = arrayOf(
      MediaStore.Files.FileColumns._ID,
      COLUMN_BUCKET_ID,
      COLUMN_BUCKET_DISPLAY_NAME,
      MediaStore.MediaColumns.MIME_TYPE,
      "COUNT(*) AS $COLUMN_COUNT"
    )
    private val PROJECTION_29 = arrayOf(
      MediaStore.Files.FileColumns._ID,
      COLUMN_BUCKET_ID,
      COLUMN_BUCKET_DISPLAY_NAME,
      MediaStore.MediaColumns.MIME_TYPE
    )

    // === params for showSingleMediaType: false ===
    private const val SELECTION = (
      "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
        " OR " +
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
        " AND " + MediaStore.MediaColumns.SIZE + ">0" +
        ") GROUP BY (bucket_id")
    private const val SELECTION_29 = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE +
      "=?" +
      " OR " +
      MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
      " AND " + MediaStore.MediaColumns.SIZE + ">0")
    private val SELECTION_ARGS = arrayOf(
      MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
      MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )
    // =============================================

    // === params for showSingleMediaType: true ===
    private const val SELECTION_FOR_SINGLE_MEDIA_TYPE = (
      MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
        " AND " + MediaStore.MediaColumns.SIZE + ">0" +
        ") GROUP BY (bucket_id")
    private const val SELECTION_FOR_SINGLE_MEDIA_TYPE_29 =
      (MediaStore.Files.FileColumns.MEDIA_TYPE +
        "=?" +
        " AND " + MediaStore.MediaColumns.SIZE + ">0")

    private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString())
    }
    // =============================================

    private const val BUCKET_ORDER_BY = "datetaken DESC"

    /**
     * @return 是否是 Android 10 （Q） 之前的版本
     */
    private fun beforeAndroid10(): Boolean {
      return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    fun newInstance(context: Context): CursorLoader {
      val selection: String
      val selectionArgs: Array<String>
      when {
        SelectionSpec.instance.onlyShowImages() -> {
          selection =
            if (beforeAndroid10()) SELECTION_FOR_SINGLE_MEDIA_TYPE else SELECTION_FOR_SINGLE_MEDIA_TYPE_29
          selectionArgs =
            getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        }
        SelectionSpec.instance.onlyShowVideos() -> {
          selection =
            if (beforeAndroid10()) SELECTION_FOR_SINGLE_MEDIA_TYPE else SELECTION_FOR_SINGLE_MEDIA_TYPE_29
          selectionArgs =
            getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        }
        else -> {
          selection = if (beforeAndroid10()) SELECTION else SELECTION_29
          selectionArgs = SELECTION_ARGS
        }
      }
      return AlbumLoader(context, selection, selectionArgs)
    }

    private fun getUri(cursor: Cursor): Uri? {
      val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
      val mimeType = cursor.getString(
        cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
      )
      val contentUri: Uri
      contentUri = when {
        MimeType.isImage(mimeType) -> {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        MimeType.isVideo(mimeType) -> {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        else -> { // ?
          MediaStore.Files.getContentUri("external")
        }
      }
      return ContentUris.withAppendedId(contentUri, id)
    }
  }
}