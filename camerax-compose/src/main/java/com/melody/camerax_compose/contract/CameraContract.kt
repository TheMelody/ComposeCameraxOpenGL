package com.melody.camerax_compose.contract

import android.net.Uri
import androidx.camera.core.CameraSelector
import com.melody.camerax_compose.model.VideoRecordState
import com.melody.camerax_compose.model.state.IUiEffect
import com.melody.camerax_compose.model.state.IUiEvent
import com.melody.camerax_compose.model.state.IUiState

/**
 * CameraContract
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 11:43
 */
class CameraContract {
    sealed class Event : IUiEvent {

        /**
         * 前置后置摄像头切换
         */
        object SwitchLensFacing : Event()

        /**
         * 显示删除文件提示的dialog
         */
        data class ShowRemoveFileDialog(val isShow: Boolean) : Event()

        /**
         * 视频文件尚未发布，是否确认退出的dialog
         */
        data class ShowExitPublishDialog(val isShow: Boolean) : Event()

        /**
         * 是否停止视频录制，此场景：正在录制中，此时用户点击了系统返回按钮
         */
        data class ShowStopRecordingDialog(val isShow: Boolean) : Event()

        /**
         * 重新启动新的视频录制，此场景：视频录制已经停止，用户没有去上传发布此视频，这个时候再去点击录制按钮，需要弹出提示
         */
        data class ShowRestartRecordingDialog(val isShow: Boolean) : Event()

    }

    data class State(
        /**
         * 当前摄像头方向，默认是前置
         */
        val lensFacing: Int = CameraSelector.LENS_FACING_FRONT,

        /**
         * 当前拍视频状态
         */
        val recordingState: VideoRecordState?,

        /**
         * 当前是不是拍照片
         */
        val isTakePhoto: Boolean?,

        /**
         * 当前视频拍摄的文件uri
         */
        val videoRecordingFileUri: Uri?,

        /**
         * 当前拍摄照片的文件uri
         */
        val takePictureFileUri: Uri?,

        /**
         * 是否显示删除文件的确认提示框
         */
        val showRemoveFileDialog: Boolean,

        /**
         * 是否显示退出当前页面，取消发布视频
         */
        val showExitPublishDialog: Boolean,

        /**
         * 是否停止视频录制，此场景：正在录制中，此时用户点击了系统返回按钮
         */
        val showStopRecordingDialog: Boolean,

        /**
         * 视频尚未发布，是否重新录制一段新的视频
         */
        val showRestartRecordingDialog: Boolean

    ) : IUiState

    sealed class Effect : IUiEffect {
        /**
         * Toast
         */
        data class Toast(val msg:String) : Effect()

        /**
         * 预览图片或者视频
         */
        data class PreviewImgAndVideo(val uri: Uri) : Effect()

        /**
         * 刷新其他外部元素可见性
         */
        data class RefreshOtherElementVisibility(val isShow: Boolean) : Effect()
    }
}