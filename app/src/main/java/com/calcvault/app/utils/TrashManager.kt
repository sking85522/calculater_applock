package com.calcvault.app.utils

import android.content.Context
import java.io.File

object TrashManager {
    fun moveToTrash(context: Context, file: File) {
        val trashDir = File(context.filesDir, "trash_bin")
        if (!trashDir.exists()) trashDir.mkdirs()

        // Include the original parent directory name to restore it properly later
        val originalParent = file.parentFile?.name ?: "unknown"
        val trashedFile = File(trashDir, "${originalParent}__${file.name}")
        file.renameTo(trashedFile)
    }

    fun restoreFromTrash(context: Context, trashedFile: File) {
        val parts = trashedFile.name.split("__", limit = 2)
        if (parts.size == 2) {
            val originalDir = File(context.filesDir, parts[0])
            if (!originalDir.exists()) originalDir.mkdirs()

            val restoredFile = File(originalDir, parts[1])
            trashedFile.renameTo(restoredFile)
        }
    }

    fun permanentlyDelete(trashedFile: File) {
        if (trashedFile.exists()) {
            trashedFile.delete()
        }
    }

    fun getTrashFiles(context: Context): List<File> {
        val trashDir = File(context.filesDir, "trash_bin")
        return if (trashDir.exists()) {
            trashDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
}
