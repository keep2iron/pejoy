package io.github.keep2iron.pejoy.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.utilities.PhotoMetadataUtils
import io.github.keep2iron.pejoy.utilities.getThemeDrawable

class PreviewItemFragment : Fragment(), View.OnClickListener {
    private val engine = SelectionSpec.instance.requireImageEngine()

    private val placeholder by lazy {
        getThemeDrawable(requireContext(), R.attr.pejoy_item_placeholder)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val imageView = engine.provideScaleImageView(requireContext())
        val content = inflater.inflate(R.layout.pejoy_fragment_preview_item, container, false) as ViewGroup
        val layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        content.addView(imageView, 0, layoutParams)
        return content
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = (view as ViewGroup).getChildAt(0)
        val item = arguments!!.getParcelable<Item>("args_item")!!

        val size = PhotoMetadataUtils.getBitmapSize(item.contentUri, requireActivity())
        when {
            item.isVideo -> {
                val playVideo = view.findViewById<View>(R.id.playVideo)
                playVideo.visibility = View.VISIBLE
                playVideo.setOnClickListener(this)
                playVideo.tag = item
                engine.loadImage(requireContext(), size.x, size.y, placeholder, imageView, item.uri)
            }
            item.isGif -> engine.loadGifImage(requireContext(), size.x, size.y, placeholder, imageView, item.uri)
            else -> engine.loadImage(requireContext(), size.x, size.y, placeholder, imageView, item.uri)
        }
    }

    override fun onClick(view: View) {
        val item = view.tag as Item?
        if (item != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(item.uri, "video/*")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.pejoy_error_no_video_activity, Toast.LENGTH_SHORT).show()
            }

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