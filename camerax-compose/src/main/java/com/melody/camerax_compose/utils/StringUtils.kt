package com.melody.camerax_compose.utils

import androidx.annotation.StringRes
import com.melody.opengl.camerax.utils.GLCameraxUtils

/**
 * StringUtils
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 14:08
 */
object StringUtils {
    fun getString(@StringRes resId: Int):String {
        return GLCameraxUtils.getApplicationContext().getString(resId)
    }
}