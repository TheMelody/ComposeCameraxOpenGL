package com.melody.camerax_compose.extensions

import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInteropFilter

/**
 * ModifierExtensions
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/08/27 11:20
 */
/**
 * 触摸控件之后，产生按压缩放
 * @param updateTargetValue 入参true:表示此时是ACTION_DOWN，false:表示此时是ACTION_UP
 * @param onClick 点击事件回调
 */
@OptIn(ExperimentalComposeUiApi::class)
inline fun Modifier.touchScale(
    crossinline updateTargetValue: (Boolean) -> Unit,
    crossinline onClick: () -> Unit
): Modifier =
    composed {
        var itemDownTime by remember { mutableStateOf(0L) }
        pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    updateTargetValue.invoke(true)
                    itemDownTime = System.currentTimeMillis()
                    return@pointerInteropFilter true
                }
                MotionEvent.ACTION_UP -> {
                    updateTargetValue.invoke(false)
                    val diffTime = System.currentTimeMillis() - itemDownTime
                    if (diffTime in 1..250) {
                        onClick.invoke()
                    }
                }
            }
            false
        }
    }
