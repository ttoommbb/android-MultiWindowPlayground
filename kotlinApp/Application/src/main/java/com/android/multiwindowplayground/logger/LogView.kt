/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.multiwindowplayground.logger

import android.app.Activity
import android.content.Context

/**
 * Simple TextView which is used to output log data received through the LogNode interface.
 */
class LogView(context: Context) : androidx.appcompat.widget.AppCompatTextView(context), LogNode {

    // The next LogNode in the chain.
    private var next: LogNode? = null

    /**
     * Formats the log data and prints it out to the LogView.
     *
     * @param priority Log level of the data being logged.  Verbose, Error, etc.
     * @param tag      Tag for for the log data.  Can be used to organize log statements.
     * @param msg      The actual message to be logged. The actual message to be logged.
     * @param tr       If an exception was thrown, this can be sent along for the logging
     *                 facilities to extract and print useful information.
     */
    override fun println(priority: Int, tag: String?, msg: String, tr: Throwable?) {

        // For the purposes of this View, we want to print the priority as readable text.
        val priorityStr = when (priority) {
            android.util.Log.VERBOSE -> "VERBOSE"
            android.util.Log.DEBUG -> "DEBUG"
            android.util.Log.INFO -> "INFO"
            android.util.Log.WARN -> "WARN"
            android.util.Log.ERROR -> "ERROR"
            android.util.Log.ASSERT -> "ASSERT"
            else -> "VERBOSE"
        }

        // Handily, the Log class has a facility for converting a stack trace into a usable string.
        var exceptionStr: String? = null
        if (tr != null) exceptionStr = android.util.Log.getStackTraceString(tr)

        // Take the priority, tag, message, and exception, and concatenate as necessary
        // into one usable line of text.
        val delimiter = "\t"
        val outputBuilder = StringBuilder().also {
            appendIfNotNullOrEmpty(it, priorityStr, delimiter)
            appendIfNotNullOrEmpty(it, tag, delimiter)
            appendIfNotNullOrEmpty(it, msg, delimiter)
            appendIfNotNullOrEmpty(it, exceptionStr, delimiter)
        }

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        (context as Activity).runOnUiThread(Thread(Runnable {
            // Display the text we just generated within the LogView as a new line of log data.
            append("\n$outputBuilder")
        }))

        next?.println(priority, tag, msg, tr)
    }

    /**
     * Takes a string and adds to it, with a separator, if the bit to be added isn't null. Since
     * the logger takes so many arguments that might be null, this method helps cut out some of the
     * agonizing tedium of writing the same statement over and over.
     *
     * @param source    StringBuilder containing the text to append to.
     * @param addStr    The String to append
     * @param delimiter The String to separate the source and appended strings.
     */
    private fun appendIfNotNullOrEmpty(source: StringBuilder, addStr: String?, delimiter: String) {
        if (addStr.isNullOrEmpty()) return
        source.append(addStr).append(delimiter)
    }

}
