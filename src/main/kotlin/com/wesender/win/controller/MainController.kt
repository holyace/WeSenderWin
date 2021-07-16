package com.wesender.win.controller

import com.wesender.common.log.Logger
import com.wesender.socket.Const
import com.wesender.socket.util.AdbHelper
import com.wesender.win.Controllers
import com.wesender.win.widget.FileTreeItem
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.util.*

class MainController: Initializable {

    companion object {
        private const val TAG = "MainController"
    }

    lateinit var btnRefresh: Button
    lateinit var labelStatus: Label
    lateinit var devicesList: ChoiceBox<String>
    lateinit var fileTreeView: TreeView<String>
    lateinit var message: TextArea
    lateinit var btnSend: Button

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        init()
        Controllers.put(this)
    }

    private fun init() {
        labelStatus.text = "开始检测设备..."
        devicesList.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            Logger.i(Const.MODULE, TAG, "select device: %s", newValue)
        }
        fileTreeView.apply {
            selectionModel.selectedItemProperty().addListener lis@{ _, _, newValue ->
                if (newValue !is FileTreeItem) {
                    return@lis
                }
                Logger.i(Const.MODULE, TAG, "select item: %s", newValue.file)
                btnSend.isDisable = false
                message.appendText("\n已选择文件: %s".format(newValue.value))
            }
            root = FileTreeItem(directoryTraver = { _, file: File ->
                file.listFiles()
            })
            isShowRoot = false
        }
        loadDevices()
    }

    fun clickRefresh(mouseEvent: MouseEvent) {
        Logger.i(Const.MODULE, TAG, "clickRefresh")
    }

    private fun loadDevices() {
        GlobalScope.launch {
            val devices = AdbHelper.getDevices()
            if (devices.isNullOrEmpty()) {
                val msg = "未检测到设备"
                labelStatus.text = msg
                message.appendText("\n" + msg)
            }
            val items = mutableListOf<String>()
            devices!!.forEach {
                var name = it.serialNumber
                if (it.isOffline) {
                    name += "[offline]"
                }
                items.add(name)
            }
            Platform.runLater {
                devicesList.items = FXCollections.observableArrayList(items)
                devicesList.value = items[0]
                devicesList.isDisable = false
            }
        }
    }

    fun onExit() {}
}