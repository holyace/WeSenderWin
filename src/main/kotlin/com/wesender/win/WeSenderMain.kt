package com.wesender.win

import com.wesender.socket.util.AdbHelper
import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Application.launch(WeSenderMain::class.java, *args)
}

class WeSenderMain: Application() {

    companion object {
        private const val TAG = "WeSenderMain"
    }

    override fun start(primaryStage: Stage?) {
        val pane = FXMLLoader.load<Pane>(Global.getLayout("layout/layout_main.fxml"))
        primaryStage?.let {
            it.title = "WeSender"
            it.icons.add(Image("drawable/we_sender_logo.png"))
            it.isResizable = false
            it.scene = Scene(pane)
            it.onCloseRequest = EventHandler<WindowEvent> {
                onAppExit()
            }
            it.show()
        }
    }

    private fun onAppExit() {
        AdbHelper.terminate()
        Controllers.clear()
        exitProcess(0)
    }
}