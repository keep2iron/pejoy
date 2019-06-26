package io.github.keep2iron.pejoy.ui

import android.os.Bundle
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection

class SelectedAlbumPreviewActivity : AbstractPreviewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.instance.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val bundle = intent.getBundleExtra(EXTRA_BUNDLE_ITEMS)
        val selected = bundle.getParcelableArrayList<Item>(SelectedItemCollection.STATE_SELECTION)
        adapter.addAll(selected)
        adapter.notifyDataSetChanged()

        if (selectionSpec.countable) {
            checkView.setCheckedNum(1)
        } else {
            checkView.setChecked(true)
        }
        previousPos = 0

        updateToolbar(selected[0])
    }

}