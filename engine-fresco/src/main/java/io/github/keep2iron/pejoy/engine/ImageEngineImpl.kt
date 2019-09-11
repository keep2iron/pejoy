package io.github.keep2iron.pejoy.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import io.github.keep2iron.pineapple.ImageLoader
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import io.github.keep2iron.pineapple.MiddlewareView
import me.relex.photodraweeview.PhotoDraweeView

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/16 14:43
 */
class ImageEngineImpl : ImageEngine {
  private val imageLoader: ImageLoader = ImageLoaderManager.getInstance()

  override fun provideImageView(context: Context): View {
    return MiddlewareView(context.applicationContext)
  }

  override fun loadBitmapByPath(
    context: Context,
    path: String
  ): Bitmap {
    return BitmapFactory.decodeFile(path)
  }

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
      placeHolder = placeholder
      placeHolderRes = 0
      isLoadGif = false
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
    val mPhotoDraweeView = imageView as PhotoDraweeView

//        mPhotoDraweeView.hierarchy = GenericDraweeHierarchyBuilder(mPhotoDraweeView.context.resources)
//            .setFadeDuration(300)
//            .build()

    mPhotoDraweeView.setPhotoUri(uri, context)
  }

  override fun loadGifImage(
    context: Context,
    resizeX: Int,
    resizeY: Int,
    placeholder: Drawable?,
    imageView: View,
    uri: Uri
  ) {
    val mPhotoDraweeView = imageView as PhotoDraweeView

    val controller = Fresco.newDraweeControllerBuilder()
        .setCallerContext(context.applicationContext)
        .setUri(uri)
        .setOldController(mPhotoDraweeView.controller)
        .setAutoPlayAnimations(true)
        .setControllerListener(object : BaseControllerListener<ImageInfo>() {
          override fun onFailure(
            id: String?,
            throwable: Throwable?
          ) {
            super.onFailure(id, throwable)
            mPhotoDraweeView.isEnableDraweeMatrix = false
          }

          override fun onFinalImageSet(
            id: String?,
            imageInfo: ImageInfo?,
            animatable: Animatable?
          ) {
            super.onFinalImageSet(id, imageInfo, animatable)
            mPhotoDraweeView.isEnableDraweeMatrix = true
            if (imageInfo != null) {
              mPhotoDraweeView.update(imageInfo.width, imageInfo.height)
            }
          }

          override fun onIntermediateImageFailed(
            id: String?,
            throwable: Throwable?
          ) {
            super.onIntermediateImageFailed(id, throwable)
            mPhotoDraweeView.isEnableDraweeMatrix = false
          }

          override fun onIntermediateImageSet(
            id: String?,
            imageInfo: ImageInfo?
          ) {
            super.onIntermediateImageSet(id, imageInfo)
            mPhotoDraweeView.isEnableDraweeMatrix = true
            if (imageInfo != null) {
              mPhotoDraweeView.update(imageInfo.width, imageInfo.height)
            }
          }
        })
        .build()
    mPhotoDraweeView.controller = controller
  }

  override fun provideScaleImageView(context: Context): View {
    return PhotoDraweeView(context)
  }

  override fun supportAnimatedGif(): Boolean = true

  override fun resetViewMatrix(view: View) {
    (view as PhotoDraweeView).attacher.let {
      it.drawMatrix.reset()
      it.checkMatrixBounds()
      it.draweeView?.invalidate()
    }
  }

  override fun resume(context: Context) {
    imageLoader.resume(context)
  }

  override fun pause(context: Context) {
    imageLoader.pause(context)
  }
}