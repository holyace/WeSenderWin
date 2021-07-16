package com.wesender.win

import javafx.application.Application
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(WeSenderMain::class.java, *args)
}

class WeSenderMain: Application() {
    override fun start(primaryStage: Stage?) {
        primaryStage?.show()
    }
}