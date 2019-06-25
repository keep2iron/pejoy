package io.github.keep2iron.pejoy.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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

    private val fragmentArr = arrayOf(
        AlbumFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val mSpec = SelectionSpec.instance
        setTheme(mSpec.themeId)
        super.onCreate(savedInstanceState)
        if (!mSpec.hasInited) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContentView(R.layout.pejoy_activity_pejoy)

        if (savedInstanceState == null) {
            setContainerFragment(AlbumFragment.newInstance())
        }
    }

    private fun setContainerFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pejoyContainer, fragment)
        transaction.commit()
        currentShowFragment = fragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        currentShowFragment?.onActivityResult(requestCode, resultCode, data)
    }
}