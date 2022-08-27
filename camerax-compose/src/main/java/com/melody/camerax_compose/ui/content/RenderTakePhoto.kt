package com.melody.camerax_compose.ui.content

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.melody.camerax_compose.optionsbar.TakePhotoOptionsBar
import com.melody.ui.camerax.CameraXView

/**
 * 拍照片
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 14:13
 */
@Composable
fun RenderTakePhoto(
    takePictureFileUri: Uri?,
    lensFacingProvider: () -> Int,
    onTakePhotoClick: () -> Unit,
) {
    val currentOnTakePhotoClick by rememberUpdatedState(newValue = onTakePhotoClick)
    CameraXView(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        lensFacing = lensFacingProvider()
    ) { glCameraPreview ->
        TakePhotoOptionsBar(
            onTakePhoto = {
                glCameraPreview.takePicture(takePictureFileUri?.path ?: "")
                currentOnTakePhotoClick.invoke()
            }
        )
    }
}