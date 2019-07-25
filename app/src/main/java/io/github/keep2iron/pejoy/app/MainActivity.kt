package io.github.keep2iron.pejoy.app

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.facebook.common.util.ByteConstants
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.engine.FrescoImageEngine
import io.github.keep2iron.pineapple.ImageLoaderConfig
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import keep2iron.github.io.compress.weatherCompressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.StrictMode


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

        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.Main) {
            // 在 UI 线程创建一个新协程
            ImageLoaderManager.init(
                application,
                ImageLoaderConfig(
                    applicationContext,
                    maxCacheCount = 300,
                    maxCacheSize = (400 * ByteConstants.MB).toLong()
                ),
                defaultImageLoaderOptions = {
                    scaleType = ImageLoaderOptions.ScaleType.FIT_CENTER
                }
            )
        }

        findViewById<View>(R.id.btnGetImages).setOnClickListener {
            Pejoy.create(this)
                .choose(MimeType.ofAll(), false)
                .maxSelectable(3)
                .theme(R.style.Pejoy_Dracula)
                .countable(true)
                .originalEnable(enable = true, originalSelectDefault = true)
                .capture(true, enableInsertAlbum = true)
                .imageEngine(FrescoImageEngine())
                .setOnOriginCheckedListener { isChecked ->
                    Log.d("keep2iron", "isChecked : $isChecked")
                }
                .toObservable()
                .weatherCompressImage(this)
                .subscribe {
                    imageResultBuilder.append("[\n")
                    it.forEach { uri ->
                        imageResultBuilder.apply {
                            append(uri)
                            if (uri != it.last()) {
                                append("\n")
                            } else {
                                append("\n]\n")
                            }
                        }
                    }
                    tvImageResult.text = imageResultBuilder.toString()
                    Log.d("keep2iron", it.toString() + "this : " + this.hashCode())
                }
        }
    }

}