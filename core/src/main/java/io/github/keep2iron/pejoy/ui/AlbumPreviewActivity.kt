package io.github.keep2iron.pejoy.ui

import android.database.Cursor
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.github.keep2iron.pejoy.adapter.PreviewFragmentAdapter
import io.github.keep2iron.pejoy.internal.entity.Album
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.model.AlbumMediaCollection
import java.util.ArrayList

class AlbumPreviewActivity : AbstractPreviewActivity(), AlbumMediaCollection.AlbumMediaCallbacks {

  private val albumMediaCollection = AlbumMediaCollection()

  private var isAlreadySetPosition = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val that = this
    lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
      fun onCreate() {
        albumMediaCollection.onCreate(that, that)

        val album = intent.getParcelableExtra<Album>(EXTRA_ALBUM)
        albumMediaCollection.load(album)
      }

      @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      fun onDestroy() {
        albumMediaCollection.onDestroy()
      }
    })

    val item = intent.getParcelableExtra<Item>(EXTRA_ITEM)
    if (selectionSpec.countable) {
      checkView.setCheckedNum(selectedCollection.checkedNumOf(item))
    } else {
      checkView.setChecked(selectedCollection.isSelected(item))
    }
    updateToolbar(item)
  }

  override fun onAlbumMediaLoad(cursor: Cursor) {
    val items = ArrayList<Item>()
    while (cursor.moveToNext()) {
      items.add(Item.valueOf(cursor))
    }

    if (items.isEmpty()) {
      return
    }

    val adapter = (viewPager.adapter as PreviewFragmentAdapter)
    adapter.addAll(items)
    adapter.notifyDataSetChanged()

    if (!isAlreadySetPosition) {
      isAlreadySetPosition = true
      val item = intent.getParcelableExtra<Item>(EXTRA_ITEM)
      val index = items.indexOf(item)
      viewPager.setCurrentItem(index, false)
      previousPos = index
    }
  }

  override fun onAlbumMediaReset() {
  }

  companion object {
    const val EXTRA_ITEM = "extra_item"

    const val EXTRA_ALBUM = "extra_album"
  }
}