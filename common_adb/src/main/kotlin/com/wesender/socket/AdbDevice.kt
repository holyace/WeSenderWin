package com.wesender.socket

import com.wesender.common.log.Logger
import com.wesender.common.transfer.ChannelReader
import com.wesender.common.transfer.ChannelWriter
import com.wesender.common.transfer.SocketDTO
import com.wesender.socket.util.AdbHelper
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
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

    private var mConnected = false
    private var mReadable = false
    private var mWriteable = false

    fun connectDevice(deviceIndex: Int = 0): Boolean {

        if (mSocketChannel != null && mSocketChannel!!.isConnected) {
            return true
        }

        val devices = AdbHelper.getDevices()
        if (devices.isNullOrEmpty()) {
            Logger.e(Const.MODULE, TAG, "no devices connect to this pc")
            return false
        }

        if (deviceIndex < 0 || deviceIndex > devices.size) {
            Logger.e(Const.MODULE, TAG, "illegal device index: %d, device count: %d", deviceIndex, devices.size)
            return false
        }

        devices[deviceIndex].createForward(pcLocalPort, phonePort)

        if (mThread == null) {
            mThread = SocketThread("socket-listen-thread", this)
            mThread?.start()
        }

        mSocketChannel = SocketChannel.open()
        mSocketChannel?.bind(InetSocketAddress("localhost", pcLocalPort))

        mThread?.listen(mSocketChannel!!, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)

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
        if (!checkState()) {
            return
        }
        mThread?.stopListen()
    }

    private fun checkState(): Boolean {
        return mSocketChannel != null && mConnected && mReadable && mWriteable
    }

    override fun onConnect(selectionKey: SelectionKey) {
        super.onConnect(selectionKey)
        val channel = selectionKey.channel() as SocketChannel
        channel.finishConnect()
        mConnected = true
    }

    override fun onReadable(selectionKey: SelectionKey) {
        super.onReadable(selectionKey)
        mReadable = true
    }

    override fun onWriteable(selectionKey: SelectionKey) {
        super.onWriteable(selectionKey)
        mWriteable = true
    }
}