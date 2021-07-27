package com.wesender.socket.handler

import java.nio.channels.SelectionKey

interface SelectorEventHandler {

    fun onAccept(selectionKey: SelectionKey) {}

    fun onConnect(selectionKey: SelectionKey) {}

    fun onReadable(selectionKey: SelectionKey) {}

    fun onWriteable(selectionKey: SelectionKey) {}
}