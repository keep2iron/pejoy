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
package io.github.keep2iron.pejoy.internal.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.ui.view.CheckView
import io.github.keep2iron.pejoy.utilities.PathUtils
import io.github.keep2iron.pejoy.utilities.PhotoMetadataUtils
import java.util.ArrayList
import java.util.LinkedHashSet

class SelectedItemCollection(private val mContext: Context) {
  private lateinit var mItems: MutableSet<Item>
  var collectionType = COLLECTION_UNDEFINED
    private set

  private var onItemSetChangeListener: ((Collection<Item>) -> Unit)? = null

  val dataWithBundle: Bundle
    get() {
      val bundle = Bundle()
      bundle.putParcelableArrayList(STATE_SELECTION, ArrayList(mItems))
      bundle.putInt(STATE_COLLECTION_TYPE, collectionType)
      return bundle
    }

  val isEmpty: Boolean
    get() = mItems.isEmpty()

  fun setOnItemSetChangeListener(onItemSetChangeListener: Function1<Collection<Item>, Unit>) {
    this.onItemSetChangeListener = onItemSetChangeListener
  }

  fun onCreate(bundle: Bundle?) {
    if (bundle == null) {
      mItems = LinkedHashSet()
    } else {
      val saved = bundle.getParcelableArrayList<Item>(STATE_SELECTION)
      mItems = LinkedHashSet(saved!!)
      collectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED)
    }
  }

  fun setDefaultSelection(uris: List<Item>) {
    mItems.addAll(uris)
  }

  fun itemSize(): Int {
    return mItems.size
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putParcelableArrayList(STATE_SELECTION, ArrayList(mItems))
    outState.putInt(STATE_COLLECTION_TYPE, collectionType)
  }

  fun add(item: Item): Boolean {
    if (typeConflict(item)) {
      throw IllegalArgumentException("Can't select images and videos at the same time.")
    }
    val added = mItems.add(item)
    if (added) {
      if (collectionType == COLLECTION_UNDEFINED) {
        if (item.isImage) {
          collectionType = COLLECTION_IMAGE
        } else if (item.isVideo) {
          collectionType = COLLECTION_VIDEO
        }
      } else if (collectionType == COLLECTION_IMAGE) {
        if (item.isVideo) {
          collectionType = COLLECTION_MIXED
        }
      } else if (collectionType == COLLECTION_VIDEO) {
        if (item.isImage) {
          collectionType = COLLECTION_MIXED
        }
      }
    }

    onItemSetChangeListener?.invoke(mItems)

    return added
  }

  fun remove(item: Item): Boolean {
    val removed = mItems.remove(item)
    if (removed) {
      if (mItems.size == 0) {
        collectionType = COLLECTION_UNDEFINED
      } else {
        if (collectionType == COLLECTION_MIXED) {
          refineCollectionType()
        }
      }
    }

    onItemSetChangeListener?.invoke(mItems)

    return removed
  }

  fun overwrite(
    items: ArrayList<Item>,
    collectionType: Int
  ) {
    if (items.size == 0) {
      this.collectionType = COLLECTION_UNDEFINED
    } else {
      this.collectionType = collectionType
    }

    if (onItemSetChangeListener != null) {
      onItemSetChangeListener!!.invoke(mItems)
    }

    mItems.clear()
    mItems.addAll(items)
  }

  fun asList(): List<Item> {
    return ArrayList(mItems)
  }

  fun asListOfUri(): List<Uri> {
    val uris = ArrayList<Uri>()
    for (item in mItems) {
      uris.add(item.contentUri)
    }
    return uris
  }

  fun asListOfString(): List<String> {
    val paths = ArrayList<String>()
    for (item in mItems) {
      paths.add(PathUtils.getPath(mContext, item.contentUri) ?: "")
    }
    return paths
  }

  fun isSelected(item: Item): Boolean {
    return mItems.contains(item)
  }

  fun isAcceptable(item: Item): IncapableCause? {
    if (maxSelectableReached()) {
      val maxSelectable = currentMaxSelectable()
      val cause: String
      cause = mContext.resources.getString(
          R.string.pejoy_error_over_count,
          maxSelectable
      )
      return IncapableCause(message = cause)
    } else if (typeConflict(item)) {
      return IncapableCause(message = mContext.getString(R.string.pejoy_error_type_conflict))
    }

    return PhotoMetadataUtils.isAcceptable(mContext, item)
  }

  fun clear() {
    mItems.clear()

    onItemSetChangeListener?.invoke(mItems)
  }

  fun maxSelectableReached(): Boolean {
    return mItems.size >= currentMaxSelectable()
  }

  // depends
  private fun currentMaxSelectable(): Int {
    val spec = SelectionSpec.instance
    return when {
      spec.maxSelectable > 0 -> spec.maxSelectable
      collectionType == COLLECTION_IMAGE -> spec.maxImageSelectable
      collectionType == COLLECTION_VIDEO -> spec.maxVideoSelectable
      else -> spec.maxSelectable
    }
  }

  private fun refineCollectionType() {
    var hasImage = false
    var hasVideo = false
    for (i in mItems) {
      if (i.isImage && !hasImage) {
        hasImage = true
      }
      if (i.isVideo && !hasVideo) {
        hasVideo = true
      }
    }
    if (hasImage && hasVideo) {
      collectionType = COLLECTION_MIXED
    } else if (hasImage) {
      collectionType = COLLECTION_IMAGE
    } else if (hasVideo) {
      collectionType = COLLECTION_VIDEO
    }
  }

  /**
   * Determine whether there will be conflict media types. A user can only select images and videos at the same time
   * while [mediaTypeExclusive][SelectionSpec] is set to false.
   */
  fun typeConflict(item: Item): Boolean {
    return SelectionSpec.instance.mediaTypeExclusive && (item.isImage && (collectionType == COLLECTION_VIDEO || collectionType == COLLECTION_MIXED) || item.isVideo && (collectionType == COLLECTION_IMAGE || collectionType == COLLECTION_MIXED))
  }

  fun count(): Int {
    return mItems.size
  }

  fun checkedNumOf(item: Item): Int {
    val index = ArrayList(mItems).indexOf(item)
    return if (index == -1) CheckView.UNCHECKED else index + 1
  }

  companion object {

    const val STATE_SELECTION = "state_selection"
    const val STATE_COLLECTION_TYPE = "state_collection_type"

    /**
     * Empty collection
     */
    const val COLLECTION_UNDEFINED = 0x00
    /**
     * Collection only with images
     */
    const val COLLECTION_IMAGE = 0x01
    /**
     * Collection only with videos
     */
    const val COLLECTION_VIDEO = 0x01 shl 1
    /**
     * Collection with images and videos.
     */
    const val COLLECTION_MIXED = COLLECTION_IMAGE or COLLECTION_VIDEO
  }
}