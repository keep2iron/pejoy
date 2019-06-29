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
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.internal.entity

import android.content.Context
import android.support.annotation.IntDef
import android.widget.Toast

class IncapableCause @JvmOverloads constructor(
    @Form val form: Int? = TOAST, val title: String? = null,
    val message: String
) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TOAST, DIALOG, NONE)
    annotation class Form


    companion object {
        const val TOAST = 0x00
        const val DIALOG = 0x01
        const val NONE = 0x02

        fun handleCause(context: Context, cause: IncapableCause?) {
            if (cause == null) {
                return
            }

            when (cause.form) {
                NONE -> {
                }
                DIALOG -> {
                }
                TOAST -> Toast.makeText(context, cause.message, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context, cause.message, Toast.LENGTH_SHORT).show()
            }// do nothing.
            //                IncapableDialog incapableDialog = IncapableDialog.newInstance(cause.title, cause.message);
            //                incapableDialog.show(((FragmentActivity) context).getSupportFragmentManager(),
            //                        IncapableDialog.class.getName());
        }
    }
}