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

import android.os.Looper

/**
 * ================================================
 * Created by JessYan on 26/09/2016 13:59
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object Preconditions {
    @JvmStatic
    fun checkArgument(expression: Boolean) {
        require(expression)
    }
    @JvmStatic
    fun checkArgument(expression: Boolean, errorMessage: Any) {
        require(expression) { errorMessage.toString() }
    }
    @JvmStatic
    fun checkArgument(
        expression: Boolean,
        errorMessageTemplate: String,
        vararg errorMessageArgs: Any?
    ) {
        require(expression) {
            format(
                errorMessageTemplate,
                *errorMessageArgs
            )
        }
    }
    @JvmStatic
    fun checkState(expression: Boolean) {
        check(expression)
    }

    fun checkState(expression: Boolean, errorMessage: Any) {
        check(expression) { errorMessage.toString() }
    }
    @JvmStatic
    fun checkState(
        expression: Boolean,
        errorMessageTemplate: String,
        vararg errorMessageArgs: Any?
    ) {
        check(expression) {
            format(
                errorMessageTemplate,
                *errorMessageArgs
            )
        }
    }
    @JvmStatic
    fun <T> checkNotNull(reference: T?): T {
        if (reference == null) {
            throw NullPointerException()
        } else {
            return reference
        }
    }
    @JvmStatic
    fun <T> checkNotNull(reference: T?, errorMessage: Any): T {
        if (reference == null) {
            throw NullPointerException(errorMessage.toString())
        } else {
            return reference
        }
    }
    @JvmStatic
    fun <T> checkNotNull(
        reference: T?,
        errorMessageTemplate: String,
        vararg errorMessageArgs: Any?
    ): T {
        if (reference == null) {
            throw NullPointerException(format(errorMessageTemplate, *errorMessageArgs))
        } else {
            return reference
        }
    }

    @JvmOverloads
    fun checkElementIndex(index: Int, size: Int, desc: String = "index"): Int {
        if (index >= 0 && index < size) {
            return index
        } else {
            throw IndexOutOfBoundsException(badElementIndex(index, size, desc))
        }
    }

    /**
     * Throws [IllegalStateException] if the calling thread is not the application's main
     * thread.
     *
     * @throws IllegalStateException If the calling thread is not the application's main thread.
     */
    @JvmStatic
    fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "Not in applications main thread" }
    }

    private fun badElementIndex(index: Int, size: Int, desc: String): String {
        return if (index < 0) {
            format(
                "%s (%s) must not be negative",
                *arrayOf<Any>(desc, index)
            )
        } else if (size < 0) {
            throw IllegalArgumentException(
                StringBuilder(26)
                    .append("negative size: ").append(size).toString()
            )
        } else {
            format(
                "%s (%s) must be less than size (%s)",
                *arrayOf<Any>(desc, index, size)
            )
        }
    }

    @JvmOverloads
    fun checkPositionIndex(index: Int, size: Int, desc: String = "index"): Int {
        if (index >= 0 && index <= size) {
            return index
        } else {
            throw IndexOutOfBoundsException(badPositionIndex(index, size, desc))
        }
    }

    private fun badPositionIndex(index: Int, size: Int, desc: String): String {
        return if (index < 0) {
            format(
                "%s (%s) must not be negative",
                *arrayOf<Any>(desc, index)
            )
        } else if (size < 0) {
            throw IllegalArgumentException(
                StringBuilder(26)
                    .append("negative size: ").append(size).toString()
            )
        } else {
            format(
                "%s (%s) must not be greater than size (%s)",
                *arrayOf<Any>(desc, index, size)
            )
        }
    }
    @JvmStatic
    fun checkPositionIndexes(start: Int, end: Int, size: Int) {
        if (start < 0 || end < start || end > size) {
            throw IndexOutOfBoundsException(badPositionIndexes(start, end, size))
        }
    }

    private fun badPositionIndexes(start: Int, end: Int, size: Int): String {
        return if (start >= 0 && start <= size) (if (end >= 0 && end <= size) format(
            "end index (%s) must not be less than start index (%s)",
            *arrayOf<Any>(end, start)
        ) else badPositionIndex(end, size, "end index")) else badPositionIndex(
            start,
            size,
            "start index"
        )
    }
    @JvmStatic
    fun format(template: String, vararg args: Any?): String {
        var template = template
        template = template.toString()
        val builder = StringBuilder(template.length + 16 * args.size)
        var templateStart = 0
        var placeholderStart: Int
        var i = 0
        while (i < args.size) {
            placeholderStart = template.indexOf("%s", templateStart)
            if (placeholderStart == -1) {
                break
            }

            builder.append(template.substring(templateStart, placeholderStart))
            builder.append(args[i++])
            templateStart = placeholderStart + 2
        }

        builder.append(template.substring(templateStart))
        if (i < args.size) {
            builder.append(" [")
            builder.append(args[i++])

            while (i < args.size) {
                builder.append(", ")
                builder.append(args[i++])
            }

            builder.append(']')
        }

        return builder.toString()
    }
}
