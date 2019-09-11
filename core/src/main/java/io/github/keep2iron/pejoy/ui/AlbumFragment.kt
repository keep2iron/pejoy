package io.github.keep2iron.pejoy.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.adapter.AlbumCategoryAdapter
import io.github.keep2iron.pejoy.adapter.AlbumMediaAdapter
import io.github.keep2iron.pejoy.internal.entity.CaptureStrategy
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection
import io.github.keep2iron.pejoy.ui.view.AlbumContentView
import io.github.keep2iron.pejoy.ui.view.PejoyCheckRadioView
import io.github.keep2iron.pejoy.utilities.MediaStoreCompat
import io.github.keep2iron.pejoy.utilities.getThemeColor
import java.util.ArrayList
import kotlin.LazyThreadSafetyMode.NONE

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/12 16:29
 */
open class AlbumFragment : Fragment(), View.OnClickListener {
  private lateinit var model: AlbumModel
  /**
   * 相册类型adapter
   */
  private lateinit var albumsAdapter: AlbumCategoryAdapter
  /**
   * 相册媒体
   */
  private lateinit var albumMediaAdapter: AlbumMediaAdapter

  var savedInstanceState: Bundle? = null

  lateinit var recyclerView: RecyclerView

  lateinit var buttonPreview: TextView

  lateinit var originalLayout: LinearLayout

  lateinit var original: PejoyCheckRadioView

  lateinit var buttonApply: TextView

  lateinit var buttonAlbumCategory: TextView

  lateinit var albumContentView: AlbumContentView

  lateinit var imageBack: ImageView

  lateinit var ivEmptyData: ImageView

  private val spec = SelectionSpec.instance

  private val mediaStoreCompat: MediaStoreCompat by lazy(NONE) {
    MediaStoreCompat(requireActivity(), this)
  }

