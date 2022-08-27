package com.melody.camerax_compose.optionsbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.melody.camerax_compose.extensions.touchScale
import com.melody.camerax_compose.R

/**
 * 点击：拍照片
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/08/27 11:30
 */
@Composable
internal fun TakePhotoOptionsBar(onTakePhoto: () -> Unit) {
    var targetValue by remember { mutableStateOf(1F) }

    val animationProgress by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring()
    )

    Box(modifier = Modifier
            .padding(bottom = 30.dp)
            .size(74.dp)
            .border(
                width = 3.dp,
                color = colorResource(id = R.color.camerax_cap_button_border_color),
                shape = CircleShape
            )
    ) {
        Spacer(
            modifier = Modifier
                .align(Alignment.Center)
                .size(62.dp)
                .scale(animationProgress)
                .background(
                    color = colorResource(id = R.color.camerax_cap_button_bg_color),
                    shape = CircleShape
                )
                .touchScale(
                    updateTargetValue = {
                        targetValue = if (it) 0.88F else 1F
                    },
                    onClick = onTakePhoto
                )
        )
    }
}