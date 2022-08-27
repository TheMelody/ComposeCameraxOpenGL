package com.melody.camerax_compose.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.melody.camerax_compose.R

/**
 * 相机模块，页面弹出的一些警告弹框合集
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/08/27 14:33
 */
/**
 * 确认删除当前视频吗?
 */
@Composable
internal fun ShowDeleteCapVideoFileDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    SimpleDialog(
        content = stringResource(id = R.string.camerax_record_delete_file_confirm_msg),
        positiveButtonResId = R.string.camerax_dialog_ok,
        negativeButtonResId = R.string.camerax_dialog_cancel,
        onPositiveClick = onConfirm,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}

/**
 * 视频尚未发布，是否确认退出?
 */
@Composable
internal fun ShowExitCapVideoFilePublishDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    SimpleDialog(
        content = stringResource(id = R.string.camerax_record_video_no_publish_confirm_msg),
        positiveButtonResId = R.string.camerax_dialog_ok,
        negativeButtonResId = R.string.camerax_dialog_cancel,
        onPositiveClick = onConfirm,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}

/**
 * 视频正在录制中，是否确认停止拍摄?
 */
@Composable
internal fun ShowStopCapVideoRecordingDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    SimpleDialog(
        content = stringResource(id = R.string.camerax_record_video_recording_stop_confirm_msg),
        positiveButtonResId = R.string.camerax_dialog_ok,
        negativeButtonResId = R.string.camerax_dialog_cancel,
        onPositiveClick = onConfirm,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}

/**
 * 视频尚未发布，是否重新录制一段新的视频?
 */
@Composable
internal fun ShowRestartCapVideoRecordingDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    SimpleDialog(
        content = stringResource(id = R.string.camerax_record_video_no_publish_restart_confirm_msg),
        positiveButtonResId = R.string.camerax_dialog_ok,
        negativeButtonResId = R.string.camerax_dialog_cancel,
        onPositiveClick = onConfirm,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}

