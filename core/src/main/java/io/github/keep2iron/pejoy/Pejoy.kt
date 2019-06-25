package io.github.keep2iron.pejoy

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import java.lang.ref.WeakReference


/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/12 23:30
 */
class Pejoy {

    companion object {
        /**
         * 选中文件的uri路径
         */
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"
        /**
         * 选中文件的path路径
         */
        const val EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path"
        /**
         * 选中的类型
         * @see io.github.keep2iron.pejoy.Pejoy.VIDEO
         * @see io.github.keep2iron.pejoy.Pejoy.IMAGE
         */
        const val EXTRA_RESULT_SELECTION_TYPE = "extra_result_selection_type"

        const val VIDEO = 0x00
        const val IMAGE = 0x01

        const val REQUEST_CODE = 101

        @JvmStatic
        fun create(activity: FragmentActivity): Pejoy {
            return Pejoy(activity)
        }

        @JvmStatic
        fun create(fragment: Fragment): Pejoy {
            return Pejoy(fragment)
        }


    }

    private var mActivity: WeakReference<FragmentActivity>? = null
    private var mFragment: WeakReference<Fragment?>? = null

    private constructor(activity: FragmentActivity) : this(activity, null)

    private constructor(fragment: Fragment) : this(fragment.activity!!, fragment)

    private constructor(activity: FragmentActivity, fragment: Fragment?) {
        mActivity = WeakReference(activity)
        mFragment = WeakReference(fragment)
    }

    fun choose(mimeTypes: Set<MimeType>): SelectionCreator {
        return this.choose(mimeTypes, true)
    }

    /**
     * MIME types the selection constrains on.
     *
     *
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes          MIME types set user can choose from.
     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
     * process. true corresponds to not being able to choose images and videos at the same
     * time, and false corresponds to being able to do this.
     * @return [SelectionCreator] to build select specifications.
     * @see MimeType
     *
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean = true): SelectionCreator {
        return SelectionCreator(this, mimeTypes, mediaTypeExclusive)
    }

    fun choose(vararg mimeTypes: MimeType, mediaTypeExclusive: Boolean = true): SelectionCreator {
        return SelectionCreator(this, mimeTypes.toSet(), mediaTypeExclusive)
    }

    fun getActivity(): FragmentActivity? {
        return mActivity?.get()
    }

    fun getFragment(): Fragment? {
        return mFragment?.get()
    }
}