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
package io.github.keep2iron.pejoy.internal.entity

import android.content.pm.ActivityInfo
import android.support.annotation.StyleRes
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.engine.ImageEngine
import io.github.keep2iron.pejoy.filter.Filter
import io.github.keep2iron.pejoy.listener.OnOriginCheckedListener
import java.lang.IllegalArgumentException

class SelectionSpec private constructor() {

    var mimeTypeSet: Set<MimeType>? = null
    @StyleRes
    var themeId = R.style.Pejoy_Light
    var mediaTypeExclusive: Boolean = false
    var showSingleMediaType: Boolean = false
    var orientation: Int = 0
    var countable: Boolean = false
    var maxSelectable: Int = 0
    var maxImageSelectable: Int = 0
    var maxVideoSelectable: Int = 0
    var filters: MutableList<Filter>? = null
    var capture: Boolean = false
    var captureInsertAlbum: Boolean = false
    var captureStrategy: CaptureStrategy? = null
    var spanCount: Int = 0
    var gridExpectedSize: Int = 0
    var thumbnailScale: Float = 0.75f
    var imageEngine: ImageEngine? = null
    var hasInited: Boolean = false
    //    public OnSelectedListener onSelectedListener;
    var originalable: Boolean = false
    var originalMaxSize: Int = 0
    var onOriginCheckedListener: OnOriginCheckedListener? = null
    var autoHideToolbar: Boolean = true

    private fun reset() {
        mimeTypeSet = null
        mediaTypeExclusive = true
        showSingleMediaType = false
        orientation = 0
        countable = false
        maxSelectable = 1
        maxImageSelectable = 0
        maxVideoSelectable = 0
        themeId = R.style.Pejoy_Light
        filters = null
        capture = false
        captureStrategy = null
        spanCount = 3
        gridExpectedSize = 0
        thumbnailScale = 0.75f
        imageEngine = null
        hasInited = true
        originalable = false
        autoHideToolbar = true
        originalMaxSize = Integer.MAX_VALUE
    }

    fun requireImageEngine(): ImageEngine {
        if (imageEngine == null) {
            throw IllegalArgumentException("imageEngine is null,do you forget set ImageEngine?")
        }

        return imageEngine!!
    }

    fun requireMinmeType(): Set<MimeType> {
        if (mimeTypeSet == null) {
            throw IllegalArgumentException("mimeTypeSet is null,do you forget set mimeTypeSet?")
        }

        return mimeTypeSet!!
    }

    fun singleSelectionModeEnabled(): Boolean {
        return !countable && (maxSelectable == 1 || maxImageSelectable == 1 && maxVideoSelectable == 1)
    }

    fun needOrientationRestriction(): Boolean {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun onlyShowImages(): Boolean {
        return showSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet!!)
    }

    fun onlyShowVideos(): Boolean {
        return showSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet!!)
    }

    companion object {

        val instance: SelectionSpec by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SelectionSpec()
        }

        fun getClearInstance(): SelectionSpec {
            val selectionSpec = instance
            selectionSpec.reset()
            return selectionSpec
        }

    }
}