  companion object {
    @JvmStatic
    fun newInstance(): AlbumFragment {
      return AlbumFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.pejoy_fragment_album, container, false)

    buttonPreview = view.findViewById(R.id.buttonPreview)
    originalLayout = view.findViewById(R.id.originalLayout)
    original = view.findViewById(R.id.original)
    buttonAlbumCategory = view.findViewById(R.id.buttonAlbumCategory)
    buttonApply = view.findViewById(R.id.buttonApply)
    albumContentView = view.findViewById(R.id.albumContentView)
    imageBack = view.findViewById(R.id.imageBack)
    recyclerView = view.findViewById(R.id.recyclerView)
    ivEmptyData = view.findViewById(R.id.ivEmptyData)

    this.savedInstanceState = savedInstanceState

    model = ViewModelProviders.of(
      this,
      ViewModelProvider.AndroidViewModelFactory.getInstance(
        requireContext().applicationContext as Application
      )
    )
      .get(AlbumModel::class.java)

    model.onCreateViewFragment(savedInstanceState)

    mediaStoreCompat.onCreate(savedInstanceState)
    if (spec.captureStrategy != null) {
      mediaStoreCompat.setCaptureStrategy(spec.captureStrategy!!)
    } else {
      val value = TypedValue()
      requireContext().theme.resolveAttribute(R.attr.pejoy_file_provider, value, true)

      mediaStoreCompat.setCaptureStrategy(
        CaptureStrategy(
          true,
          requireContext().applicationContext.packageName + ".provider.PejoyProvider"
        )
      )
    }

    albumsAdapter = AlbumCategoryAdapter(requireContext(), null, false, model)

    albumMediaAdapter =
      AlbumMediaAdapter(requireActivity(), model.selectedItemCollection, recyclerView, model)

    initRecyclerView()

    initAlbumCategory()

    subscribeOnUI()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0x01)
    } else {
      model.loadAlbum(requireActivity(), albumsAdapter, albumMediaAdapter, savedInstanceState)
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      buttonApply.setBackgroundDrawable(GradientDrawable().apply {
        setColor(
          getThemeColor(
            requireContext(),
            R.attr.colorPrimaryDark,
            R.color.pejoy_light_primary_dark
          )
        )
        cornerRadius = resources.displayMetrics.density * 8
      })
    } else {
      buttonApply.setBackgroundResource(R.drawable.pejoy_shape_apply_background)
    }

    if (spec.originalable && spec.originEnabledDefault) {
      model.originEnabled = spec.originEnabledDefault
    }

    updateBottomToolbar()

    return view
  }

  private fun initAlbumCategory() {
    val drawables = buttonAlbumCategory.compoundDrawables
    val captureColor = getThemeColor(
      requireContext(),
      R.attr.pejoy_bottom_toolbar_lintColor,
      R.color.pejoy_dracula_check_apply_text_color
    )
    drawables.indices.forEach {
      val drawable = drawables[it]
      if (drawable != null && drawable.constantState != null) {
        val state = drawable.constantState!!

        val newDrawable = state.newDrawable()
          .mutate()
        newDrawable.setColorFilter(captureColor, PorterDuff.Mode.SRC_IN)
        newDrawable.bounds = drawable.bounds
        drawables[it] = newDrawable
      }
    }
    buttonAlbumCategory.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])

    albumContentView.setAdapter(albumsAdapter)
    albumContentView.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
      model.onAlbumClick(requireActivity(), position, albumsAdapter, albumMediaAdapter)
      albumContentView.hidden()
    })
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == 0x01) {
      val isReadExternalStoragePermission =
        permissions.isNotEmpty() && permissions.first() == Manifest.permission.READ_EXTERNAL_STORAGE
      val hadGetPermission =
        grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED
      if (isReadExternalStoragePermission && hadGetPermission) {
        model.loadAlbum(requireActivity(), albumsAdapter, albumMediaAdapter, savedInstanceState)
      } else {
        model.emptyData.postValue(true)
        Toast.makeText(
          requireContext(),
          R.string.pejoy_error_read_external_storage_permission,
          Toast.LENGTH_SHORT
        )
          .show()
      }
    }

    if (requestCode == 0x02) {
      val isWriteExternalStoragePermission =
        permissions.isNotEmpty() && permissions[1] == Manifest.permission.WRITE_EXTERNAL_STORAGE
      val hadGetWriteExternalStoragePermission =
        isWriteExternalStoragePermission && grantResults[1] == PackageManager.PERMISSION_GRANTED

      if (hadGetWriteExternalStoragePermission) {
        mediaStoreCompat.dispatchCaptureIntent(requireContext(), Pejoy.REQUEST_CODE_CAPTURE)
      } else {
        Toast.makeText(
          requireContext(),
          R.string.pejoy_error_read_external_storage_permission,
          Toast.LENGTH_SHORT
        ).show()
      }
    }

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
    albumMediaAdapter.setHasStableIds(true)
    recyclerView.adapter = albumMediaAdapter
    albumMediaAdapter.setOnCheckedViewStateChangeListener {
      updateBottomToolbar()
    }
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        Log.d("tag", "newState : ${newState}")
        when (newState) {
          RecyclerView.SCROLL_STATE_IDLE -> {
            SelectionSpec.instance.requireImageEngine().resume(requireContext().applicationContext)
          }
          else -> {
            SelectionSpec.instance.requireImageEngine().pause(requireContext().applicationContext)
          }
        }
      }
    })
  }

  private fun subscribeOnUI() {
    buttonApply.setOnClickListener(this)
    original.setOnClickListener(this)
    buttonAlbumCategory.setOnClickListener(this)
    buttonPreview.setOnClickListener(this)
    imageBack.setOnClickListener(this)
    model.currentAlbum.observe(this, Observer {
      if (it != null) {
        buttonAlbumCategory.text = it.getDisplayName(requireContext())
      }
    })
    model.emptyData.observe(this, Observer { isEmpty ->
      if (isEmpty == true) {
        ivEmptyData.visibility = View.VISIBLE
      } else {
        ivEmptyData.visibility = View.GONE
      }
    })
  }

  private fun setResult() {
    val result = Intent()
    val selectedPaths = model.selectedItemCollection.asListOfString() as ArrayList<String>
    result.putStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH, selectedPaths)
    result.putParcelableArrayListExtra(
      Pejoy.EXTRA_RESULT_SELECTION,
      model.selectedItemCollection.asListOfUri() as ArrayList<Uri>
    )
    result.putExtra(Pejoy.EXTRA_RESULT_ORIGIN_ENABLE, model.originEnabled)

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
        spec.onOriginCheckedListener?.invoke(model.originEnabled)
        updateBottomToolbar()
      }
      R.id.buttonAlbumCategory -> {
        albumContentView.switch()
      }
      R.id.buttonPreview -> {
        val intent = Intent(requireContext(), SelectedAlbumPreviewActivity::class.java)
        intent.putExtra(
          AbstractPreviewActivity.EXTRA_BUNDLE_ITEMS, model.selectedItemCollection.dataWithBundle
        )
        intent.putExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_ORIGIN_ENABLE, model.originEnabled)
        startActivityForResult(intent, AbstractPreviewActivity.REQUEST_CODE)
      }
      R.id.imageBack -> {
        requireActivity().finish()
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    model.onSaveInstanceState(outState)
    mediaStoreCompat.onSaveInstanceState(outState)
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

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == AbstractPreviewActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
      val apply = data.getBooleanExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_RESULT_APPLY, false)

      val originEnabled =
        data.getBooleanExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_ORIGIN_ENABLE, false)

      val bundle = data.getBundleExtra(AbstractPreviewActivity.EXTRA_BUNDLE_ITEMS)
      val collectionType = bundle.getInt(
        SelectedItemCollection.STATE_COLLECTION_TYPE,
        SelectedItemCollection.COLLECTION_UNDEFINED
      )
      val selected =
        bundle.getParcelableArrayList<Item>(SelectedItemCollection.STATE_SELECTION) ?: ArrayList()

      model.selectedItemCollection.overwrite(selected, collectionType)

      model.originEnabled = originEnabled

      if (!apply) {
        albumMediaAdapter.notifyDataSetChanged()
        updateBottomToolbar()
      } else {
        setResult()
        requireActivity().finish()
      }
    } else if (requestCode == Pejoy.REQUEST_CODE_CAPTURE && resultCode == Activity.RESULT_OK) {
      // Just pass the data back to previous calling Activity.
      val contentUri = mediaStoreCompat.getCurrentPhotoUri()
      val path = mediaStoreCompat.getCurrentPhotoPath()
      val selected = ArrayList<Uri>()
      selected.add(contentUri)
      val selectedPath = ArrayList<String>()
      selectedPath.add(path)
      val result = Intent()
      result.putExtra(Pejoy.EXTRA_RESULT_ORIGIN_ENABLE, model.originEnabled)
      result.putParcelableArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION, selected)
      result.putStringArrayListExtra(Pejoy.EXTRA_RESULT_SELECTION_PATH, selectedPath)
      val activity = requireActivity()
      activity.setResult(Activity.RESULT_OK, result)

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        activity.revokeUriPermission(
          contentUri,
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      }

      if (spec.captureInsertAlbum) {
        mediaStoreCompat.insertAlbum(requireContext(), path)
        activity.finish()
      } else {
        activity.finish()
      }
    }
  }

  fun capture() {
    requestPermissions(
      arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x2
    )
  }
}