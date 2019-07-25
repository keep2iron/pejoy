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
package io.github.keep2iron.pejoy

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.annotation.IntDef
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentManager

import java.util.ArrayList

import io.github.keep2iron.pejoy.engine.ImageEngine
import io.github.keep2iron.pejoy.filter.Filter
import io.github.keep2iron.pejoy.internal.HockFragment
import io.github.keep2iron.pejoy.internal.entity.CaptureStrategy
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.listener.OnOriginCheckedListener
import io.reactivex.Observable

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.support.annotation.StyleRes
import kotlin.annotation.Retention

/**
 * Fluent API for building media select specification.
 */
class SelectionCreator
/**
 * Constructs a new specification builder on the context.
 *
 * @param pejoy     a requester context wrapper.
 * @param mimeTypes MIME type set to select.
 */
internal constructor(private val pejoy: Pejoy, mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean) {
    private val mSelectionSpec: SelectionSpec = SelectionSpec.getClearInstance()

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef(
        SCREEN_ORIENTATION_UNSPECIFIED,
        SCREEN_ORIENTATION_LANDSCAPE,
        SCREEN_ORIENTATION_PORTRAIT,
        SCREEN_ORIENTATION_USER,
        SCREEN_ORIENTATION_BEHIND,
        SCREEN_ORIENTATION_SENSOR,
        SCREEN_ORIENTATION_NOSENSOR,
        SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
        SCREEN_ORIENTATION_SENSOR_PORTRAIT,
        SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        SCREEN_ORIENTATION_FULL_SENSOR,
        SCREEN_ORIENTATION_USER_LANDSCAPE,
        SCREEN_ORIENTATION_USER_PORTRAIT,
        SCREEN_ORIENTATION_FULL_USER,
        SCREEN_ORIENTATION_LOCKED
    )
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class ScreenOrientation

    init {
        mSelectionSpec.mimeTypeSet = mimeTypes
        mSelectionSpec.mediaTypeExclusive = mediaTypeExclusive
        mSelectionSpec.orientation = SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return [SelectionCreator] for fluent API.
     * @see SelectionSpec.onlyShowImages
     * @see SelectionSpec.onlyShowVideos
     */
    fun showSingleMediaType(showSingleMediaType: Boolean): SelectionCreator {
        mSelectionSpec.showSingleMediaType = showSingleMediaType
        return this
    }


    /**
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     * value is false.
     * @return [SelectionCreator] for fluent API.
     */
    fun countable(countable: Boolean): SelectionCreator {
        mSelectionSpec.countable = countable
        return this
    }

    /**
     * Theme for media selecting Activity.
     *
     *
     * There are two built-in themes:
     * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
     * 2. com.zhihu.matisse.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return [SelectionCreator] for fluent API.
     */
    fun theme(@StyleRes themeId: Int): SelectionCreator {
        mSelectionSpec.themeId = themeId
        return this
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return [SelectionCreator] for fluent API.
     */
    fun maxSelectable(maxSelectable: Int): SelectionCreator {
        if (maxSelectable < 1) {
            throw IllegalArgumentException("maxSelectable must be greater than or equal to one")
        }
        if (mSelectionSpec.maxImageSelectable > 0 || mSelectionSpec.maxVideoSelectable > 0) {
            throw IllegalStateException("already set maxImageSelectable and maxVideoSelectable")
        }
        mSelectionSpec.maxSelectable = maxSelectable
        return this
    }

    /**
     * Only useful when [SelectionSpec.mediaTypeExclusive] set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return
     */
    fun maxSelectablePerMediaType(maxImageSelectable: Int, maxVideoSelectable: Int): SelectionCreator {
        if (maxImageSelectable < 1 || maxVideoSelectable < 1) {
            throw IllegalArgumentException("max selectable must be greater than or equal to one")
        }
        mSelectionSpec.maxSelectable = -1
        mSelectionSpec.maxImageSelectable = maxImageSelectable
        mSelectionSpec.maxVideoSelectable = maxVideoSelectable
        return this
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter [Filter]
     * @return [SelectionCreator] for fluent API.
     */
    fun addFilter(filter: Filter): SelectionCreator {
        if (mSelectionSpec.filters == null) {
            mSelectionSpec.filters = ArrayList()
        }
        mSelectionSpec.filters?.add(filter)
        return this
    }


    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return [SelectionCreator] for fluent API.
     */
    fun originalEnable(enable: Boolean, originalSelectDefault: Boolean = false): SelectionCreator {
        mSelectionSpec.originalable = enable
        mSelectionSpec.originEnabledDefault = originalSelectDefault
        return this
    }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return [SelectionCreator] for fluent API.
     */
    fun maxOriginalSize(size: Int): SelectionCreator {
        mSelectionSpec.originalMaxSize = size
        return this
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for [android.support.v4.content.FileProvider].
     *
     * @param captureStrategy [CaptureStrategy], needed only when capturing is enabled.
     * @return [SelectionCreator] for fluent API.
     */
    fun captureStrategy(captureStrategy: CaptureStrategy): SelectionCreator {
        mSelectionSpec.captureStrategy = captureStrategy
        return this
    }

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     *
     *
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @param enableInsertAlbum Whether to enable capturing picture to insert system album;
     * @return [SelectionCreator] for fluent API.
     */
    fun capture(enable: Boolean, enableInsertAlbum: Boolean = false): SelectionCreator {
        mSelectionSpec.capture = enable
        mSelectionSpec.captureInsertAlbum = enableInsertAlbum
        return this
    }

    /**
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in [ScreenOrientation].
     * Default value is [android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT].
     * @return [SelectionCreator] for fluent API.
     * @see Activity.setRequestedOrientation
     */
    fun restrictOrientation(@ScreenOrientation orientation: Int): SelectionCreator {
        mSelectionSpec.orientation = orientation
        return this
    }

    fun spanCount(spanCount: Int): SelectionCreator {
        if (spanCount < 1) {
            throw IllegalArgumentException("spanCount cannot be less than 1")
        }
        mSelectionSpec.spanCount = spanCount
        return this
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return [SelectionCreator] for fluent API.
     */
    fun gridExpectedSize(size: Int): SelectionCreator {
        mSelectionSpec.gridExpectedSize = size
        return this
    }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return [SelectionCreator] for fluent API.
     */
    fun thumbnailScale(scale: Float): SelectionCreator {
        if (scale <= 0f || scale > 1f) {
            throw IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0]")
        }
        mSelectionSpec.thumbnailScale = scale
        return this
    }

    fun imageEngine(imageEngine: ImageEngine): SelectionCreator {
        mSelectionSpec.imageEngine = imageEngine
        return this
    }

    fun setOnOriginCheckedListener(listener: OnOriginCheckedListener?): SelectionCreator {
        mSelectionSpec.onOriginCheckedListener = listener
        return this
    }

    private var hockFragment: HockFragment? = null

    private fun findFragmentByTag(fragmentManager: FragmentManager): HockFragment? =
        fragmentManager.findFragmentByTag(HockFragment::class.java.simpleName) as HockFragment?

    fun toObservable(): Observable<Intent> {
        val activity = pejoy.getActivity() ?: return Observable.empty()
        val fragment = pejoy.getFragment()

        val fragmentManager = if (fragment != null) {
            val fragmentActivity = fragment.activity
            if (fragmentActivity == null) {
                return Observable.error(IllegalArgumentException("activity is null!!"))
            } else {
                fragmentActivity.supportFragmentManager
            }
        } else {
            activity.supportFragmentManager
        }

        hockFragment = findFragmentByTag(fragmentManager)
        val newInstance = hockFragment == null

        if (newInstance) {
            hockFragment = HockFragment.newInstance(
                Pejoy.REQUEST_CODE
            )

            val transaction = fragmentManager.beginTransaction()
            transaction.add(hockFragment!!, HockFragment::class.java.simpleName)
            transaction.commitNow()
        } else {
            hockFragment!!.startActivityForResult()
        }
        hockFragment!!.restoreObservable()

        return hockFragment!!.toObservable()
    }
}