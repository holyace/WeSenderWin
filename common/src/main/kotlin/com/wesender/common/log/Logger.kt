package com.wesender.common.log

import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    private var sEnable = true
    private var sDateFormat: SimpleDateFormat? = null

    fun d(module: String, tag: String, format: String, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.DEBUG, module, tag, String.format(format, *args)))
        }
    }

    fun i(module: String, tag: String, format: String, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.INFO, module, tag, String.format(format, *args)))
        }
    }

    fun w(module: String, tag: String, format: String, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.WARN, module, tag, String.format(format, *args)))
        }
    }

    fun e(module: String, tag: String, format: String, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.ERROR, module, tag, String.format(format, *args)))
        }
    }

    fun e(module: String, tag: String, e: Throwable, format: String, vararg args: Any?) {
        if (sEnable) {
            log(getLog(Level.ERROR, module, tag, "%s, stack:\n%s".format(String.format(format, args), getStackTrace(e))))
        }
    }

    private fun getLog(level: Level, module: String, tag: String, log: String): String {
        return when (level) {
            Level.INFO, Level.DEBUG -> "\u001B[m%s %s/%s.%s: %s\u001B[0m".format(time, level.tag, module, tag, log)
            else -> "\u001B[%sm%s %s/%s.%s: %s\u001B[0m".format(level.color, time, level.tag, module, tag, log)
        }
    }

    private fun getStackTrace(e: Throwable): String {
        val pw = PrintWriter(StringWriter())
        pw.use {
            e.printStackTrace(it)
            it.flush()
        }
        pw.flush()
        return pw.toString()
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