package com.melody.camerax_compose.widget

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melody.camerax_compose.R

/**
 * 提交按钮
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/7/11 15:25
 */
@Composable
fun SubmitButton(
    modifier: Modifier,
    enabled: Boolean = true,
    @StringRes buttonTextRes: Int? = null,
    buttonText: String? = null,
    buttonHeight: Dp,
    shapeRadius: Dp = 4.dp,
    fontSize: TextUnit = 14.sp,
    background:Color = colorResource(id = R.color.camerax_button_submit_background_color),
    textColor:Color = colorResource(id = R.color.camerax_button_submit_text_color),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    Box(modifier = modifier
        .height(buttonHeight)
        .background(
            color = background,
            shape = RoundedCornerShape(shapeRadius)
        )
        .clip(RoundedCornerShape(shapeRadius))
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = indication
        ) {
            onClick.invoke()
        }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = if (null != buttonTextRes) stringResource(id = buttonTextRes) else buttonText
                ?: "",
            style = TextStyle(
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            )
        )
    }
}