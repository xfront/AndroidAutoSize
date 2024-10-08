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

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

/**
 * ================================================
 * Created by JessYan on 26/09/2016 16:59
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object ScreenUtils {
    @JvmStatic
    val statusBarHeight: Int
        get() {
            var result = 0
            try {
                val resourceId =
                    Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    result = Resources.getSystem().getDimensionPixelSize(resourceId)
                }
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * 获取当前的屏幕尺寸
     *
     * @param context [Context]
     * @return 屏幕尺寸
     */
    @JvmStatic
    fun getScreenSize(context: Context): IntArray {
        val size = IntArray(2)

        val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = w.defaultDisplay
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)

        size[0] = metrics.widthPixels
        size[1] = metrics.heightPixels
        return size
    }

    /**
     * 获取原始的屏幕尺寸
     *
     * @param context [Context]
     * @return 屏幕尺寸
     */
    @JvmStatic
    fun getRawScreenSize(context: Context): IntArray {
        val size = IntArray(2)

        val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = w.defaultDisplay
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)
        // since SDK_INT = 1;
        var widthPixels = metrics.widthPixels
        var heightPixels = metrics.heightPixels

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) try {
            widthPixels = Display::class.java.getMethod("getRawWidth").invoke(d) as Int
            heightPixels = Display::class.java.getMethod("getRawHeight").invoke(d) as Int
        } catch (ignored: Exception) {
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) try {
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java).invoke(d, realSize)
            widthPixels = realSize.x
            heightPixels = realSize.y
        } catch (ignored: Exception) {
        }
        size[0] = widthPixels
        size[1] = heightPixels
        return size
    }
    @JvmStatic
    fun getHeightOfNavigationBar(context: Context): Int {
        //如果小米手机开启了全面屏手势隐藏了导航栏则返回 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.Global.getInt(context.contentResolver, "force_fsg_nav_bar", 0) != 0) {
                return 0
            }
        }

        val realHeight = getRawScreenSize(context)[1]
        val displayHeight = getScreenSize(context)[1]
        return realHeight - displayHeight
    }
}
