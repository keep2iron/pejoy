package io.github.keep2iron.pejoy.utilities

import android.content.Intent
import android.net.Uri
import io.github.keep2iron.pejoy.Pejoy
import io.reactivex.Observable

fun Observable<Intent>.extractStringPath(): Observable<List<String>> {
  return map {
    it.getStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH)
  }
}

fun Observable<Intent>.extractUriPath(): Observable<ArrayList<Uri>> {
  return map {
    it.getParcelableArrayListExtra<Uri>(Pejoy.EXTRA_RESULT_SELECTION)
  }
}

