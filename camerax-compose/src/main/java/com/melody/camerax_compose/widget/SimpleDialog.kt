package com.melody.camerax_compose.widget

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.melody.camerax_compose.R

/**
 * 统一的Dialog样式
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/4/22 11:40
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SimpleDialog(
    @StringRes positiveButtonResId: Int,
    @StringRes negativeButtonResId: Int,
    content: String,
    onPositiveClick:()->Unit,
    onNegativeClick:()->Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(283.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(4.dp),
            color = colorResource(id = R.color.camerax_dialog_window_background)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(19.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = content,
                        style = TextStyle(
                            color = colorResource(id = R.color.camerax_dialog_content_text_color),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 19.dp, end = 19.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .height(42.dp)
                ) {

                    SubmitButton(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                        buttonHeight = 42.dp,
                        background = colorResource(id = R.color.camerax_dialog_negative_button_bg_color),
                        textColor = colorResource(id = R.color.camerax_dialog_negative_button_text_color),
                        buttonTextRes = negativeButtonResId,
                        onClick = onNegativeClick
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    SubmitButton(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                        background = colorResource(id = R.color.camerax_dialog_positive_button_bg_color),
                        textColor = colorResource(id = R.color.camerax_dialog_positive_button_text_color),
                        buttonHeight = 42.dp,
                        buttonTextRes = positiveButtonResId,
                        onClick = onPositiveClick
                    )
                }
            }
        }
    }
}