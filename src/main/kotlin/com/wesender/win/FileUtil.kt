package com.wesender.win

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelFormat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView

class FileUtil {

    companion object {

        fun getFileSystemIcon(file: File): Canvas {
            val imageIcon = FileSystemView.getFileSystemView().getSystemIcon(file) as ImageIcon
            val image = imageIcon.image

            val bi = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.BITMASK)

            bi.graphics.drawImage(image, 0, 0, null)

            val data = (bi.data.dataBuffer as DataBufferInt).data

            val pixelFormat = PixelFormat.getIntArgbInstance()

            val canvas = Canvas(bi.width + 2.0, bi.height + 2.0)

            val pixelWriter = canvas.graphicsContext2D.pixelWriter

            pixelWriter.setPixels(1, 1, bi.width, bi.height, pixelFormat, data, 0, bi.width)

            return canvas
        }

        fun getFileSystemName(file: File): String {
            return FileSystemView.getFileSystemView().getSystemDisplayName(file)
        }
    }
}