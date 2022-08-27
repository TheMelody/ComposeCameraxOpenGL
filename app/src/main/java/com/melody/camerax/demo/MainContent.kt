package com.melody.camerax.demo

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.camerax_compose.utils.openAppPermissionSettingPage
import com.melody.camerax_compose.utils.requestMultiplePermission

/**
 * MainContent
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 12:16
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BoxScope.MainContent(block: (Boolean) -> Unit) {
    val context = LocalContext.current
    val reqTakePhotoPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        onGrantAll = {
            block.invoke(true)
        },
        onNoGrantPermission = {
            // TODO: demo就不写那么复杂了，自行弹出Dialog，如果点击确定，跳转应用详情的权限授权页面，这里只是为了写demo
            openAppPermissionSettingPage(context.applicationContext)
        }
    )

    val reqCaptureVideo = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        onGrantAll = {
            block.invoke(false)
        },
        onNoGrantPermission = {
            // TODO: demo就不写那么复杂了，自行弹出Dialog，如果点击确定，跳转应用详情的权限授权页面，这里只是为了写demo
            openAppPermissionSettingPage(context.applicationContext)
        }
    )

    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FileSavePathTips()
        CameraPhoto {
            reqTakePhotoPermission.launchMultiplePermissionRequest()
        }
        CameraVideo {
            reqCaptureVideo.launchMultiplePermissionRequest()
        }
    }
}

@Composable
fun FileSavePathTips() {
    Text(
        text = "文件保存位置，SD卡根目录，自己到这个目录查看\n" +
                "视频：/Android/data/com.melody.camerax.demo/files/Movies\n" +
                "图片：/Android/data/com.melody.camerax.demo/files/Pictures",
        style = MaterialTheme.typography.body1.copy(fontSize = 11.sp, textAlign = TextAlign.Center)
    )
}

@Composable
fun CameraPhoto(onTakePhoto: () -> Unit) {
    Button(
        onClick = onTakePhoto
    ) {
        Text(text = "拍照")
    }
}

@Composable
fun CameraVideo(onCaptureVideo: () -> Unit) {
    Button(
        onClick = onCaptureVideo
    ) {
        Text(text = "拍视频")
    }
}
