package com.wesender.common.dto

import com.google.gson.Gson
import java.nio.charset.Charset

class MessageUtil {

    companion object {

        fun toJson(message: Message): String {
            return Gson().toJson(message)
        }

        fun fromJson(json: String?): Message? {
            json?: return null
            return Gson().fromJson(json, Message::class.java)
        }

        fun wrapJson(json: String): Message {
            return Message(MsgType.TYPE_JSON, "", json.toByteArray(Charset.forName("utf-8")))
        }
    }
}