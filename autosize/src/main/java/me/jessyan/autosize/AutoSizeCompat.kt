/*
 * Copyright 2019 JessYan
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
package me.jessyan.autosize

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.SparseArray
import me.jessyan.autosize.external.ExternalAdaptInfo
import me.jessyan.autosize.internal.CustomAdapt
import me.jessyan.autosize.unit.Subunits
import me.jessyan.autosize.utils.Preconditions

/**
 * ================================================
 * 当遇到本来适配正常的布局突然出现适配失效，适配异常等问题, 重写当前 [Activity] 的 [Activity.getResources] 并调用
 * [AutoSizeCompat] 的对应方法即可解决问题
 *
 *
 * Created by JessYan on 2018/8/8 19:20
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object AutoSizeCompat {
    private val mCache = SparseArray<DisplayMetricsInfo>()
    private const val MODE_SHIFT = 30
    private const val MODE_MASK = 0x3 shl MODE_SHIFT
    private const val MODE_ON_WIDTH = 1 shl MODE_SHIFT
    private const val MODE_DEVICE_SIZE = 2 shl MODE_SHIFT

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     *
     * @param resources [Resources]
     */
    @JvmStatic
    fun autoConvertDensityOfGlobal(resources: Resources) {
        if (AutoSizeConfig.isBaseOnWidth) {
            autoConvertDensityBaseOnWidth(resources, AutoSizeConfig.designWidthInDp.toFloat())
        } else {
            autoConvertDensityBaseOnHeight(resources, AutoSizeConfig.designHeightInDp.toFloat())
        }
    }

    /**
     * 使用 [Activity] 或 Fragment 的自定义参数进行适配
     *
     * @param resources   [Resources]
     * @param customAdapt [Activity] 或 Fragment 需实现 [CustomAdapt]
     */
    @JvmStatic
    fun autoConvertDensityOfCustomAdapt(resources: Resources, customAdapt: CustomAdapt) {
        var sizeInDp = customAdapt.sizeInDp

        //如果 CustomAdapt#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            sizeInDp = if (customAdapt.isBaseOnWidth) {
                AutoSizeConfig.designWidthInDp.toFloat()
            } else {
                AutoSizeConfig.designHeightInDp.toFloat()
            }
        }
        autoConvertDensity(resources, sizeInDp, customAdapt.isBaseOnWidth)
    }

    /**
     * 使用外部三方库的 [Activity] 或 Fragment 的自定义适配参数进行适配
     *
     * @param resources         [Resources]
     * @param externalAdaptInfo 三方库的 [Activity] 或 Fragment 提供的适配参数, 需要配合 [ExternalAdaptManager.addExternalAdaptInfoOfActivity]
     */
    @JvmStatic
    fun autoConvertDensityOfExternalAdaptInfo(
        resources: Resources,
        externalAdaptInfo: ExternalAdaptInfo
    ) {
        var sizeInDp = externalAdaptInfo.sizeInDp

        //如果 ExternalAdaptInfo#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            sizeInDp = if (externalAdaptInfo.isBaseOnWidth) {
                AutoSizeConfig.designWidthInDp.toFloat()
            } else {
                AutoSizeConfig.designHeightInDp.toFloat()
            }
        }
        autoConvertDensity(resources, sizeInDp, externalAdaptInfo.isBaseOnWidth)
    }

    /**
     * 以宽度为基准进行适配
     *
     * @param resources       [Resources]
     * @param designWidthInDp 设计图的总宽度
     */
    @JvmStatic
    fun autoConvertDensityBaseOnWidth(resources: Resources, designWidthInDp: Float) {
        autoConvertDensity(resources, designWidthInDp, true)
    }

    /**
     * 以高度为基准进行适配
     *
     * @param resources        [Resources]
     * @param designHeightInDp 设计图的总高度
     */
    @JvmStatic
    fun autoConvertDensityBaseOnHeight(resources: Resources, designHeightInDp: Float) {
        autoConvertDensity(resources, designHeightInDp, false)
    }

    /**
     * 这里是今日头条适配方案的核心代码, 核心在于根据当前设备的实际情况做自动计算并转换 [DisplayMetrics.density]、
     * [DisplayMetrics.scaledDensity]、[DisplayMetrics.densityDpi] 这三个值, 额外增加 [DisplayMetrics.xdpi]
     * 以支持单位 `pt`、`in`、`mm`
     *
     * @param resources     [Resources]
     * @param sizeInDp      设计图上的设计尺寸, 单位 dp, 如果 {@param isBaseOnWidth} 设置为 `true`,
     * {@param sizeInDp} 则应该填写设计图的总宽度, 如果 {@param isBaseOnWidth} 设置为 `false`,
     * {@param sizeInDp} 则应该填写设计图的总高度
     * @param isBaseOnWidth 是否按照宽度进行等比例适配, `true` 为以宽度进行等比例适配, `false` 为以高度进行等比例适配
     * @see [今日头条官方适配方案](https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA)
     */
    @JvmStatic
    fun autoConvertDensity(resources: Resources, sizeInDp: Float, isBaseOnWidth: Boolean) {
        Preconditions.checkMainThread()

        var subunitsDesignSize = if (isBaseOnWidth)
            AutoSizeConfig.unitsManager.designWidth
        else
            AutoSizeConfig.unitsManager.designHeight
        subunitsDesignSize = if (subunitsDesignSize > 0) subunitsDesignSize else sizeInDp

        val screenSize = if (isBaseOnWidth) AutoSizeConfig.screenWidth else AutoSizeConfig.screenHeight

        var key =
            Math.round((sizeInDp + subunitsDesignSize + screenSize) * AutoSizeConfig.initScaledDensity) and MODE_MASK.inv()
        key = if (isBaseOnWidth) (key or MODE_ON_WIDTH) else (key and MODE_ON_WIDTH.inv())
        key =
            if (AutoSizeConfig.isUseDeviceSize) (key or MODE_DEVICE_SIZE) else (key and MODE_DEVICE_SIZE.inv())

        val displayMetricsInfo = mCache[key]

        var targetDensity = 0f
        var targetDensityDpi = 0
        var targetScaledDensity = 0f
        var targetXdpi = 0f
        val targetScreenWidthDp: Int
        val targetScreenHeightDp: Int

        if (displayMetricsInfo == null) {
            targetDensity = if (isBaseOnWidth) {
                AutoSizeConfig.screenWidth * 1.0f / sizeInDp
            } else {
                AutoSizeConfig.screenHeight * 1.0f / sizeInDp
            }
            if (AutoSizeConfig.privateFontScale > 0) {
                targetScaledDensity = targetDensity * AutoSizeConfig.privateFontScale
            } else {
                val systemFontScale =
                    if (AutoSizeConfig.isExcludeFontScale) 1f else AutoSizeConfig.initScaledDensity * 1.0f / AutoSizeConfig.initDensity
                targetScaledDensity = targetDensity * systemFontScale
            }
            targetDensityDpi = (targetDensity * 160).toInt()

            targetScreenWidthDp = (AutoSizeConfig.screenWidth / targetDensity).toInt()
            targetScreenHeightDp = (AutoSizeConfig.screenHeight / targetDensity).toInt()

            targetXdpi = if (isBaseOnWidth) {
                AutoSizeConfig.screenWidth * 1.0f / subunitsDesignSize
            } else {
                AutoSizeConfig.screenHeight * 1.0f / subunitsDesignSize
            }

            mCache.put(
                key,
                DisplayMetricsInfo(
                    targetDensity,
                    targetDensityDpi,
                    targetScaledDensity,
                    targetXdpi,
                    targetScreenWidthDp,
                    targetScreenHeightDp
                )
            )
        } else {
            targetDensity = displayMetricsInfo.density
            targetDensityDpi = displayMetricsInfo.densityDpi
            targetScaledDensity = displayMetricsInfo.scaledDensity
            targetXdpi = displayMetricsInfo.xdpi
            targetScreenWidthDp = displayMetricsInfo.screenWidthDp
            targetScreenHeightDp = displayMetricsInfo.screenHeightDp
        }

        setDensity(resources, targetDensity, targetDensityDpi, targetScaledDensity, targetXdpi)
        setScreenSizeDp(resources, targetScreenWidthDp, targetScreenHeightDp)
    }

    /**
     * 取消适配
     *
     * @param resources [Resources]
     */
    @JvmStatic
    fun cancelAdapt(resources: Resources) {
        Preconditions.checkMainThread()
        var initXdpi = AutoSizeConfig.initXdpi
        when (AutoSizeConfig.unitsManager.supportSubunits) {
            Subunits.PT -> initXdpi = initXdpi / 72f
            Subunits.MM -> initXdpi = initXdpi / 25.4f
            else -> {}
        }
        setDensity(
            resources, AutoSizeConfig.initDensity,
            AutoSizeConfig.initDensityDpi,
            AutoSizeConfig.initScaledDensity,
            initXdpi
        )
        setScreenSizeDp(
            resources,
            AutoSizeConfig.initScreenWidthDp,
            AutoSizeConfig.initScreenHeightDp
        )
    }

    /**
     * 给几大 [DisplayMetrics] 赋值
     *
     * @param resources     [Resources]
     * @param density       [DisplayMetrics.density]
     * @param densityDpi    [DisplayMetrics.densityDpi]
     * @param scaledDensity [DisplayMetrics.scaledDensity]
     * @param xdpi          [DisplayMetrics.xdpi]
     */
    private fun setDensity(
        resources: Resources,
        density: Float,
        densityDpi: Int,
        scaledDensity: Float,
        xdpi: Float
    ) {
        val activityDisplayMetrics = resources.displayMetrics
        setDensity(activityDisplayMetrics, density, densityDpi, scaledDensity, xdpi)
        val appDisplayMetrics = AutoSizeConfig.application.resources.displayMetrics
        setDensity(appDisplayMetrics, density, densityDpi, scaledDensity, xdpi)

        //兼容 MIUI
        val activityDisplayMetricsOnMIUI = getMetricsOnMiui(resources)
        val appDisplayMetricsOnMIUI = getMetricsOnMiui(
            AutoSizeConfig.application.resources
        )

        if (activityDisplayMetricsOnMIUI != null) {
            setDensity(activityDisplayMetricsOnMIUI, density, densityDpi, scaledDensity, xdpi)
        }
        if (appDisplayMetricsOnMIUI != null) {
            setDensity(appDisplayMetricsOnMIUI, density, densityDpi, scaledDensity, xdpi)
        }
    }

    /**
     * 赋值
     *
     * @param displayMetrics [DisplayMetrics]
     * @param density        [DisplayMetrics.density]
     * @param densityDpi     [DisplayMetrics.densityDpi]
     * @param scaledDensity  [DisplayMetrics.scaledDensity]
     * @param xdpi           [DisplayMetrics.xdpi]
     */
    private fun setDensity(
        displayMetrics: DisplayMetrics,
        density: Float,
        densityDpi: Int,
        scaledDensity: Float,
        xdpi: Float
    ) {
        if (AutoSizeConfig.unitsManager.isSupportDP) {
            displayMetrics.density = density
            displayMetrics.densityDpi = densityDpi
        }
        if (AutoSizeConfig.unitsManager.isSupportSP) {
            displayMetrics.scaledDensity = scaledDensity
        }
        when (AutoSizeConfig.unitsManager.supportSubunits) {
            Subunits.NONE -> {}
            Subunits.PT -> displayMetrics.xdpi = xdpi * 72f
            Subunits.IN -> displayMetrics.xdpi = xdpi
            Subunits.MM -> displayMetrics.xdpi = xdpi * 25.4f
            else -> {}
        }
    }

    /**
     * 给 [Configuration] 赋值
     *
     * @param resources      [Resources]
     * @param screenWidthDp  [Configuration.screenWidthDp]
     * @param screenHeightDp [Configuration.screenHeightDp]
     */
    private fun setScreenSizeDp(resources: Resources, screenWidthDp: Int, screenHeightDp: Int) {
        if (AutoSizeConfig.unitsManager.isSupportDP && AutoSizeConfig.unitsManager.isSupportScreenSizeDP) {
            val activityConfiguration = resources.configuration
            setScreenSizeDp(activityConfiguration, screenWidthDp, screenHeightDp)

            val appConfiguration = AutoSizeConfig.application.resources.configuration
            setScreenSizeDp(appConfiguration, screenWidthDp, screenHeightDp)
        }
    }

    /**
     * Configuration赋值
     *
     * @param configuration  [Configuration]
     * @param screenWidthDp  [Configuration.screenWidthDp]
     * @param screenHeightDp [Configuration.screenHeightDp]
     */
    private fun setScreenSizeDp(
        configuration: Configuration,
        screenWidthDp: Int,
        screenHeightDp: Int
    ) {
        configuration.screenWidthDp = screenWidthDp
        configuration.screenHeightDp = screenHeightDp
    }

    /**
     * 解决 MIUI 更改框架导致的 MIUI7 + Android5.1.1 上出现的失效问题 (以及极少数基于这部分 MIUI 去掉 ART 然后置入 XPosed 的手机)
     * 来源于: https://github.com/Firedamp/Rudeness/blob/master/rudeness-sdk/src/main/java/com/bulong/rudeness/RudenessScreenHelper.java#L61:5
     *
     * @param resources [Resources]
     * @return [DisplayMetrics], 可能为 `null`
     */
    private fun getMetricsOnMiui(resources: Resources): DisplayMetrics? {
        if (AutoSizeConfig.isMiui && AutoSizeConfig.tmpMetricsField != null) {
            return try {
                AutoSizeConfig.tmpMetricsField!![resources] as DisplayMetrics
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}