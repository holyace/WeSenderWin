package com.wesender.socket

import com.android.ddmlib.IDevice
import com.wesender.common.log.Logger
import com.wesender.common.transfer.ChannelReader
import com.wesender.common.transfer.ChannelWriter
import com.wesender.common.transfer.SocketDTO
import com.wesender.socket.constants.Const
import com.wesender.socket.handler.SelectorEventHandler
import com.wesender.socket.util.AdbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

open class AdbDevice(private val pcLocalPort: Int,
                     private val phonePort: Int)
    : SelectorEventHandler {

    companion object {
        private const val TAG = "AdbSocket"
    }

    private var mSocketChannel: SocketChannel? = null

    private var mThread: SocketThread? = null

    private val mWriter = ChannelWriter()
    private val mReader = ChannelReader()

    private var mDevice: IDevice? = null

    private var mWaiting = false
    private var mConnected = false
    private var mReadable = false
    private var mWriteable = false

    fun connectDevice(deviceIndex: Int = 0): Boolean {

        if (mWaiting || checkState()) return true

        val devices = AdbHelper.getDevices()
        if (devices.isNullOrEmpty()) {
            Logger.e(Const.MODULE, TAG, "no devices connect to this pc")
            return false
        }

        if (deviceIndex < 0 || deviceIndex > devices.size) {
            Logger.e(Const.MODULE, TAG, "illegal device index: %d, device count: %d", deviceIndex, devices.size)
            return false
        }

        mDevice = devices[deviceIndex]

        mDevice?.createForward(pcLocalPort, phonePort)

        if (mThread == null) {
            mThread = SocketThread("socket-listen-thread", this)
            mThread?.start()
        }

        mSocketChannel = SocketChannel.open()
        mSocketChannel?.configureBlocking(false)

        mWaiting = true
        mThread?.listen(mSocketChannel!!, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                mSocketChannel?.connect(InetSocketAddress("localhost", pcLocalPort))
            } catch (e: Exception) {
                Logger.w(Const.MODULE, TAG, "connect devices error: %s", e.message)
            }
        }

        return true
    }

    @Throws(IOException::class)
    fun send(data: SocketDTO) {
        if (!checkState()) {
            Logger.e(Const.MODULE, TAG, "socket error, ignore data write")
            return
        }
        mWriter.write(mSocketChannel!!, data)
    }

    @Throws(IOException::class)
    fun read(): SocketDTO? {
        if (!checkState()) {
            Logger.e(Const.MODULE, TAG, "socket error, ignore read data")
            return null
        }
        return mReader.read(mSocketChannel!!)
    }

    fun disconnect() {
//        if (!checkState()) {
//            return
//        }
        try {
            mDevice?.removeForward(pcLocalPort, phonePort)
        } catch (e: Exception) {
            Logger.w(Const.MODULE, TAG, "removeForward error: %s", e.message)
        }
        mSocketChannel?.close()
        mThread?.stopListen()
    }

    private fun checkState(): Boolean {
        return mSocketChannel != null && mConnected
    }

    override fun onConnect(selectionKey: SelectionKey) {
        super.onConnect(selectionKey)
        mWaiting = false
        val channel = selectionKey.channel() as SocketChannel
        mConnected = try {
            channel.finishConnect()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        Logger.i(Const.MODULE, TAG, "onConnect $mConnected")
    }

    override fun onReadable(selectionKey: SelectionKey) {
        super.onReadable(selectionKey)
        mReadable = true
    }

    override fun onWriteable(selectionKey: SelectionKey) {
        super.onWriteable(selectionKey)
        mWriteable = true
    }

    fun isDeviceReady(): Boolean = checkState()
}