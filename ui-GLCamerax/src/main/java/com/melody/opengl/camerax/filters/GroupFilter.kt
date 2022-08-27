package com.melody.opengl.camerax.filters

import android.content.res.Resources
import com.melody.opengl.camerax.utils.MatrixUtils.flip
import android.opengl.GLES20
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class GroupFilter(mRes: Resources) : BaseFilter(mRes) {
    private val mFilterQueue: Queue<AFilter>
    private val mFilters: MutableList<AFilter> = mutableListOf()
    private var width = 0
    private var height = 0
    private var size = 0

    fun addFilter(filter: AFilter) {
        flip(filter.matrix, false, true)
        mFilterQueue.add(filter)
    }

    fun removeFilter(filter: AFilter): Boolean {
        val b = mFilters.remove(filter)
        if (b) {
            size--
        }
        return b
    }

    fun removeFilter(index: Int): AFilter {
        val f = mFilters.removeAt(index)
        size--
        return f
    }

    /**
     * 双Texture,一个输入一个输出,循环往复
     */
    fun draw(time: Long) {
        updateFilter()
        textureIndex = 0
        for (filter in mFilters) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0])
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fTexture[textureIndex % 2], 0
            )
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]
            )
            GLES20.glViewport(0, 0, width, height)
            if (textureIndex == 0) {
                filter.textureId = textureId
            } else {
                filter.textureId = fTexture[(textureIndex - 1) % 2]
            }
            filter.draw(time)
            unBindFrame()
            textureIndex++
        }
    }

    private fun updateFilter() {
        var f: AFilter?
        while (mFilterQueue.poll().also { f = it } != null) {
            f?.let {
                it.create()
                it.setSize(width, height)
                mFilters.add(it)
            }
            size++
        }
    }

    override val outputTexture: Int
        get() = if (size == 0) textureId else fTexture[(textureIndex - 1) % 2]

    override fun onCreate() {}
    override fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        updateFilter()
        createFrameBuffer()
    }

    //创建离屏buffer
    private val fTextureSize = 2
    private val fFrame = IntArray(1)
    private val fRender = IntArray(1)
    private val fTexture = IntArray(fTextureSize)
    private var textureIndex = 0

    //创建FrameBuffer
    private fun createFrameBuffer(): Boolean {
        GLES20.glGenFramebuffers(1, fFrame, 0)
        GLES20.glGenRenderbuffers(1, fRender, 0)
        genTextures()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0])
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0])
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width,
            height
        )
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, fTexture[0], 0
        )
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER, fRender[0]
        )
        unBindFrame()
        return false
    }

    //生成Textures
    private fun genTextures() {
        GLES20.glGenTextures(fTextureSize, fTexture, 0)
        for (i in 0 until fTextureSize) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
        }
    }

    //取消绑定Texture
    private fun unBindFrame() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteRenderbuffers(1, fRender, 0)
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
    }

    init {
        mFilterQueue = ConcurrentLinkedDeque()
    }
}