package io.github.keep2iron.pejoy.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.Pejoy
import io.github.keep2iron.pejoy.engine.FrescoImageEngine
import keep2iron.github.io.compress.weatherCompressImage

class ChildFragment : Fragment() {

  private val tvImageResult by lazy {
    view!!.findViewById<TextView>(R.id.tvImageResult)
  }

  private val imageResultBuilder = StringBuilder()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.activity_main, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<View>(R.id.btnGetImages).setOnClickListener {
      Pejoy.create(this)
        .choose(MimeType.ofAll(), false)
        .maxSelectable(3)
        .theme(R.style.Pejoy_Dracula)
        .countable(true)
        .originalEnable(enable = true, originalSelectDefault = true)
        .capture(true, enableInsertAlbum = true)
        .imageEngine(FrescoImageEngine())
        .setOnOriginCheckedListener { isChecked ->
          Log.d("keep2iron", "isChecked : $isChecked")
        }
        .toObservable()
        .weatherCompressImage(requireContext())
        .subscribe {
          imageResultBuilder.append("[\n")
          it.forEach { uri ->
            imageResultBuilder.apply {
              append(uri)
              if (uri != it.last()) {
                append("\n")
              } else {
                append("\n]\n")
              }
            }
          }
          tvImageResult.text = imageResultBuilder.toString()
          Log.d("keep2iron", it.toString() + "this : " + this.hashCode())
        }
    }
  }

}