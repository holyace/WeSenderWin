package com.wesender.socket

import com.wesender.common.log.Logger
import com.wesender.common.transfer.SocketDTO
import com.wesender.socket.constants.Const
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class AdbDataTransfer(localPort: Int, phonePort: Int): AdbDevice(localPort, phonePort) {

    fun sendFile(file: File) {
        if (!file.exists() || file.isDirectory) return

        val chanel = RandomAccessFile(file, "r").channel
        val buffer = ByteBuffer.allocate(SocketDTO.BUFFER_SIZE)

        val byteArray = ByteArray(SocketDTO.BUFFER_SIZE)
        val read = chanel.read(buffer)

        buffer.flip()
        buffer.get(byteArray, 0, read)

        val dto = SocketDTO.of(byteArray, 0, read)

        try {
            send(dto)
        } catch (e: Exception) {
            Logger.w(Const.MODULE, TAG, "send file error, file: %s, error: %s", file, e.message)
        }
    }

    companion object {
        private const val TAG = "AdbDataTransfer"
    }
}