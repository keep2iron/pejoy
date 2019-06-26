package io.github.keep2iron.pejoy.ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.adapter.AlbumMediaAdapter
import io.github.keep2iron.pejoy.adapter.AlbumCategoryAdapter
import io.github.keep2iron.pejoy.ui.view.CheckRadioView
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection
import io.github.keep2iron.pejoy.ui.view.AlbumContentView
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

    private lateinit var recyclerView: RecyclerView

    private lateinit var buttonPreview: TextView

    private lateinit var originalLayout: LinearLayout

    private lateinit var original: CheckRadioView

    private lateinit var buttonApply: TextView

    private lateinit var buttonAlbumCategory: TextView

    private lateinit var albumContentView: AlbumContentView

    private val spec = SelectionSpec.instance

    companion object {
        @JvmStatic
        fun newInstance(): AlbumFragment {
            return AlbumFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pejoy_fragment_album, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        buttonPreview = view.findViewById(R.id.buttonPreview)
        originalLayout = view.findViewById(R.id.originalLayout)
        original = view.findViewById(R.id.original)
        buttonAlbumCategory = view.findViewById(R.id.buttonAlbumCategory)
        buttonApply = view.findViewById(R.id.buttonApply)
        albumContentView = view.findViewById(R.id.albumContentView)

        this.savedInstanceState = savedInstanceState

        model = ViewModelProviders.of(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireContext().applicationContext as Application)
        ).get(AlbumModel::class.java)

        model.onCreateViewFragment(savedInstanceState)

        albumsAdapter = AlbumCategoryAdapter(requireContext(), null, false, model)

        albumMediaAdapter = AlbumMediaAdapter(requireActivity(), model.selectedItemCollection, recyclerView, model)

        initRecyclerView()

        initAlbumCategory()

        subscribeOnUI()

        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0x01)

        updateBottomToolbar()

        return view
    }

    private fun initAlbumCategory() {

        albumContentView.setAdapter(albumsAdapter)
        albumContentView.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            model.onAlbumClick(requireActivity(), position, albumsAdapter, albumMediaAdapter)
            albumContentView.hidden()
        })
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
        original.setOnClickListener(this)
        buttonAlbumCategory.setOnClickListener(this)
        buttonPreview.setOnClickListener(this)
        model.currentAlbum.observe(this, Observer {
            if (it != null) {
                buttonAlbumCategory.text = it.getDisplayName(requireContext())
            }
        })
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spec.spanCount)
        recyclerView.setHasFixedSize(true)

        recyclerView.addItemDecoration(
            MediaGridInset(
                3,
                resources.getDimensionPixelOffset(R.dimen.pejoy_media_grid_spacing),
                false
            )
        )
        recyclerView.adapter = albumMediaAdapter
        albumMediaAdapter.setOnCheckedViewStateChangeListener {
            updateBottomToolbar()
        }
    }

    private fun setResult() {
        val result = Intent()
        val selectedPaths = model.selectedItemCollection.asListOfString() as ArrayList<String>
        result.putStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH, selectedPaths)

        val activity = requireActivity()
        activity.setResult(Activity.RESULT_OK, result)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonApply -> {
                setResult()
                requireActivity().finish()
            }
            R.id.original -> {
                model.originEnabled = !model.originEnabled
                updateBottomToolbar()
            }
            R.id.buttonAlbumCategory -> {
                albumContentView.switch()
            }
            R.id.buttonPreview -> {
                val intent = Intent(requireContext(), SelectedAlbumPreviewActivity::class.java)
                intent.putExtra(AbstractPreviewActivity.EXTRA_BUNDLE_ITEMS, model.selectedItemCollection.dataWithBundle)
                intent.putExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_ORIGIN_ENABLE, model.originEnabled)
                startActivityForResult(intent, AbstractPreviewActivity.REQUEST_CODE)
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

    private fun updateBottomToolbar() {
        val selectCount = model.selectedItemCollection.count()

        when {
            selectCount == 0 -> {
                buttonPreview.isEnabled = false
                buttonPreview.alpha = 0.5f
                buttonApply.isEnabled = false
                buttonApply.alpha = 0.5f
                buttonApply.text = getString(R.string.pejoy_button_apply_default)
            }
            selectCount == 1 && spec.singleSelectionModeEnabled() -> {
                buttonPreview.isEnabled = true
                buttonPreview.alpha = 1f
                buttonApply.isEnabled = true
                buttonApply.alpha = 1f
                buttonApply.text = getString(R.string.pejoy_button_apply_default)
            }
            else -> {
                buttonPreview.isEnabled = true
                buttonPreview.alpha = 1f
                buttonApply.isEnabled = true
                buttonApply.alpha = 1f
                buttonApply.text = getString(R.string.pejoy_button_apply, selectCount)
            }
        }

        original.setChecked(model.originEnabled)

        originalLayout.visibility = if (spec.originalable) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AbstractPreviewActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val apply = data.getBooleanExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_RESULT_APPLY, false)
            if (!apply) {
                val originEnabled = data.getBooleanExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_ORIGIN_ENABLE, false)

                val bundle = data.getBundleExtra(AbstractPreviewActivity.EXTRA_BUNDLE_ITEMS)
                val collectionType = bundle.getInt(
                    SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED
                )
                val selected = bundle.getParcelableArrayList<Item>(SelectedItemCollection.STATE_SELECTION)

                model.selectedItemCollection.overwrite(selected, collectionType)

                model.originEnabled = originEnabled

                albumMediaAdapter.notifyDataSetChanged()
                updateBottomToolbar()
            } else {
                setResult()
                requireActivity().finish()
            }
        }
    }
}