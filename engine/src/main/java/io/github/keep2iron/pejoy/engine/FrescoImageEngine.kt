package io.github.keep2iron.pejoy.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import io.github.keep2iron.pineapple.ImageLoader
import io.github.keep2iron.pineapple.ImageLoaderManager
import io.github.keep2iron.pineapple.ImageLoaderOptions
import io.github.keep2iron.pineapple.MiddlewareView

/**
 *
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2018/07/16 14:43
 */
class FrescoImageEngine : ImageEngine {

    private val imageLoader: ImageLoader = ImageLoaderManager.getInstance()

    override fun provideImageView(context: Context): View {
        return MiddlewareView(context)
    }

    override fun loadBitmapByPath(context: Context, path: String): Bitmap {
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
            scaleType = ImageLoaderOptions.ScaleType.FIT_CENTER
            placeHolder = placeholder
            placeHolderRes = 0
            isLoadGif = true
        }
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: View, uri: Uri) {
        imageLoader.showImageView(
            imageView as MiddlewareView, uri
        ) {
            imageWidth = resizeX
            imageHeight = resizeY
            scaleType = ImageLoaderOptions.ScaleType.FIT_CENTER
        }
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: View, uri: Uri) {
        imageLoader.showImageView(
            imageView as MiddlewareView, uri
        ) {
            imageWidth = resizeX
            imageHeight = resizeY
            scaleType = ImageLoaderOptions.ScaleType.FIT_CENTER
            isLoadGif = true
        }
    }


    override fun supportAnimatedGif(): Boolean = true
}