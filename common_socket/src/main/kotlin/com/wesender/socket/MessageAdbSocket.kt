package com.wesender.socket

import com.wesender.common.dto.Message
import com.wesender.common.dto.MessageUtil
import com.wesender.common.log.Logger
import java.nio.charset.Charset

class MessageAdbSocket(pcLocalPort: Int, phonePort: Int) : AdbSocket(pcLocalPort, phonePort) {

    companion object {
        const val TAG = "MessageAdbSocket"
        const val LINE_END = "\n"
    }

    fun sendMessage(msg: Message): Boolean {
        val json = MessageUtil.toJson(msg)
        return try {
            send((json + LINE_END).toByteArray(Charset.forName("utf-8")))
            true
        }
        catch (e: Exception) {
            Logger.e(Const.MODULE, TAG, e, "sendMessage error")
            false
        }
    }

    fun readMessage(): Message? {
        return MessageUtil.fromJson(readLine())
    }

    fun sendJson(json: String?): Boolean {
        json?: return false
        return sendMessage(MessageUtil.wrapJson(json))
    }
}