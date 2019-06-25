package io.github.keep2iron.pejoy.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.utilities.PhotoMetadataUtils
import io.github.keep2iron.pejoy.utilities.getThemeDrawable

class PreviewItemFragment : Fragment() {
    private val engine = SelectionSpec.instance.requireImageEngine()

    private val placeholder by lazy {
        getThemeDrawable(requireContext(), R.attr.pejoy_item_placeholder)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val imageView = engine.provideScaleImageView(requireContext())
        val content = inflater.inflate(R.layout.pejoy_fragment_preview_item, container, false) as ViewGroup
        val layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        content.addView(imageView, layoutParams)
        return content
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = (view as ViewGroup).getChildAt(0)
        val item = arguments!!.getParcelable<Item>("args_item")!!

        val size = PhotoMetadataUtils.getBitmapSize(item.contentUri, requireActivity())
        if (item.isGif) {
            engine.loadGifImage(requireContext(), size.x, size.y, placeholder, imageView, item.uri)
        } else {
            engine.loadImage(requireContext(), size.x, size.y, placeholder, imageView, item.uri)
        }
    }

    /**
     * 将图像复原
     */
    fun resetView() {
        val imageView = (view as ViewGroup).getChildAt(0)
        engine.resetViewMatrix(imageView)
    }

    companion object {
        fun newInstance(item: Item): PreviewItemFragment {
            val previewItemFragment = PreviewItemFragment()
            val arguments = Bundle()
            arguments.putParcelable("args_item", item)
            previewItemFragment.arguments = arguments
            return previewItemFragment
        }
    }
}