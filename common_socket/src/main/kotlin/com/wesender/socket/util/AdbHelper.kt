package com.wesender.socket.util

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.wesender.common.log.Logger
import com.wesender.socket.Const
import java.io.File
import java.lang.IllegalStateException

object AdbHelper {

    private const val TAG = "AdbHelper"

    private var mAdb: AndroidDebugBridge? = null

    init {
        initAdb()
    }

    private fun initAdb() {
        if (mAdb != null) return
        val adbLocation = System.getenv("ADB_HOME") + File.separator + "platform-tools" + File.separator + "adb.exe"
        AndroidDebugBridge.init(false)
        mAdb = AndroidDebugBridge.createBridge(adbLocation, false)
        if (mAdb == null) {
            throw IllegalStateException("create adb fail, please check you adb path")
        }

        var count = 0
        while (count < 100 && !mAdb!!.hasInitialDeviceList()) {
            count++
            try {
                Logger.i(Const.MODULE, TAG, "wait for adb connect...")
                Thread.sleep(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!mAdb!!.hasInitialDeviceList()) {
            terminate()
            throw IllegalStateException("no adb connect, please check you devices")
        }
    }

    fun getAdb(): AndroidDebugBridge? {
        initAdb()
        return mAdb
    }

    fun getDevices(): Array<IDevice>? {
        initAdb()
        return mAdb?.devices
    }

    fun terminate() {
        AndroidDebugBridge.terminate()
    }
}