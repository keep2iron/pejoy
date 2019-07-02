package io.github.keep2iron.pejoy.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.adapter.PreviewFragmentAdapter
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection
import io.github.keep2iron.pejoy.ui.view.CheckRadioView
import io.github.keep2iron.pejoy.ui.view.CheckView
import io.github.keep2iron.pejoy.ui.view.PreviewViewPager
import io.github.keep2iron.pejoy.utilities.PhotoMetadataUtils
import io.github.keep2iron.pejoy.utilities.Platform

abstract class AbstractPreviewActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {
    protected val viewPager: PreviewViewPager by lazy { findViewById<PreviewViewPager>(R.id.viewPager) }
    protected val checkView: CheckView by lazy { findViewById<CheckView>(R.id.checkView) }
    private val imageBack: View by lazy { findViewById<View>(R.id.imageBack) }
    private val original: CheckRadioView by lazy { findViewById<CheckRadioView>(R.id.original) }
    private val buttonApply: TextView by lazy { findViewById<TextView>(R.id.buttonApply) }
    private val originalLayout: View by lazy { findViewById<View>(R.id.originalLayout) }
    private val topToolbar: View by lazy { findViewById<View>(R.id.topToolbar) }
    private val bottomToolbar: View by lazy { findViewById<View>(R.id.bottomToolbar) }
    private val size: TextView by lazy { findViewById<TextView>(R.id.size) }

    protected lateinit var adapter: PreviewFragmentAdapter

    protected val selectionSpec by lazy { SelectionSpec.instance }

    protected val selectedCollection by lazy {
        SelectedItemCollection(applicationContext)
    }
    protected var originEnable: Boolean = false
    protected var previousPos: Int = -1

    private var hidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(selectionSpec.themeId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pejoy_activity_preview)
        if (Platform.hasKitKat()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, android.R.color.black)
        }
        originEnable = if (savedInstanceState == null) {
            selectedCollection.onCreate(intent.getBundleExtra(EXTRA_BUNDLE_ITEMS))
            intent.getBooleanExtra(EXTRA_BOOLEAN_ORIGIN_ENABLE, false)
        } else {
            selectedCollection.onCreate(savedInstanceState)
            savedInstanceState.getBoolean(EXTRA_BOOLEAN_ORIGIN_ENABLE, false)
        }

        imageBack.setOnClickListener(this)
        original.setOnClickListener(this)
        buttonApply.setOnClickListener(this)
        checkView.setOnClickListener(this)
        checkView.setCountable(selectionSpec.countable)

        adapter = PreviewFragmentAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
        viewPager.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.checkView -> {
                val item = (viewPager.adapter as PreviewFragmentAdapter).getMediaItem(viewPager.currentItem)

                val selected = selectedCollection.isSelected(item)
                if (selected) {
                    selectedCollection.remove(item)
                    if (selectionSpec.countable) {
                        checkView.setCheckedNum(CheckView.UNCHECKED)
                    } else {
                        checkView.setChecked(false)
                    }
                } else {
                    if (!selectedCollection.maxSelectableReached() && assertAddSelection(this, item)) {
                        selectedCollection.add(item)
                        if (selectionSpec.countable) {
                            checkView.setCheckedNum(selectedCollection.checkedNumOf(item))
                        } else {
                            checkView.setChecked(true)
                        }
                    }
                }
                updateToolbar(adapter.getMediaItem(viewPager.currentItem))
            }
            R.id.imageBack -> {
                setResult(false)
                finish()
            }
            R.id.viewPager -> {
                if (!selectionSpec.autoHideToolbar) {
                    return
                }

                val factor = if (hidden) {
                    1
                } else {
                    -1
                }
                topToolbar.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationYBy(factor * topToolbar.measuredHeight.toFloat())
                    .start()
                bottomToolbar.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationYBy(-1 * factor * bottomToolbar.measuredHeight.toFloat())
                    .start()
                hidden = !hidden
            }
            R.id.original -> {
                originEnable = !originEnable
                updateToolbar(adapter.getMediaItem(viewPager.currentItem))
                selectionSpec.onOriginCheckedListener?.invoke(originEnable)
            }
            R.id.buttonApply -> {
                setResult(true)
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedCollection.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_BOOLEAN_ORIGIN_ENABLE, originEnable)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (previousPos != -1 && previousPos != position) {
            val adapter = viewPager.adapter as PreviewFragmentAdapter
            (adapter.instantiateItem(viewPager, previousPos) as PreviewItemFragment).resetView()

            val item = adapter.getMediaItem(position)
            if (selectionSpec.countable) {
                val checkNum = selectedCollection.checkedNumOf(item)
                checkView.setCheckedNum(checkNum)
                if (checkNum > 0) {
                    checkView.isEnabled = true
                } else {
                    checkView.isEnabled = !selectedCollection.maxSelectableReached()
                }
            } else {
                val isSelect = selectedCollection.isSelected(item)
                checkView.setChecked(isSelect)
                if (isSelect) {
                    checkView.isEnabled = true
                } else {
                    checkView.isEnabled = !selectedCollection.maxSelectableReached()
                }
            }

            previousPos = position
            updateToolbar(item)
        }
    }

    private fun setResult(apply: Boolean) {
        val intent = Intent()
        intent.putExtra(EXTRA_BOOLEAN_ORIGIN_ENABLE, originEnable)
        intent.putExtra(EXTRA_BUNDLE_ITEMS, selectedCollection.dataWithBundle)
        intent.putExtra(EXTRA_BOOLEAN_RESULT_APPLY, apply)
        setResult(Activity.RESULT_OK, intent)
    }

    fun updateToolbar(item: Item) {

        if (item.isGif) {
            size.visibility = View.VISIBLE
            size.text = "${PhotoMetadataUtils.getSizeInMB(item.size)}M"
        } else {
            size.visibility = View.GONE
        }

        originalLayout.visibility = if (!item.isVideo && selectionSpec.originalable) {
            original.setChecked(originEnable)
            View.VISIBLE
        } else {
            View.GONE
        }

        val selectCount = selectedCollection.count()

        when {
            selectCount == 0 -> {
                buttonApply.isEnabled = false
                buttonApply.alpha = 0.5f
                buttonApply.text = getString(R.string.pejoy_button_apply_default)
            }
            selectCount == 1 && selectionSpec.singleSelectionModeEnabled() -> {
                buttonApply.isEnabled = true
                buttonApply.alpha = 1f
                buttonApply.text = getString(R.string.pejoy_button_apply_default)
            }
            else -> {
                buttonApply.isEnabled = true
                buttonApply.alpha = 1f
                buttonApply.text = getString(R.string.pejoy_button_apply, selectCount)
            }
        }

    }

    private fun assertAddSelection(context: Context, item: Item): Boolean {
        val cause = selectedCollection.isAcceptable(item)
        IncapableCause.handleCause(context, cause)
        return cause == null
    }

    override fun finish() {
        super.finish()
    }

    companion object {
        const val EXTRA_BUNDLE_ITEMS = "extra_bundle_items"

        const val EXTRA_BOOLEAN_ORIGIN_ENABLE = "extra_boolean_origin_enable"

        const val EXTRA_BOOLEAN_RESULT_APPLY = "extra_boolean_result_apply"

        const val REQUEST_CODE = 0x01
    }
}