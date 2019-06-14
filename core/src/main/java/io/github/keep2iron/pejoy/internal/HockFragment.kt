package io.github.keep2iron.pejoy.internal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.keep2iron.pejoy.ui.PejoyActivity
import io.reactivex.subjects.PublishSubject

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/20 21:19
 */
class HockFragment : Fragment() {
    private var subject: PublishSubject<Intent>? = null
    private var requestCode = 0

    companion object {
        @JvmStatic
        fun newInstance(requestCode: Int): HockFragment {
            val hockFragment = HockFragment()
            val arguments = Bundle()
            arguments.putInt("requestCode", requestCode)
            hockFragment.arguments = arguments

            return hockFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        arguments?.apply {
            requestCode = getInt("requestCode")
        }
        val isOnCreateView = arguments?.getBoolean("onCreateView") ?: false
        if (!isOnCreateView) {
            startActivityForResult()
            arguments?.putBoolean("onCreateView", true)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun startActivityForResult() {
        val intent = Intent(activity, PejoyActivity::class.java)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                subject!!.onNext(data)
            } else {
                subject!!.onNext(Intent())
            }
            subject!!.onComplete()
        }

        subject = null
    }

    fun restoreObservable() {
        subject = PublishSubject.create()
    }

    fun toObservable(): PublishSubject<Intent> {
        return subject!!
    }
}