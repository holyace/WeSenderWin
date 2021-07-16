package com.wesender.socket

import com.wesender.common.log.Logger
import com.wesender.socket.util.AdbHelper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class AdbSocket(private val pcLocalPort: Int, private val phonePort: Int) {

    companion object {
        private const val TAG = "AdbSocket"
    }

    private var mSocket: Socket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    fun connectPhone(deviceIndex: Int = 0): Boolean {

        if (mSocket != null && mSocket!!.isConnected) {
            return true
        }

        val devices = AdbHelper.getDevices()
        if (devices.isNullOrEmpty()) {
            Logger.e(TAG, "no devices connect to this pc")
            return false
        }

        if (deviceIndex < 0 || deviceIndex > devices.size) {
            Logger.e(TAG, "illegal device index: %d, device count: %d", deviceIndex, devices.size)
            return false
        }

        devices[deviceIndex].createForward(pcLocalPort, phonePort)

        mSocket = Socket("localhost", pcLocalPort)

        mSocket!!.tcpNoDelay = true

        mInputStream = mSocket!!.getInputStream()

        mOutputStream = mSocket!!.getOutputStream()

        return true
    }

    @Throws(IOException::class)
    fun send(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (!checkState()) {
            Logger.e(TAG, "socket error, ignore data write")
            return
        }
        mOutputStream?.let {
            it.write(data, offset, length)
            it.flush()
        }
    }

    @Throws(IOException::class)
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (!checkState()) {
            Logger.e(TAG, "socket error, ignore read data")
            return
        }
        mInputStream!!.read(data, offset, length)
    }

    fun disconnect() {
        if (!checkState()) {
            return
        }
        mSocket!!.shutdownInput()
        mSocket!!.shutdownOutput()
        mInputStream = null
        mOutputStream = null
        mSocket = null
    }

    private fun checkState(): Boolean {
        return mSocket?.isConnected == true && mInputStream != null && mOutputStream != null
    }
}