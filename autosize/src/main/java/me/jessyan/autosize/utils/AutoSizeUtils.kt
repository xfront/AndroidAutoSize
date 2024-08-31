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

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.TypedValue
import java.lang.reflect.InvocationTargetException

/**
 * ================================================
 * AndroidAutoSize 常用工具类
 *
 *
 * Created by JessYan on 2018/8/25 15:24
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object AutoSizeUtils {
    @JvmStatic
    fun dp2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        ) + 0.5f).toInt()
    }
    @JvmStatic
    fun sp2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.resources.displayMetrics
        ) + 0.5f).toInt()
    }
    @JvmStatic
    fun pt2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PT,
            value,
            context.resources.displayMetrics
        ) + 0.5f).toInt()
    }
    @JvmStatic
    fun in2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_IN,
            value,
            context.resources.displayMetrics
        ) + 0.5f).toInt()
    }
    @JvmStatic
    fun mm2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM,
            value,
            context.resources.displayMetrics
        ) + 0.5f).toInt()
    }
    @JvmStatic
    val applicationByReflect: Application
        get() {
            try {
                @SuppressLint("PrivateApi") val activityThread =
                    Class.forName("android.app.ActivityThread")
                val thread = activityThread.getMethod("currentActivityThread").invoke(null)
                val app = activityThread.getMethod("getApplication").invoke(thread)
                    ?: throw NullPointerException("you should init first")
                return app as Application
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            throw NullPointerException("you should init first")
        }
}
