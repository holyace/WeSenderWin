package com.wesender.win.controller

import com.wesender.common.log.Logger
import com.wesender.socket.AdbDataTransfer
import com.wesender.socket.constants.Const
import com.wesender.socket.util.AdbHelper
import com.wesender.win.Controllers
import com.wesender.win.Global
import com.wesender.win.widget.FileTreeItem
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventType
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.util.*

class MainController: BaseController() {

    companion object {
        private const val TAG = "MainController"

        fun copyToClipboard(text: String) {
            Clipboard.getSystemClipboard().setContent(
                ClipboardContent().apply {
                    putString(text)
                }
            )
        }
    }

    lateinit var btnRefresh: Button
    lateinit var labelStatus: Label
    lateinit var devicesList: ChoiceBox<String>
    lateinit var fileTreeView: TreeView<String>
    lateinit var message: TextArea
    lateinit var btnSend: Button
    lateinit var mAdbDevice: AdbDataTransfer
    private var selectedDeviceIndex = -1
    private var selectedFile: File? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        init()
        Controllers.put(this)
    }

    private fun init() {
        val cortexMenu = ContextMenu().apply {
            val selectAll = MenuItem("全选").apply {
                addEventHandler(EventType.ROOT) {
                    message.selectAll()
                }
            }
            items.add(selectAll)

            val copy = MenuItem("复制").apply {
                addEventHandler(EventType.ROOT) {
                    val text = message.selectedText
                    if (text.isNullOrBlank()) {
                        return@addEventHandler
                    }
                    copyToClipboard(text)
                }
            }
            items.add(copy)

            val item = MenuItem("清除").apply {
                addEventHandler(EventType.ROOT) {
                    message.text = ""
                }
            }
            items.add(item)
        }
        message.contextMenu = cortexMenu

        labelStatus.text = "开始检测设备..."
        devicesList.selectionModel.selectedItemProperty().addListener lis@ { _, _, newValue ->
            Logger.i(Const.MODULE, TAG, "select device: %s", newValue)
            selectedDeviceIndex = devicesList.items.indexOf(newValue)
            if (selectedDeviceIndex < 0 || selectedDeviceIndex > devicesList.items.size) {
                showMsg("选择设备无效，请重新选择...")
                return@lis
            }
            val msg = "选择设备[${devicesList.value}]"
            showMsg(msg)
        }
        fileTreeView.apply {
            selectionModel.selectedItemProperty().addListener lis@{ _, _, newValue ->
                if (newValue !is FileTreeItem) {
                    return@lis
                }
                selectedFile = newValue.file
                Logger.i(Const.MODULE, TAG, "select item: %s", newValue.file)
                btnSend.isDisable = false
                showMsg("已选择文件: %s".format(newValue.value))
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
        if (!mAdbDevice.isDeviceReady()) {
            showMsg("等待设备响应...", true)
        }
        else {
            showMsg("设备已就绪", true)
        }
    }

    fun clickSend(mouseEvent: MouseEvent) {
        Logger.i(Const.MODULE, TAG, "clickSend")
        GlobalScope.launch(Dispatchers.IO) block@ {
            try {
                if (selectedFile == null || !selectedFile!!.exists()) {
                    showMsg("请先选择要发送的文件")
                    return@block
                }
                val file = selectedFile!!
                if (file.isDirectory) {
                    showMsg("暂不支持发送目录")
                    return@block
                }
                if (!mAdbDevice.isDeviceReady()) {
                    val ret = mAdbDevice.connectDevice(selectedDeviceIndex)
                    val msg = "连接设备[${devicesList.value}]" + if (ret) "成功" else "失败"
                    showMsg(msg, true)
                    if (!ret) {
                        return@block
                    }
                }
                if (!mAdbDevice.isDeviceReady()) {
                    showMsg("等待设备响应...", true)
                    return@block
                }
                showMsg("开始发送...")
                mAdbDevice.sendFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadDevices() {
        GlobalScope.launch {
            val devices = AdbHelper.getDevices()
            if (devices.isNullOrEmpty()) {
                showMsg("未检测到设备", true)
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
        mAdbDevice = AdbDataTransfer(Global.localPort, Global.phonePort)

        showMsg("等待设备响应...", true)

    }

    private fun showMsg(msg: String, status: Boolean = false) {
        Platform.runLater {
            if (status) labelStatus.text = msg
            message.appendText("\n" + msg)
        }
    }

    override fun onExit() {
        super.onExit()
        Logger.i(Const.MODULE, TAG, "onExit")
        mAdbDevice?.disconnect()
    }

}