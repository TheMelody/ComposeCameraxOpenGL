package com.melody.camerax_compose.ui.content

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melody.camerax_compose.contract.CameraContract
import com.melody.camerax_compose.optionsbar.CameraMenuBar
import com.melody.camerax_compose.viewmodel.CameraViewModel
import com.melody.camerax_compose.widget.ShowDeleteCapVideoFileDialog
import com.melody.camerax_compose.widget.ShowExitCapVideoFilePublishDialog
import com.melody.camerax_compose.widget.ShowRestartCapVideoRecordingDialog
import com.melody.camerax_compose.widget.ShowStopCapVideoRecordingDialog
import com.melody.opengl.camerax.utils.GLCameraxUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * CameraScreen
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 12:08
 */
@Composable
internal fun CameraScreen(
    isTakePhoto: Boolean,
    onOpenPreviewFile: (Uri) -> Unit,
    onNavigateUp: () -> Unit
) {
    val viewModel: CameraViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.init(isTakePhoto)
    }
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            when (it) {
                is CameraContract.Effect.Toast -> {
                    Toast.makeText(
                        GLCameraxUtils.getApplicationContext(),
                        it.msg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is CameraContract.Effect.RefreshOtherElementVisibility -> {
                    // TODO:如果你在其他地方塞了CameraScreen,拍视频的时候，肯定需要隐藏其他元素的可见性，参考：小红书，或者其他APP效果你懂的
                    // 这里就预留了,给读者留更多操作空间
                }
                is CameraContract.Effect.PreviewImgAndVideo -> {
                    onOpenPreviewFile.invoke(it.uri)
                }
            }
        }.collect()
    }
    if (isTakePhoto) {
        RenderTakePhoto(
            lensFacingProvider = {
                currentState.lensFacing
            },
            takePictureFileUri = currentState.takePictureFileUri,
            onTakePhotoClick = viewModel::takePhoto
        )
    } else {
        viewModel.CameraCaptureVideoDialog(
            showRemoveFileDialog = currentState::showRemoveFileDialog,
            showExitPublishDialog = currentState::showExitPublishDialog,
            showStopRecordingDialog = currentState::showStopRecordingDialog,
            showRestartRecordingDialog = currentState::showRestartRecordingDialog,
            onNavigateUp = onNavigateUp
        )
        RenderCaptureVideo(
            lensFacingProvider = {
                currentState.lensFacing
            },
            recordingStateProvider = {
                currentState.recordingState
            },
            videoRecordingFileUri = currentState.videoRecordingFileUri,
            onDeleteFileClick = {
                viewModel.showRemoveFileDialog(true)
            },
            onPublishVideoClick = viewModel::publishVideoFile,
            onTakeVideoClick = viewModel::takeVideo
        )
        BackHandler {
            viewModel.handleBackPress(onNavigateUp)
        }
    }
    CameraMenuBar(
        recordingState = currentState.recordingState,
        onReverseCameraClick = viewModel::switchLensFacing,
    )
}

@Composable
private fun CameraViewModel.CameraCaptureVideoDialog(
    showRemoveFileDialog: () -> Boolean,
    showExitPublishDialog: () -> Boolean,
    showStopRecordingDialog: () -> Boolean,
    showRestartRecordingDialog: () -> Boolean,
    onNavigateUp: () -> Unit
) {
    if (showRemoveFileDialog()) {
        ShowDeleteCapVideoFileDialog(
            onConfirm = {
                removeVideoFile()
            },
            onDismiss = {
                showRemoveFileDialog(false)
            }
        )
    }

    if (showExitPublishDialog()) {
        ShowExitCapVideoFilePublishDialog(
            onDismiss = {
                showExitPublishDialog(false)
            },
            onConfirm = {
                showExitPublishDialog(false)
                onNavigateUp.invoke()
            }
        )
    }

    if (showStopRecordingDialog()) {
        ShowStopCapVideoRecordingDialog(
            onDismiss = {
                handleStopRecordingDialog(false)
            },
            onConfirm = {
                handleStopRecordingDialog(true)
            }
        )
    }

    if (showRestartRecordingDialog()) {
        ShowRestartCapVideoRecordingDialog(
            onDismiss = {
                handleRestartRecordingDialog(false)
            },
            onConfirm = {
                handleRestartRecordingDialog(true)
            }
        )
    }
}
