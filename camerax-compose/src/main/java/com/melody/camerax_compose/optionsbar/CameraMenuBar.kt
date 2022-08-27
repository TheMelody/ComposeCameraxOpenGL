package com.melody.camerax_compose.optionsbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melody.camerax_compose.R
import com.melody.camerax_compose.model.VideoRecordState

/**
 * CameraMenuBar
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 22:22
 */
@Composable
internal fun CameraMenuBar(recordingState: VideoRecordState?, onReverseCameraClick: () -> Unit) {
    val transition = updateTransition(targetState = (null== recordingState), label = "")
    val animateAlpha = transition.animateFloat(label = "") {
        when (it) {
            true -> 1F
            else -> 0F
        }
    }
    Box(
        modifier = Modifier
            .statusBarsPadding()
            .padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp
            )
            .fillMaxSize()
    ) {
        OptionsBarMenuItem(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(animateAlpha.value),
            iconResId = R.mipmap.ic_camera_menu_reverse,
            labelResId = R.string.camerax_record_menu_options_reserve,
            onClick = onReverseCameraClick
        )
    }
}


@Composable
private fun OptionsBarMenuItem(
    modifier: Modifier,
    @DrawableRes iconResId: Int,
    @StringRes labelResId: Int,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.width(24.dp),
        onClick = onClick
    ) {
        Column {
            Image(
                painter = painterResource(id = iconResId),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(20.dp),
                contentDescription = null
            )
            Text(
                text = stringResource(id = labelResId), style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )
        }
    }
}