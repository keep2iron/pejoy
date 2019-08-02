package keep2iron.github.io.compress

import android.content.Context
import android.content.Intent
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.Pejoy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import top.zibin.luban.Luban

fun Observable<Intent>.weatherCompressImage(
  context: Context,
  compressDir: String = context.cacheDir.absolutePath
): Observable<List<String>> {
  return observeOn(Schedulers.io())
      .map {
        val originEnable = it.getBooleanExtra(Pejoy.EXTRA_RESULT_ORIGIN_ENABLE, false)
        val list = it.getStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH)

        if (originEnable) {
          list
        } else {
          val indexList = mutableListOf<Int>()
          val images = list.filterIndexed { index, filePath ->
            MimeType.ofImage()
                .forEach { mimeType ->
                  if (mimeType.checkType(filePath)) {
                    indexList.add(index)
                    return@filterIndexed true
                  }
                }
            return@filterIndexed false
          }

          val files = Luban.with(context)
              .load(images)
              .setTargetDir(compressDir)
              .get()

          indexList.forEachIndexed { i, index ->
            list.removeAt(index)
            list.add(index, files[i].absolutePath)
          }

          list.toList()
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
}