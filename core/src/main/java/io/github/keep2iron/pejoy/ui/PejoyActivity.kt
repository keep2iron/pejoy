package io.github.keep2iron.pejoy.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/12 23:15
 */
internal class PejoyActivity : AppCompatActivity() {

  private var currentShowFragment: Fragment? = null

  private val albumFragment by lazy {
    AlbumFragment.newInstance()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val mSpec = SelectionSpec.instance
    setTheme(mSpec.themeId)
    super.onCreate(savedInstanceState)
    if (!mSpec.hasInited) {
      setResult(Activity.RESULT_CANCELED)
      finish()
      return
    }

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    setContentView(R.layout.pejoy_activity_pejoy)

    if (savedInstanceState == null) {
      setContainerFragment(albumFragment)
    }
  }

  private fun setContainerFragment(fragment: Fragment) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(android.R.id.content, fragment)
    transaction.commit()
    currentShowFragment = fragment
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    currentShowFragment?.onActivityResult(requestCode, resultCode, data)
  }

  fun capture() {
    albumFragment.capture()
  }
}