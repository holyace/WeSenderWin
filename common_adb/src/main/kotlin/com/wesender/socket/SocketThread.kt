package com.wesender.socket

import com.wesender.common.log.Logger
import com.wesender.common.transfer.ext.hexHash
import com.wesender.socket.constants.Const
import com.wesender.socket.handler.SelectorEventHandler
import java.lang.Exception
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class SocketThread(name: String,
                   private val eventHandler: SelectorEventHandler
): Thread(name) {

    companion object {
        private const val TAG = "SocketThread"
    }

    private val mSelector: Selector = Selector.open()

    @Volatile
    private var mListening = true

    fun listen(channel: SelectableChannel, op: Int) {
        if (channel.isRegistered) return
        channel.configureBlocking(false)
        channel.register(mSelector, op)
        Logger.i(Const.MODULE, TAG, "listen channel: ${channel.hexHash()}, op: $op")
        mSelector.wakeup()
    }

    fun stopListen() {
        mListening = false
        mSelector.close()
    }

    override fun run() {
        super.run()

        Logger.i(Const.MODULE, TAG, "start listen in loop")

        while (mListening) {

            val readyCount = mSelector.select()
            if (readyCount <= 0) {
//                Logger.i(Const.MODULE, TAG, "readyCount = 0")
                continue
            }

            mSelector.selectedKeys().forEach {
                when {
                    it.isAcceptable -> onAcceptable(it)

                    it.isConnectable -> onConnectable(it)

                    it.isReadable -> onReadable(it)

                    it.isWritable -> onWriteable(it)
                }
            }
        }

        Logger.i(Const.MODULE, TAG, "thread $name stop")
    }

    private fun onAcceptable(selectionKey: SelectionKey) {
        try {
            eventHandler.onAccept(selectionKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onConnectable(selectionKey: SelectionKey) {
        try {
            eventHandler.onConnect(selectionKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onReadable(selectionKey: SelectionKey) {
        try {
            eventHandler.onReadable(selectionKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onWriteable(selectionKey: SelectionKey) {
        try {
            eventHandler.onWriteable(selectionKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}