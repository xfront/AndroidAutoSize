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
import me.jessyan.autosize.external.ExternalAdaptInfo
import me.jessyan.autosize.internal.CancelAdapt
import me.jessyan.autosize.internal.CustomAdapt
import me.jessyan.autosize.utils.AutoSizeLog
import java.util.Locale

/**
 * ================================================
 * 屏幕适配逻辑策略默认实现类, 可通过 [AutoSizeConfig.init]
 * 和 [AutoSizeConfig.setAutoAdaptStrategy] 切换策略
 *
 * @see AutoAdaptStrategy
 * Created by JessYan on 2018/8/9 15:57
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class DefaultAutoAdaptStrategy : AutoAdaptStrategy {
    override fun applyAdapt(target: Any, activity: Activity) {
        //检查是否开启了外部三方库的适配模式, 只要不主动调用 ExternalAdaptManager 的方法, 下面的代码就不会执行

        if (AutoSizeConfig.externalAdaptManager.isRun) {
            if (AutoSizeConfig.externalAdaptManager.isCancelAdapt(target.javaClass)) {
                AutoSizeLog.w(
                    String.format(
                        Locale.ENGLISH,
                        "%s canceled the adaptation!",
                        target.javaClass.name
                    )
                )
                AutoSize.cancelAdapt(activity)
                return
            } else {
                val info = AutoSizeConfig.externalAdaptManager
                    .getExternalAdaptInfoOfActivity(target.javaClass)
                if (info != null) {
                    AutoSizeLog.d(
                        String.format(
                            Locale.ENGLISH,
                            "%s used %s for adaptation!",
                            target.javaClass.name,
                            ExternalAdaptInfo::class.java.name
                        )
                    )
                    AutoSize.autoConvertDensityOfExternalAdaptInfo(activity, info)
                    return
                }
            }
        }

        //如果 target 实现 CancelAdapt 接口表示放弃适配, 所有的适配效果都将失效
        if (target is CancelAdapt) {
            AutoSizeLog.w(
                String.format(
                    Locale.ENGLISH,
                    "%s canceled the adaptation!",
                    target.javaClass.name
                )
            )
            AutoSize.cancelAdapt(activity)
            return
        }

        //如果 target 实现 CustomAdapt 接口表示该 target 想自定义一些用于适配的参数, 从而改变最终的适配效果
        if (target is CustomAdapt) {
            AutoSizeLog.d(
                String.format(
                    Locale.ENGLISH,
                    "%s implemented by %s!",
                    target.javaClass.name,
                    CustomAdapt::class.java.name
                )
            )
            AutoSize.autoConvertDensityOfCustomAdapt(activity, target)
        } else {
            AutoSizeLog.d(
                String.format(
                    Locale.ENGLISH,
                    "%s used the global configuration.",
                    target.javaClass.name
                )
            )
            AutoSize.autoConvertDensityOfGlobal(activity)
        }
    }
}
