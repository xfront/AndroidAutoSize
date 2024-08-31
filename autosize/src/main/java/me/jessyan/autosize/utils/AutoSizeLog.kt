/*
 * Copyright 2018 JessYan
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
package me.jessyan.autosize.utils

import android.util.Log

/**
 * ================================================
 * Created by JessYan on 2018/8/8 18:48
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object AutoSizeLog {

    private const val TAG = "AndroidAutoSize"
    @JvmStatic
    var isDebug: Boolean = false
    @JvmStatic
    fun d(message: String?) {
        if (isDebug) {
            Log.d(TAG, message!!)
        }
    }
    @JvmStatic
    fun w(message: String?) {
        if (isDebug) {
            Log.w(TAG, message!!)
        }
    }
    @JvmStatic
    fun e(message: String?) {
        if (isDebug) {
            Log.e(TAG, message!!)
        }
    }
}
