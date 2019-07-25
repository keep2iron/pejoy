package io.github.keep2iron.pejoy.ui.view

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.utilities.getThemeColor

class PejoyCheckRadioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private var mDrawable: Drawable? = null

    private var mSelectedColor: Int = 0
    private var mUnSelectUdColor: Int = 0

    init {
        val array = resources.obtainAttributes(attrs, R.styleable.PejoyCheckRadioView)
        mSelectedColor = array.getColor(
            R.styleable.PejoyCheckRadioView_pejoy_check_radio_on_color,
            getThemeColor(
                context,
                R.attr.pejoy_bottom_toolbar_preview_radioOnColor,
                R.color.pejoy_dracula_item_checkCircle_backgroundColor
            )
        )
        mUnSelectUdColor = array.getColor(
            R.styleable.PejoyCheckRadioView_pejoy_check_radio_off_color,
            getThemeColor(
                context,
                R.attr.pejoy_bottom_toolbar_preview_radioOffColor,
                R.color.pejoy_dracula_check_original_radio_disable
            )
        )
        setChecked(false)
        array.recycle()
    }


    fun setChecked(enable: Boolean) {
        if (enable) {
            setImageResource(R.drawable.pejoy_ic_preview_radio_on)
            mDrawable = drawable
            mDrawable!!.setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_IN)
        } else {
            setImageResource(R.drawable.pejoy_ic_preview_radio_off)
            mDrawable = drawable
            mDrawable!!.setColorFilter(mUnSelectUdColor, PorterDuff.Mode.SRC_IN)
        }
    }

    fun setColor(color: Int) {
        if (mDrawable == null) {
            mDrawable = drawable
        }
        mDrawable!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}