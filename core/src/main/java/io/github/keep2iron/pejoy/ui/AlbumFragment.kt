package io.github.keep2iron.pejoy.ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.adapter.AlbumMediaAdapter
import io.github.keep2iron.pejoy.adapter.AlbumCategoryAdapter
import java.security.Permission
import android.widget.TextView
import io.github.keep2iron.pejoy.ui.view.CheckRadioView
import android.widget.LinearLayout
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import java.util.ArrayList


/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/12 16:29
 */
class AlbumFragment : Fragment(), View.OnClickListener {
    private lateinit var model: AlbumModel
    /**
     * 相册类型adapter
     */
    private lateinit var albumsAdapter: AlbumCategoryAdapter
    /**
     * 相册媒体
     */
    private lateinit var albumMediaAdapter: AlbumMediaAdapter

    private var savedInstanceState: Bundle? = null

    private lateinit var toolbar: Toolbar

    private lateinit var recyclerView: RecyclerView

    private lateinit var buttonPreview: TextView

    private lateinit var originalLayout: LinearLayout

    private lateinit var original: CheckRadioView

    private lateinit var buttonApply: TextView

    companion object {
        @JvmStatic
        fun newInstance(): AlbumFragment {
            return AlbumFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pejoy_fragment_album, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        recyclerView = view.findViewById(R.id.recyclerView)
        buttonPreview = view.findViewById(R.id.buttonPreview)
        originalLayout = view.findViewById(R.id.originalLayout)
        original = view.findViewById(R.id.original)

        buttonApply = view.findViewById(R.id.buttonApply)

        this.savedInstanceState = savedInstanceState

        model = ViewModelProviders.of(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireContext().applicationContext as Application)
        ).get(AlbumModel::class.java)

        model.onCreateViewFragment(savedInstanceState)

        albumsAdapter = AlbumCategoryAdapter(requireContext(), null, false)

        albumMediaAdapter = AlbumMediaAdapter(requireContext(), model.selectedItemCollection, recyclerView, model)

        initRecyclerView()

        initAlbumTitles()

        subscribeOnUI()

        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0x01)

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val isReadExternalStoragePermission =
            permissions.isNotEmpty() && permissions.first() == Manifest.permission.READ_EXTERNAL_STORAGE
        val hadGetPermission = grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED
        if (requestCode == 0x01 && isReadExternalStoragePermission && hadGetPermission) {
            model.loadAlbum(requireActivity(), albumsAdapter, albumMediaAdapter, savedInstanceState)
        } else {
            Toast.makeText(requireContext(), R.string.pejoy_error_read_external_storage_permission, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun subscribeOnUI() {
        buttonApply.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.addItemDecoration(DividerGridItemDecoration(requireContext()))
        recyclerView.adapter = albumMediaAdapter
    }

    /**
     * 绑定相册title
     */
    private fun initAlbumTitles() {
        val popupWindow = PopupWindow()

        val listView = ListView(context)
        listView.divider = ColorDrawable(Color.parseColor("#e6e6e6"))
        listView.dividerHeight = 3
        listView.setBackgroundColor(Color.WHITE)
        listView.adapter = albumsAdapter
        albumsAdapter.onItemClickListener = {
            model.onAlbumSelected(activity!!, it, albumMediaAdapter)
            popupWindow.dismiss()
        }

        popupWindow.apply {
            height = (300 * resources.displayMetrics.density).toInt()
            width = ViewGroup.LayoutParams.MATCH_PARENT
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            contentView = listView
        }

//        dataBinding.tvTitle.setOnClickListener {
//            popupWindow.showAsDropDown(dataBinding.tvTitle)
//        }
//
//        model.selectedItemCollection.setOnItemSetChangeListener {
//            if (it.isNotEmpty()) {
//                dataBinding.tvContinue.text = resources.getString(R.string.str_continue_format, it.size)
//            } else {
//                dataBinding.tvContinue.text = resources.getString(R.string.str_continue)
//            }
//        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonApply -> {
                val result = Intent()
                val selectedUris = model.selectedItemCollection.asListOfUri() as ArrayList<Uri>
                result.putParcelableArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION, selectedUris)
                val selectedPaths = model.selectedItemCollection.asListOfString() as ArrayList<String>
                result.putStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH, selectedPaths)

                val activity = requireActivity()
                activity.setResult(Activity.RESULT_OK, result)
                activity.finish()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        model.onSaveInstanceState(outState)
    }


    override fun onDestroy() {
        model.onDestroy()
        super.onDestroy()
    }

    fun updateBottomToolbar() {
        originalLayout.visibility = if (SelectionSpec.instance.originalable) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}