package io.github.keep2iron.pejoy

import android.content.Intent
import android.net.Uri
import android.os.Build
import io.github.keep2iron.pejoy.engine.ImageEngine
import io.github.keep2iron.pejoy.internal.entity.CaptureStrategy
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.utilities.MediaStoreCompat
import io.github.keep2iron.rxresult.RxResult
import io.reactivex.Observable

class CaptureSelectionCreator
internal constructor(
  private val pejoy: Pejoy
) {

  private var captureStrategy: CaptureStrategy? = null

  private var originalable: Boolean = true

  /**
   * Capture strategy provided for the location to save photos including internal and external
   * storage and also a authority for [androidx.core.content.FileProvider].
   *
   * @param captureStrategy [CaptureStrategy], needed only when capturing is enabled.
   * @return [SelectionCreator] for fluent API.
   */
  fun captureStrategy(captureStrategy: CaptureStrategy): CaptureSelectionCreator {
    this.captureStrategy = captureStrategy
    return this
  }

  /**
   * Show a original photo check options.Let users decide whether use original photo after select
   *
   * @param enable Whether to enable original photo or not
   * @return [SelectionCreator] for fluent API.
   */
  fun originalEnable(
    enable: Boolean
  ): CaptureSelectionCreator {
    originalable = enable
    return this
  }

  fun toObservable(): Observable<Intent> {
    val requestActivity = pejoy.getActivity()
    val requestFragment = pejoy.getFragment()

    val activityCtx = requestActivity ?: requestFragment?.requireActivity()
    ?: throw IllegalArgumentException("activity or fragment is not null")
    val mediaStoreCompat = MediaStoreCompat(activityCtx)
    val capture = captureStrategy ?: CaptureStrategy(
      true,
      activityCtx.applicationContext.packageName + ".provider.PejoyProvider"
    )
    mediaStoreCompat.setCaptureStrategy(capture)

    return when {
      requestActivity != null -> RxResult(requestActivity)
      requestFragment != null -> RxResult(requestFragment)
      else -> throw IllegalArgumentException("activity or fragment is not null")
    }
      .prepare(mediaStoreCompat.buildCaptureIntent(activityCtx))
      .requestForResult()
      .filter {
        it.result
      }
      .map {
        val contentUri = mediaStoreCompat.getCurrentPhotoUri()
        val path = mediaStoreCompat.getCurrentPhotoPath()
        val selected = ArrayList<Uri>()
        selected.add(contentUri)
        val selectedPath = ArrayList<String>()
        selectedPath.add(path)
        val result = Intent()
        result.putExtra(Pejoy.EXTRA_RESULT_ORIGIN_ENABLE, originalable)
        result.putParcelableArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION, selected)
        result.putStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH, selectedPath)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          activityCtx.revokeUriPermission(
            contentUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
          )
        }
        result
      }
  }

}