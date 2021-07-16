package com.wesender.win.util

import javafx.application.Platform

class SystemUtil {

    companion object {

        fun runOnUi(block: () -> Unit) {
            Platform.runLater(block)
        }
    }
}