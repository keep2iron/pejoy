package io.github.keep2iron.pejoy.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.facebook.common.util.ByteConstants
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.engine.FrescoImageEngine
import io.github.keep2iron.pejoy.utilities.extractStringPath
import io.github.keep2iron.pineapple.ImageLoaderConfig
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val subject: PublishSubject<Intent> = PublishSubject.create<Intent>()

    private val handler = Handler()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    placeHolderRes = R.drawable.ic_launcher_background
                }
            )
        }

        findViewById<View>(R.id.btnGetImages).setOnClickListener {
            Pejoy.create(this)
                .choose(MimeType.ofImage())
                .maxSelectable(10)
                .imageEngine(FrescoImageEngine())
                .toObservable()
                .extractStringPath()
                .subscribe {
                    Log.d("keep2iron", it.toString() + "this : " + this.hashCode())
                }
        }
    }

}
