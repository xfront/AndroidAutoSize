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
package me.jessyan.autosize

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle

/**
 * ================================================
 * [ActivityLifecycleCallbacksImpl] 可用来代替在 BaseActivity 中加入适配代码的传统方式
 * [ActivityLifecycleCallbacksImpl] 这种方案类似于 AOP, 面向接口, 侵入性低, 方便统一管理, 扩展性强, 并且也支持适配三方库的 [Activity]
 *
 *
 * Created by JessYan on 2018/8/8 14:32
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class ActivityLifecycleCallbacksImpl(private var autoAdaptStrategy: AutoAdaptStrategy?) :
    ActivityLifecycleCallbacks {
    /**
     * 屏幕适配逻辑策略类
     */
    //private var autoAdaptStrategy: AutoAdaptStrategy?

    /**
     * 让 Fragment 支持自定义适配参数
     */
    private var fragmentLifecycleCallbacksToSys: FragmentLifecycleCallbacksImplToSys? = null
    private var fragmentLifecycleCallbacksToSupport: FragmentLifecycleCallbacksImplToSupport? = null
    private var fragmentLifecycleCallbacksToAndroidx: FragmentLifecycleCallbacksImplToAndroidx? = null

    init {
        if (AutoSizeConfig.DEPENDENCY_ANDROIDX) {
            fragmentLifecycleCallbacksToAndroidx = FragmentLifecycleCallbacksImplToAndroidx(autoAdaptStrategy)
        } else if (AutoSizeConfig.DEPENDENCY_SUPPORT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fragmentLifecycleCallbacksToSupport = FragmentLifecycleCallbacksImplToSupport(autoAdaptStrategy)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fragmentLifecycleCallbacksToSys = FragmentLifecycleCallbacksImplToSys(autoAdaptStrategy)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (AutoSizeConfig.isCustomFragment) {
            if (fragmentLifecycleCallbacksToAndroidx != null && activity is androidx.fragment.app.FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacksToAndroidx!!,
                    true
                )
            } else if (fragmentLifecycleCallbacksToSupport != null && activity is android.support.v4.app.FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacksToSupport!!,
                    true
                )
            } else if (fragmentLifecycleCallbacksToSys != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacksToSys, true)
                }
            }
        }

        //Activity 中的 setContentView(View) 一定要在 super.onCreate(Bundle); 之后执行
        autoAdaptStrategy?.applyAdapt(activity, activity)
    }

    override fun onActivityStarted(activity: Activity) {
        autoAdaptStrategy?.applyAdapt(activity, activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    /**
     * 设置屏幕适配逻辑策略类
     *
     * @param autoAdaptStrategy [AutoAdaptStrategy]
     */
    fun setAutoAdaptStrategy(autoAdaptStrategy: AutoAdaptStrategy?) {
        this.autoAdaptStrategy = autoAdaptStrategy
        fragmentLifecycleCallbacksToAndroidx?.setAutoAdaptStrategy(autoAdaptStrategy)
        fragmentLifecycleCallbacksToSupport?.setAutoAdaptStrategy(autoAdaptStrategy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fragmentLifecycleCallbacksToSys?.setAutoAdaptStrategy(autoAdaptStrategy)
        }
    }
}
