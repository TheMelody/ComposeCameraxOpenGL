package com.melody.opengl.camerax.filters

import android.content.res.Resources
import com.melody.opengl.camerax.utils.MatrixUtils.originalMatrix
import com.melody.opengl.camerax.utils.MatrixUtils.flip
import com.melody.opengl.camerax.utils.EasyGlUtils.bindFrameTexture
import com.melody.opengl.camerax.utils.EasyGlUtils.unBindFrameBuffer
import com.melody.opengl.camerax.utils.EasyGlUtils.genTexturesWithParameter
import android.opengl.GLES20

/**
 * 这里重写了draw方法，所以父类中的矩阵对此绘制无效
 */
class CameraDrawProcessFilter(mRes: Resources) : BaseFilter(
    mRes
) {
    private val mFilter: BaseFilter
    private val fFrame = IntArray(1)
    private val fRender = IntArray(1)
    private val fTexture = IntArray(1)
    private var width = 0
    private var height = 0
    override fun onCreate() {
        mFilter.create()
    }

    override val outputTexture: Int
        get() = fTexture[0]

    override fun draw() {
        val b = GLES20.glIsEnabled(GLES20.GL_CULL_FACE)
        if (b) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
        GLES20.glViewport(0, 0, width, height)
        bindFrameTexture(fFrame[0], fTexture[0])
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER, fRender[0]
        )
        mFilter.textureId = textureId
        mFilter.draw()
        unBindFrameBuffer()
        if (b) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }
    }

    override fun onSizeChanged(width: Int, height: Int) {
        if (this.width != width && this.height != height) {
            this.width = width
            this.height = height
            mFilter.setSize(width, height)
            deleteFrameBuffer()
            GLES20.glGenFramebuffers(1, fFrame, 0)
            GLES20.glGenRenderbuffers(1, fRender, 0)
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0])
            GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                width, height
            )
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]
            )
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
            genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height)
        }
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteRenderbuffers(1, fRender, 0)
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
    }

    init {
        mFilter = NoneFilter(mRes!!)
        val OM = originalMatrix
        //矩阵上下翻转
        flip(OM, false, false)
        mFilter.matrix = OM
    }
}