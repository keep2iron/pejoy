package keep2iron.github.io.compress

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.Pejoy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import top.zibin.luban.InputStreamProvider
import top.zibin.luban.Luban
import java.io.InputStream
import java.lang.IllegalArgumentException

fun Observable<Intent>.weatherCompressImage(
  context: Context,
  compressDir: String = context.cacheDir.absolutePath
): Observable<List<Uri>> {
  return observeOn(Schedulers.io())
    .map {
      val originEnable = it.getBooleanExtra(Pejoy.EXTRA_RESULT_ORIGIN_ENABLE, false)
      val uriList = it.getParcelableArrayListExtra<Uri>(Pejoy.EXTRA_RESULT_SELECTION) ?: ArrayList()

      if (originEnable) {
        uriList
      } else {
        val filePathList = it.getStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH) ?: ArrayList()
        val path2uriMap = mutableMapOf<String,Uri>()

        filePathList.forEachIndexed { index, filePath->
          MimeType.ofImage()
            .forEach { mimeType ->
              if (mimeType.checkType(filePath)) {
                path2uriMap[filePath] = uriList[index]
              }
            }
        }

        val uris = path2uriMap.map {entity->
          val file = Luban.with(context)
            .load(object:InputStreamProvider{
              override fun getPath(): String = entity.key

              override fun open(): InputStream {
                val fd = context.contentResolver.openFileDescriptor(entity.value,"r")?:throw IllegalArgumentException("file open error")
                return ParcelFileDescriptor.AutoCloseInputStream(fd)
              }
            })
            .setTargetDir(compressDir)
            .get(entity.key)

          Uri.fromFile(file)
        }

        uris
      }
    }
    .observeOn(AndroidSchedulers.mainThread())
}