package io.github.keep2iron.pejoy.utilities

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri

class CaptureMediaScanner constructor(
  val context: Context,
  val path: String,
  private val onScanningComplete: (() -> Unit)? = null
) : MediaScannerConnection.MediaScannerConnectionClient {

  private var scanner: MediaScannerConnection? = null

  init {
    scanner = MediaScannerConnection(context, this)
    if (!scanner!!.isConnected) {
      scanner!!.connect()
    }
  }

  override fun onMediaScannerConnected() {
    scanner?.scanFile(path, null)
  }

  override fun onScanCompleted(
    path: String,
    uri: Uri
  ) {
    scanner?.disconnect()
    scanner = null
    onScanningComplete?.invoke()
  }
}