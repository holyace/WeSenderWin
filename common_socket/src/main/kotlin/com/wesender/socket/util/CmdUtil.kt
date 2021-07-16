package com.wesender.util

import com.wesender.common.log.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class CmdUtil {

    companion object {

        private const val TAG = "CmdUtil"

        fun exec(cmd: String): Pair<Int, String> {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(cmd)

            val inputStream = process.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("gbk")))
            var line: String? = null

            val result = StringBuffer()

            while ((bufferedReader.readLine()?.apply { line = this }) != null) {
                result.append(line).append("\n")
            }

            val retCode = process.waitFor()

            Logger.d(TAG, "exec cmd: %s, ret: %d, detail: \n%s", cmd, retCode, result)

            return Pair(retCode, result.toString())
        }

    }
}