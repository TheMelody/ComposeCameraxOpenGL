package com.melody.camerax_compose.ui.content

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.melody.camerax_compose.model.VideoRecordState
import com.melody.camerax_compose.optionsbar.TakeVideoOptionsBar
import com.melody.ui.camerax.CameraXView

/**
 * 拍视频页面
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 14:13
 */
@Composable
fun RenderCaptureVideo(
    videoRecordingFileUri: Uri?,
    lensFacingProvider: () -> Int,
    recordingStateProvider: () -> VideoRecordState?,
    onTakeVideoClick: (Boolean) -> Unit,
    onPublishVideoClick: () -> Unit,
    onDeleteFileClick: () -> Unit
) {
    CameraXView(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        lensFacing = lensFacingProvider()
    ) { glCameraPreview ->

        LaunchedEffect(recordingStateProvider()) {
            if (null != recordingStateProvider()) {
                // 拍视频
                glCameraPreview.takeVideo(videoRecordingFileUri?.path ?: "")
            }
            // 为null的状态，才可以滑动切换滤镜
            glCameraPreview.slideGpuFilterGroupEnable = (recordingStateProvider() == null)
        }

        val recordingTimeState = glCameraPreview.getRecordingTimeState().collectAsState()

        TakeVideoOptionsBar(
            recordingState = recordingStateProvider(),
            recordingTime = recordingTimeState.value,
            onTakeVideo = {
                // 回调当前状态，再根据viewModel返回的state去做ui数据展示
                onTakeVideoClick.invoke(glCameraPreview.isRecording())
            },
            onPublishVideo = onPublishVideoClick,
            onDeleteFile = onDeleteFileClick
        )
    }
}