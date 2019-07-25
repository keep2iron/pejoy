package io.github.keep2iron.pejoy.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import io.github.keep2iron.pejoy.adapter.AlbumCategoryAdapter
import io.github.keep2iron.pejoy.adapter.AlbumMediaAdapter
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.AlbumCollection
import io.github.keep2iron.pejoy.internal.model.AlbumMediaCollection
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/12 17:41
 */

class AlbumModel(application: Application) : AndroidViewModel(application) {

    private val albumCollection = AlbumCollection()

    private var albumMediaCollection: AlbumMediaCollection = AlbumMediaCollection()

    lateinit var selectedItemCollection: SelectedItemCollection

    var currentAlbum: MutableLiveData<Album> = MutableLiveData()

    var emptyData: MutableLiveData<Boolean> = MutableLiveData()

    private val selectionSpec: SelectionSpec = SelectionSpec.instance

    var originEnabled = false

    fun onCreateViewFragment(savedInstanceState: Bundle?) {
        selectedItemCollection = SelectedItemCollection(getApplication())
        selectedItemCollection.onCreate(savedInstanceState)
    }

    fun onSaveInstanceState(outState: Bundle) {
        selectedItemCollection.onSaveInstanceState(outState)
        albumCollection.onSaveInstanceState(outState)
    }

    /**
     * 加载所有的相册类型
     */
    fun loadAlbum(
        activity: FragmentActivity,
        albumsAdapter: AlbumCategoryAdapter,
        albumMediaAdapter: AlbumMediaAdapter,
        savedInstanceState: Bundle?
    ) {
        albumCollection.onCreate(activity, object : AlbumCollection.AlbumCallbacks {
            override fun onAlbumLoad(cursor: Cursor) {
                albumsAdapter.swapCursor(cursor)
                // select default album.
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    cursor.moveToPosition(albumCollection.currentSelection)
                    val album = Album.valueOf(cursor)
                    if (album.isAll && SelectionSpec.instance.capture) {
                        album.addCaptureCount()
                    }
                    onAlbumSelected(activity, album, albumMediaAdapter)
                }
            }

            override fun onAlbumReset() {
                albumsAdapter.swapCursor(null)
            }
        })
        albumCollection.onRestoreInstanceState(savedInstanceState)
        albumCollection.loadAlbums()
    }

    fun onAlbumClick(
        activity: FragmentActivity,
        position: Int,
        albumsAdapter: AlbumCategoryAdapter,
        albumMediaAdapter: AlbumMediaAdapter
    ) {
        albumCollection.setStateCurrentSelection(position)
        albumsAdapter.cursor.moveToPosition(position)
        val album = Album.valueOf(albumsAdapter.cursor)
        onAlbumSelected(activity, album, albumMediaAdapter)
    }

    /**
     *
     */
    private fun onAlbumSelected(activity: FragmentActivity, album: Album, albumMediaAdapter: AlbumMediaAdapter) {
        currentAlbum.postValue(album)
        if (albumMediaCollection.isLoadComplete) {
            albumMediaCollection.onDestroy()
            albumMediaCollection = AlbumMediaCollection()
        }

        albumMediaCollection.onCreate(activity, object : AlbumMediaCollection.AlbumMediaCallbacks {
            override fun onAlbumMediaLoad(cursor: Cursor) {
                if (cursor.count > 0) {
                    albumMediaAdapter.swapCursor(cursor)
                    emptyData.postValue(false)
                } else {
                    emptyData.postValue(true)
                }
            }

            override fun onAlbumMediaReset() {
            }
        })
        albumMediaCollection.load(album, selectionSpec.capture)
    }

    fun onDestroy() {
        albumCollection.onDestroy()
        albumMediaCollection.onDestroy()
    }
}