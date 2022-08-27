package com.melody.camerax_compose.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import java.io.*

/**
 * 根据uri删除文件
 */
fun Context.deleteFileUri(uri: Uri): Result<Any> {
    return if(uri.scheme?.startsWith("file") == true) {
        kotlin.runCatching {
            uri.toFile().delete()
        }
    } else {
        kotlin.runCatching {
            contentResolver.delete(uri, null, null)
        }
    }
}

/**
 * 删除目录文件
 */
fun deleteFileDir(dirFile: File){
    if (dirFile.isDirectory) {
        val children = dirFile.list()
        if (children != null) {
            for (i in children.indices) {
                runCatching {
                    File(dirFile, children[i]).delete()
                }
            }
        }
    }
}
