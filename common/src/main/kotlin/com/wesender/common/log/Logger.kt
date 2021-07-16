package com.wesender.common.log

import java.text.SimpleDateFormat
import java.util.*

object Logger {

    private var sEnable = true
    private var sDateFormat: SimpleDateFormat? = null

    fun d(tag: String, format: String?, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.DEBUG, tag, String.format(format!!, *args)))
        }
    }

    fun i(tag: String, format: String?, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.INFO, tag, String.format(format!!, *args)))
        }
    }

    fun w(tag: String, format: String?, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.WARN, tag, String.format(format!!, *args)))
        }
    }

    fun e(tag: String, format: String?, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.ERROR, tag, String.format(format!!, *args)))
        }
    }

    private fun getLog(level: Level, tag: String, log: String): String {
        return "\u001B[%sm%s %s/%s: %s\u001B[0m".format(level.color, time, level.tag, tag, log)
    }

    private val time: String
        private get() {
            if (sDateFormat == null) {
                sDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS")
            }
            return sDateFormat!!.format(Date())
        }

    private fun log(log: String) {
        println(log)
    }
}

internal enum class Level(val tag: String, val color: String) {
    DEBUG("D", "30"),
    INFO("I", "30"),
    WARN("W", "33"),
    ERROR("E", "31");
}