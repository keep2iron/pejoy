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
package io.github.keep2iron.pejoy.utilities

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec

class PhotoMetadataUtils private constructor() {

    init {
        throw AssertionError("oops! the utility class is about to be instantiated...")
    }

    companion object {
        private val TAG = PhotoMetadataUtils::class.java.simpleName
        private const val MAX_WIDTH = 1600
        private const val SCHEME_CONTENT = "content"

        fun getPixelsCount(resolver: ContentResolver, uri: Uri): Int {
            val size = getBitmapBound(resolver, uri)
            return size.x * size.y
        }

        fun getBitmapSize(uri: Uri, context: Context): Point {
            val resolver = context.contentResolver
            val imageSize = getBitmapBound(resolver, uri)
            var w = imageSize.x
            var h = imageSize.y
            if (PhotoMetadataUtils.shouldRotate(resolver, uri)) {
                w = imageSize.y
                h = imageSize.x
            }
            if (h == 0) {
                return Point(MAX_WIDTH, MAX_WIDTH)
            }
            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()
            val widthScale = screenWidth / w
            val heightScale = screenHeight / h
            return if (widthScale > heightScale) {
                Point((w * widthScale).toInt(), (h * heightScale).toInt())
            } else Point((w * widthScale).toInt(), (h * heightScale).toInt())
        }

        fun getBitmapBound(resolver: ContentResolver, uri: Uri): Point {
            var `is`: InputStream? = null
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                `is` = resolver.openInputStream(uri)
                BitmapFactory.decodeStream(`is`, null, options)
                val width = options.outWidth
                val height = options.outHeight
                return Point(width, height)
            } catch (e: FileNotFoundException) {
                return Point(0, 0)
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        fun getPath(resolver: ContentResolver, uri: Uri?): String? {
            if (uri == null) {
                return null
            }

            if (SCHEME_CONTENT == uri.scheme) {
                var cursor: Cursor? = null
                try {
                    cursor = resolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                    return if (cursor == null || !cursor.moveToFirst()) {
                        null
                    } else cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                } finally {
                    cursor?.close()
                }
            }
            return uri.path
        }

        fun isAcceptable(context: Context, item: Item): IncapableCause? {
            if (!isSelectableType(context, item)) {
                return IncapableCause(context.getString(R.string.pejoy_error_file_type))
            }

            if (SelectionSpec.instance.filters != null) {
                for (filter in SelectionSpec.instance.filters!!) {
                    val incapableCause = filter.filter(context, item)
                    if (incapableCause != null) {
                        return incapableCause
                    }
                }
            }
            return null
        }

        private fun isSelectableType(context: Context?, item: Item): Boolean {
            if (context == null) {
                return false
            }

            val resolver = context.contentResolver
            for (type in SelectionSpec.instance.requireMinmeType()) {
                if (type.checkType(resolver, item.contentUri)) {
                    return true
                }
            }
            return false
        }

        private fun shouldRotate(resolver: ContentResolver, uri: Uri): Boolean {
            val exif: ExifInterface
            try {
                exif = ExifInterfaceCompat.newInstance(getPath(resolver, uri))
            } catch (e: IOException) {
                Log.e(TAG, "could not read exif info of the image: $uri")
                return false
            }

            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            return orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270
        }

        fun getSizeInMB(sizeInBytes: Long): Float {
            val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
            df.applyPattern("0.0")
            var result = df.format((sizeInBytes.toFloat() / 1024f / 1024f).toDouble())
            Log.e(TAG, "getSizeInMB: $result")
            result = result.replace(",".toRegex(), ".") // in some case , 0.0 will be 0,0
            return java.lang.Float.valueOf(result)
        }
    }
}