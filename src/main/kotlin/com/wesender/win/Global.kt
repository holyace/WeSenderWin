package com.wesender.win

import java.io.File
import java.net.URL

object Global {

    private val root = "file:/${System.getProperty("user.dir")}"
    private val layout = root + File.separator + "layout"
    const val localPort = 8025
    const val phonePort = 8999

    private val classLoader = Thread.currentThread().contextClassLoader

    fun getRoot(): String = root

    fun getLayout(name: String): URL? = classLoader.getResource(name)
}