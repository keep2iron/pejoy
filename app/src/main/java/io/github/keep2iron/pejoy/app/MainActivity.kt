package io.github.keep2iron.pejoy.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.StrictMode
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.keep2iron.pineapple.ImageLoaderConfig
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import io.github.keep2iron.pineapple.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private val tvImageResult by lazy {
    findViewById<TextView>(R.id.tvImageResult)
  }

  private val imageResultBuilder = StringBuilder()

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())

//    setContentView(R.layout.activity_main)
    // 在 UI 线程创建一个新协程
    ImageLoaderManager.init(
      application,
      ImageLoaderConfig(
        applicationContext,
        maxCacheCount = 300,
        maxCacheSize = (400 * Util.MB)
      ),
      defaultImageLoaderOptions = {
        scaleType = ImageLoaderOptions.ScaleType.FIT_CENTER
      }
    )

    supportFragmentManager.beginTransaction()
      .replace(android.R.id.content, ChildFragment())
      .commit()
  }
}