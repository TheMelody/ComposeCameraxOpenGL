package com.melody.opengl.camerax.filters

import android.content.res.Resources
import androidx.camera.core.CameraSelector

/**
 * description:
 * Created by aserbao on 2018/5/15.
 */
class CameraFilter(mRes: Resources) : OESFilter(mRes) {
    //后置摄像头 顺时针旋转90度
    //前置摄像头 顺时针旋转90,并上下颠倒
    override var flag: Int
        get() = super.flag
        set(flag) {
            super.flag = flag
            val coord: FloatArray = if (flag == CameraSelector.LENS_FACING_FRONT) {
                //前置摄像头 顺时针旋转90,并上下颠倒
                floatArrayOf(
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f
                )
            } else {
                //后置摄像头 顺时针旋转90度
                floatArrayOf(
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
                )
            }
            texBuffer?.clear()
            texBuffer?.put(coord)
            texBuffer?.position(0)
        }
}