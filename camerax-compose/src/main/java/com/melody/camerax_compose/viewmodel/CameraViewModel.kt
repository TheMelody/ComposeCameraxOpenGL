package com.melody.camerax_compose.viewmodel

import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import com.melody.camerax_compose.R
import com.melody.camerax_compose.base.BaseViewModel
import com.melody.camerax_compose.contract.CameraContract
import com.melody.camerax_compose.model.VideoRecordState
import com.melody.camerax_compose.repo.CameraRepository
import com.melody.camerax_compose.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * CameraViewModel
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 11:42
 */
class CameraViewModel :
    BaseViewModel<CameraContract.Event, CameraContract.State, CameraContract.Effect>() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    /**
     * 初始化Camera类型
     */
    fun init(isTakePhoto:Boolean) {
        setState {
            copy(isTakePhoto = isTakePhoto)
        }
        if (isTakePhoto) {
            setState {
                copy(takePictureFileUri = CameraRepository.getTakePictureFileUri())
            }
        }
    }

    /**
     * 切换摄像头
     */
    fun switchLensFacing() {
        setEvent(CameraContract.Event.SwitchLensFacing)
    }

    /**
     * 打开文件发布页面
     */
    fun publishVideoFile() {
        currentState.videoRecordingFileUri?.apply {
            setEffect { CameraContract.Effect.PreviewImgAndVideo(this) }
        }
    }

    /**
     * 拍照
     */
    fun takePhoto() = asyncLaunch {
        CameraRepository.playShutterSound()
        currentState.takePictureFileUri.apply {
            if(null == this) {
                setEffect {
                    CameraContract.Effect.Toast(StringUtils.getString(R.string.camerax_take_photo_error_toast))
                }
            } else {
                setEffect { CameraContract.Effect.PreviewImgAndVideo(this) }
            }
        }
    }

    /**
     * 拍视频
     */
    fun takeVideo(isRecording: Boolean) {
        if (null != currentState.videoRecordingFileUri
            && currentState.recordingState == VideoRecordState.STOP
        ) {
            setEvent(CameraContract.Event.ShowRestartRecordingDialog(true))
            return
        }
        if (isRecording) {
            setState { copy(recordingState = VideoRecordState.STOP) }
        } else {
            // 通知其他元素可见性
            setEffect { CameraContract.Effect.RefreshOtherElementVisibility(false) }
            setState {
                copy(
                    recordingState = VideoRecordState.START,
                    videoRecordingFileUri = CameraRepository.getRecordVideoFileUri()
                )
            }
        }
    }

    /**
     * 删除视频文件
     */
    fun removeVideoFile() = asyncLaunch(Dispatchers.IO) {
        currentState.videoRecordingFileUri?.apply {
            CameraRepository.deleteVideoFile(uri = this)
        }
        setState {
            copy(
                videoRecordingFileUri = null,
                showRemoveFileDialog = false,
                recordingState = null
            )
        }
        // 通知其他元素可见性
        setEffect { CameraContract.Effect.RefreshOtherElementVisibility(true) }
    }

    fun showRemoveFileDialog(isShow: Boolean) {
        setEvent(CameraContract.Event.ShowRemoveFileDialog(isShow))
    }

    fun showExitPublishDialog(isShow: Boolean) {
        setEvent(CameraContract.Event.ShowExitPublishDialog(isShow))
    }

    fun handleStopRecordingDialog(isStop: Boolean) {
        if (isStop) {
            // 停止视频录制
            setState { copy(recordingState = VideoRecordState.STOP) }
        }
        // 隐藏dialog
        setEvent(CameraContract.Event.ShowStopRecordingDialog(false))
    }

    fun handleRestartRecordingDialog(isRestart: Boolean) = asyncLaunch(Dispatchers.IO) {
        setEvent(CameraContract.Event.ShowRestartRecordingDialog(false))
        if (isRestart) {
            setState { copy(videoRecordingFileUri = null, recordingState = null) }
            // 确保setState状态更新成功，再去执行takeVideo
            delay(100)
            // 重新录制新的视频文件，然后去上传
            takeVideo(false)
        }
    }

    fun handleBackPress(block: () -> Unit) {
        if (currentState.isTakePhoto == false) {
            if (null != currentState.recordingState
                && currentState.recordingState == VideoRecordState.START
            ) {
                // 显示是否停止视频拍摄的Dialog
                setEvent(CameraContract.Event.ShowStopRecordingDialog(true))
                return
            } else if (null != currentState.videoRecordingFileUri) {
                // 视频文件尚未发布，显示：是否确认退出页面的 Dialog
                showExitPublishDialog(true)
                return
            }
        }
        block.invoke()
    }

    override fun createInitialState(): CameraContract.State {
        return CameraContract.State(
            recordingState = null,
            isTakePhoto = null,
            takePictureFileUri = null,
            videoRecordingFileUri = null,
            showRemoveFileDialog = false,
            showExitPublishDialog = false,
            showStopRecordingDialog = false,
            showRestartRecordingDialog = false
        )
    }

    override fun handleEvents(event: CameraContract.Event) {
        when (event) {
            is CameraContract.Event.SwitchLensFacing -> {
                val curLensFacing = currentState.lensFacing
                if (currentState.isTakePhoto == true) {
                    setState {
                        copy(
                            lensFacing = if (curLensFacing == CameraSelector.LENS_FACING_FRONT) {
                                CameraSelector.LENS_FACING_BACK
                            } else {
                                CameraSelector.LENS_FACING_FRONT
                            }
                        )
                    }
                } else {
                    setState {
                        copy(
                            lensFacing = if (curLensFacing == CameraSelector.LENS_FACING_FRONT) {
                                CameraSelector.LENS_FACING_BACK
                            } else {
                                CameraSelector.LENS_FACING_FRONT
                            }
                        )
                    }
                }
            }
            is CameraContract.Event.ShowRemoveFileDialog -> {
                setState { copy(showRemoveFileDialog = event.isShow) }
            }
            is CameraContract.Event.ShowExitPublishDialog -> {
                setState { copy(showExitPublishDialog = event.isShow) }
            }
            is CameraContract.Event.ShowStopRecordingDialog -> {
                setState { copy(showStopRecordingDialog = event.isShow) }
            }
            is CameraContract.Event.ShowRestartRecordingDialog -> {
                setState { copy(showRestartRecordingDialog = event.isShow) }
            }
        }
    }
}