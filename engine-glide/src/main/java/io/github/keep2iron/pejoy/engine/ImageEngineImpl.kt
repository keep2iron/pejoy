package io.github.keep2iron.pejoy.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.github.chrisbanes.photoview.PhotoView
import io.github.keep2iron.pineapple.ImageLoader
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import io.github.keep2iron.pineapple.MiddlewareView

class ImageEngineImpl : ImageEngine {
  private val imageLoader: ImageLoader = ImageLoaderManager.getInstance()

  override fun loadThumbnail(
    context: Context,
    resize: Int,
    placeholder: Drawable?,
    imageView: View,
    uri: Uri
  ) {
    imageLoader.showImageView(
      imageView as MiddlewareView, uri
    ) {
      imageWidth = resize
      imageHeight = resize
      scaleType = ImageLoaderOptions.ScaleType.CENTER_CROP
      placeHolderRes = 0
      placeHolder = placeholder
      isLoadGif = false
    }
  }

  override fun loadGifThumbnail(
    context: Context,
    resize: Int,
    placeholder: Drawable?,
    imageView: View,
    uri: Uri
  ) {
    imageLoader.showImageView(
      imageView as MiddlewareView, uri
    ) {
      imageWidth = resize
      imageHeight = resize
      scaleType = ImageLoaderOptions.ScaleType.CENTER_CROP
      placeHolderRes = 0
      placeHolder = placeholder
      isLoadGif = true
    }
  }

  override fun loadImage(
    context: Context,
    resizeX: Int,
    resizeY: Int,
    placeholder: Drawable?,
    imageView: View,
    uri: Uri
  ) {
    val photoView = imageView as PhotoView
    photoView.setImageURI(uri)
    photoView.setImageDrawable(placeholder)
  }

  override fun loadGifImage(
    context: Context,
    resizeX: Int,
    resizeY: Int,
    placeholder: Drawable?,
    imageView: View,
    uri: Uri
  ) {
  }

  override fun supportAnimatedGif(): Boolean = true

  override fun loadBitmapByPath(context: Context, path: String): Bitmap {
    return BitmapFactory.decodeFile(path)
  }

  override fun provideImageView(context: Context): View = MiddlewareView(context)

  override fun provideScaleImageView(context: Context): View = PhotoView(context)

  override fun resetViewMatrix(view: View) {
  }

  override fun resume(context: Context) {
    imageLoader.resume(context)
  }

  override fun pause(context: Context) {
    imageLoader.pause(context)
  }
}