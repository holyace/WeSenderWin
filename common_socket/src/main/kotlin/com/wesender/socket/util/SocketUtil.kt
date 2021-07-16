package com.wesender.util

import com.wesender.common.log.Logger

class SocketUtil{

    companion object {

        private const val TAG = "SocketUtil"

        fun startAdbSocketService(hostPort: Int, serverPort: Int) {
            CmdUtil.exec("adb -s ${getAdbDeviceId()} forward tcp:$hostPort tcp:$serverPort")
        }

        private fun getAdbDeviceId(): String? {
            val ret = CmdUtil.exec("adb devices")
            if (ret.first != 0) {
                Logger.e(TAG, "getAdbDeviceId exec cmd error: %s", ret.second)
                return null
            }
            return parseFirstDevices(ret.second)
        }

        private fun parseFirstDevices(ret: String): String? {
            return ret.split("\n").let {
                return@let if (it.size > 1) {
                    val dev = it[1].split("\t")
                    return if (dev.isNotEmpty()) dev[0] else null
                } else {
                    null
                }
            }
        }
    }
}