package com.melody.camerax_compose.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.*

/**
 * 权限统一分发
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 12:06
 */
@ExperimentalPermissionsApi
@Composable
fun requestMultiplePermission(
    permissions: List<String>,
    onGrantAll: () -> Unit,
    onNoGrantPermission: () -> Unit
): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { mapInfo ->
            val noGrantPermissionMap = mapInfo.filter { !it.value }
            if (noGrantPermissionMap.isNotEmpty()) {
                // 用户没有同意某个权限
                onNoGrantPermission()
            } else {
                onGrantAll()
            }
        }
    )
}

/**
 * 打开App权限设置页面
 */
fun openAppPermissionSettingPage(context: Context) {
    val packageName = context.applicationContext.packageName
    try {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.applicationContext.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            // 往设置页面跳
            context.applicationContext.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (ignore: ActivityNotFoundException) {
            // 市面上有些手机跳系统设置也崩溃
        }
    }
}