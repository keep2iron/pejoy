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
package io.github.keep2iron.pejoy.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import java.lang.ref.WeakReference
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.loader.AlbumMediaLoader
import java.lang.IllegalArgumentException

class AlbumMediaCollection : LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mContext: WeakReference<Context>
    private var mLoaderManager: LoaderManager? = null
    private var mCallbacks: AlbumMediaCallbacks? = null

    var isLoadComplete = false

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = mContext.get() ?: throw IllegalArgumentException("context is null")

        val album = args?.getParcelable<Album>(ARGS_ALBUM) ?: throw IllegalArgumentException("album is null")

        return AlbumMediaLoader.newInstance(
            context, album,
            album.isAll && args.getBoolean(ARGS_ENABLE_CAPTURE, false)
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mContext.get() ?: return

        mCallbacks?.onAlbumMediaLoad(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mContext.get() ?: return

        mCallbacks?.onAlbumMediaReset()
    }

    fun onCreate(context: FragmentActivity, callbacks: AlbumMediaCallbacks) {
        mContext = WeakReference(context)
        mLoaderManager = context.supportLoaderManager
        mCallbacks = callbacks
    }

    fun onDestroy() {
        mLoaderManager?.destroyLoader(LOADER_ID)
        mLoaderManager = null
        mCallbacks = null
        isLoadComplete = true
    }

    @JvmOverloads
    fun load(target: Album?, enableCapture: Boolean = false) {
        isLoadComplete = true

        val args = Bundle()
        args.putParcelable(ARGS_ALBUM, target)
        args.putBoolean(ARGS_ENABLE_CAPTURE, enableCapture)
        mLoaderManager?.initLoader(LOADER_ID, args, this)
    }

    interface AlbumMediaCallbacks {

        fun onAlbumMediaLoad(cursor: Cursor)

        fun onAlbumMediaReset()
    }

    companion object {
        private const val LOADER_ID = 2
        private const val ARGS_ALBUM = "args_album"
        private const val ARGS_ENABLE_CAPTURE = "args_enable_capture"
    }
}