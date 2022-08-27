package com.melody.opengl.camerax.filters

import android.content.res.Resources

class RotationOESFilter(mRes: Resources) : OESFilter(mRes) {
    /**
     * 旋转视频操作
     *
     */
    fun setRotation(rotation: Int) {
        val coord: FloatArray = when (rotation) {
            ROT_0 -> floatArrayOf(
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
            )
            ROT_90 -> floatArrayOf(
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
            )
            ROT_180 -> floatArrayOf(
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                0.0f, 0.0f
            )
            ROT_270 -> floatArrayOf(
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
            )
            else -> return
        }
        texBuffer?.clear()
        texBuffer?.put(coord)
        texBuffer?.position(0)
    }

    companion object {
        const val ROT_0 = 0
        const val ROT_90 = 90
        const val ROT_180 = 180
        const val ROT_270 = 270
    }
}