/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.os.EnvironmentCompat

import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import io.github.keep2iron.pejoy.internal.entity.CaptureStrategy

class MediaStoreCompat {

    private val mContext: WeakReference<Activity>
    private var mFragment: WeakReference<Fragment>? = null
    private var mCaptureStrategy: CaptureStrategy? = null
    private lateinit var currentPhotoUri: Uri
    private lateinit var currentPhotoPath: String

    constructor(activity: Activity, fragment: Fragment? = null) {
        mContext = WeakReference(activity)

        if (fragment != null) {
            mFragment = WeakReference(fragment)
        }
    }

    fun setCaptureStrategy(strategy: CaptureStrategy) {
        mCaptureStrategy = strategy
    }

    fun dispatchCaptureIntent(context: Context, requestCode: Int) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(context.packageManager) != null) {
            val photoFile: File? = createImageFile()

            if (photoFile != null) {
                currentPhotoPath = photoFile.absolutePath
                currentPhotoUri = FileProvider.getUriForFile(context,
                        mCaptureStrategy!!.authority, photoFile)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    val resInfoList = context.packageManager
                            .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        context.grantUriPermission(packageName, currentPhotoUri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                val fragment = mFragment?.get()
                if (fragment != null) {
                    fragment.startActivityForResult(captureIntent, requestCode)
                } else {
                    mContext.get()?.startActivityForResult(captureIntent, requestCode)
                }
            }
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = String.format("JPEG_%s.jpg", timeStamp)
        val storageDir: File?
        if (mCaptureStrategy!!.isPublic) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES)
            if (!storageDir!!.exists()) {
                storageDir.mkdirs()
            }
        } else {
            storageDir = mContext.get()?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }

        // Avoid joining path components manually
        val tempFile = File(storageDir, imageFileName)

        // Handle the situation that user's external storage is not ready
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile

    }

    fun getCurrentPhotoUri(): Uri {
        return currentPhotoUri
    }

    fun getCurrentPhotoPath(): String {
        return currentPhotoPath
    }

    companion object {

        /**
         * Checks whether the device has a camera feature or not.
         *
         * @param context a context to check for camera feature.
         * @return true if the device has a camera feature. false otherwise.
         */
        fun hasCameraFeature(context: Context): Boolean {
            val pm = context.applicationContext.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }
}