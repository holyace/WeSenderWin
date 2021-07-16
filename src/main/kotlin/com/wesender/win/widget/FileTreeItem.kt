package com.wesender.win.widget

import com.wesender.win.FileUtil
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import java.io.File
import javax.swing.filechooser.FileSystemView

class FileTreeItem(val file: File = SYSTEM_ROOT,
                   private val directoryTraver: (FileTreeItem, File) -> Array<File>? = DEF_TRAVER)
    : TreeItem<String>(FileUtil.getFileSystemName(file),
        FileUtil.getFileSystemIcon(file)) {

    companion object {
        private val SYSTEM_ROOT = FileSystemView.getFileSystemView().roots[0]

        private val DEF_TRAVER: (FileTreeItem, File) -> Array<File>? = func@ { item, file ->
            if ((item.parent as FileTreeItem).file == SYSTEM_ROOT) {
                val name = FileUtil.getFileSystemName(file)
                if (name == "网络" || name == "家庭组") {
                    return@func arrayOf<File>()
                }
            }
            return@func file.listFiles()
        }
    }

    private var mListChildren = false

    override fun getChildren(): ObservableList<TreeItem<String>> {
        val children = super.getChildren()
        if (!mListChildren && isExpanded) {
            mListChildren = true
            directoryTraver.invoke(this, file)?.forEach {
//                if (it.isDirectory) {
//                    children?.add(FileTreeItem(it))
//                }
                children?.add(FileTreeItem(it))
            }
        }
        return children
    }

    override fun isLeaf(): Boolean {
        return !file.isDirectory
    }
}

