package com.melody.camerax_compose.optionsbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.melody.camerax_compose.R
import com.melody.camerax_compose.extensions.touchScale
import com.melody.camerax_compose.model.VideoRecordState

/**
 * 点击：拍摄视频
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/08/27 11:17
 */
@Composable
internal fun TakeVideoOptionsBar(
    recordingState: VideoRecordState?,
    recordingTime: String,
    onPublishVideo: () -> Unit,
    onDeleteFile: () -> Unit,
    onTakeVideo: () -> Unit
) {
    // 当前是否处于正在录制中
    val isRecording = null != recordingState && recordingState == VideoRecordState.START

    ConstraintLayout(modifier = Modifier.padding(bottom = 30.dp)) {
        val (deleteRef,recordRef,saveRef,timeInfoRef)  = createRefs()

        // 这里显示当前拍摄视频的进度，时间
        RenderVideoRecordTimeInfo(
            modifier = Modifier.constrainAs(timeInfoRef) {
                start.linkTo(recordRef.start)
                end.linkTo(recordRef.end)
                bottom.linkTo(recordRef.top, margin = 16.dp)
            },
            isRecording = isRecording,
            showTimeInfo = null != recordingState,
            recordingTime = recordingTime
        )

        ButtonTakeVideo(
            modifier = Modifier
                .constrainAs(recordRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            isRecording = isRecording,
            onTakeVideo = onTakeVideo,
        )

        ButtonOptions(
            modifier = Modifier.constrainAs(deleteRef){
                top.linkTo(recordRef.top)
                bottom.linkTo(recordRef.bottom)
                end.linkTo(recordRef.start, margin = 36.dp)
            },
            iconResId = R.mipmap.ic_camera_options_delete,
            labelResId = R.string.camerax_video_options_delete,
            verticalSpacing = 7.dp,
            onClick = onDeleteFile,
            showOptions = recordingState == VideoRecordState.STOP
        )

        ButtonOptions(
            modifier = Modifier.constrainAs(saveRef){
                top.linkTo(recordRef.top)
                bottom.linkTo(recordRef.bottom)
                start.linkTo(recordRef.end, margin = 36.dp)
            },
            iconResId = R.mipmap.ic_camera_options_save,
            labelResId = R.string.camerax_video_options_complete,
            verticalSpacing = 4.dp,
            onClick = onPublishVideo,
            showOptions = recordingState == VideoRecordState.STOP
        )
    }
}

@Composable
private fun ButtonOptions(
    modifier: Modifier,
    showOptions:Boolean,
    @DrawableRes iconResId: Int,
    @StringRes labelResId: Int,
    verticalSpacing: Dp,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = showOptions, label = "")
    val animateAlpha = transition.animateFloat(label = "") {
        when (it) {
            true -> 1F
            else -> 0F
        }
    }
    IconButton(
        modifier = modifier.alpha(animateAlpha.value),
        enabled = animateAlpha.value == 1F,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconResId),
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.FillBounds,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(verticalSpacing))

            Text(
                text = stringResource(id = labelResId),
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 12.sp
                )
            )
        }
    }
}

/**
 * 渲染录制的时长信息
 * @param isRecording true:有颜色交换的动画,false:无动画
 * @param showTimeInfo true:显示时长信息,false:空白占位
 */
@Composable
private fun RenderVideoRecordTimeInfo(
    modifier: Modifier,
    isRecording: Boolean,
    showTimeInfo: Boolean,
    recordingTime:String
) {
    val redColor = colorResource(id = R.color.camerax_cap_video_time_info_record_color)
    val transition = updateTransition(targetState = showTimeInfo, label = "")
    val animateAlpha = transition.animateFloat(label = "") {
        when (it) {
            true -> 1F
            else -> 0F
        }
    }
    val infiniteTransition = rememberInfiniteTransition()
    // 颜色动画
    val gradientRecordingColor by infiniteTransition.animateColor(
        initialValue = colorResource(id = R.color.camerax_cap_video_time_info_record_color2),
        targetValue = redColor,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    Row(
        modifier = modifier
            .alpha(animateAlpha.value)
            .width(80.dp)
            .height(28.dp)
            .background(
                color = colorResource(id = R.color.camerax_cap_video_time_info_shape_color),
                shape = CircleShape
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 3.dp
                )
                .size(3.dp)
                .drawBehind {
                    drawCircle(
                        color = if (isRecording) {
                            gradientRecordingColor
                        } else {
                            redColor
                        }
                    )
                }
        )
        Text(
            text = recordingTime,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}


@Composable
private fun ButtonTakeVideo(
    modifier: Modifier,
    isRecording: Boolean,
    onTakeVideo: () -> Unit
) {
    val redColor = colorResource(id = R.color.camerax_cap_video_recording_btn_bg_color)

    // 按钮切换动画
    val transition = updateTransition(targetState = isRecording, label = "")

    val innerButtonSize = transition.animateDp(label = "") {
        when (it) {
            true -> 26.dp
            else -> 62.dp
        }
    }
    val roundSize = transition.animateFloat(label = "") {
        when (it) {
            true -> {
                with(LocalDensity.current){
                    4.dp.toPx()
                }
            }
            else -> with(LocalDensity.current){
                42.dp.toPx()
            }
        }
    }
    var targetValue by remember { mutableStateOf(1F) }

    val animationProgress by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring()
    )
    Box(
        modifier = modifier
            .size(74.dp)
            .border(
                width = 3.dp,
                color = colorResource(id = R.color.camerax_cap_video_recording_border_bg_color),
                shape = CircleShape
            )
    ) {
        Spacer(
            modifier = Modifier
                .align(Alignment.Center)
                .size(innerButtonSize.value)
                .scale(animationProgress)
                .drawBehind {
                    drawRoundRect(
                        color = redColor,
                        cornerRadius = CornerRadius(roundSize.value,roundSize.value)
                    )
                }
                .touchScale(
                    updateTargetValue = {
                        targetValue = if (it) 0.88F else 1F
                    },
                    onClick = onTakeVideo
                )
        )
    }
}