package com.melody.opengl.camerax.utils

import android.opengl.GLES20
import android.util.Log

object EasyGlUtils {
    private fun useTexParameter() {
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
    }

    fun useTexParameter(
        gl_wrap_s: Int, gl_wrap_t: Int, gl_min_filter: Int,
        gl_mag_filter: Int
    ) {
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, gl_wrap_s.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, gl_wrap_t.toFloat())
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            gl_min_filter.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            gl_mag_filter.toFloat()
        )
    }

    @JvmStatic
    fun genTexturesWithParameter(
        size: Int, textures: IntArray, start: Int,
        gl_format: Int, width: Int, height: Int
    ) {
        GLES20.glGenTextures(size, textures, start)
        for (i in 0 until size) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, gl_format, width, height,
                0, gl_format, GLES20.GL_UNSIGNED_BYTE, null
            )
            useTexParameter()
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    @JvmStatic
    fun bindFrameTexture(frameBufferId: Int, textureId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textureId, 0
        )
    }

    @JvmStatic
    fun unBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    @JvmStatic
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e("EasyGIUtils", msg)
            throw RuntimeException(msg)
        }
    }
}