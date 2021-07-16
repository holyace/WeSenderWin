package com.wesender.common.dto

class Message(val type: Int = MsgType.TYPE_UNKNOWN,
              val header: String = "",
              val body: ByteArray? = null)