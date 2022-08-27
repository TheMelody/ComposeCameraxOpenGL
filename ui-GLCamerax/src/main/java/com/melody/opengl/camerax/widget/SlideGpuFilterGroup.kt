package com.melody.opengl.camerax.widget

import com.melody.opengl.camerax.model.MagicFilterType
import android.widget.Scroller
import com.melody.opengl.camerax.model.MagicFilterFactory
import android.opengl.GLES20
import com.melody.opengl.camerax.utils.EasyGlUtils
import android.view.MotionEvent
import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.GPUImageFilter
import com.melody.opengl.camerax.utils.GLCameraxUtils
import kotlin.math.abs

/**
 * 左右水平滑动切换滤镜
 */
class SlideGpuFilterGroup {
    private val types = arrayOf(
        MagicFilterType.NONE,
        MagicFilterType.FAIRYTALE,
        MagicFilterType.SUNRISE,
        MagicFilterType.SUNSET,
        MagicFilterType.WHITECAT,
        MagicFilterType.BLACKCAT,
        MagicFilterType.SKINWHITEN,
        MagicFilterType.HEALTHY,
        MagicFilterType.SWEETS,
        MagicFilterType.ROMANCE,
        MagicFilterType.SAKURA,
        MagicFilterType.WARM,
        MagicFilterType.ANTIQUE,
        MagicFilterType.CALM,
        MagicFilterType.LATTE,
        MagicFilterType.TENDER,
        MagicFilterType.COOL,
        MagicFilterType.EMERALD,
        MagicFilterType.EVERGREEN,
        MagicFilterType.AMARO,
        MagicFilterType.BRANNAN,
        MagicFilterType.BROOKLYN,
        MagicFilterType.EARLYBIRD,
        MagicFilterType.FREUD,
        MagicFilterType.HEFE,
        MagicFilterType.HUDSON,
        MagicFilterType.INKWELL,
        MagicFilterType.KEVIN,
        MagicFilterType.N1977,
        MagicFilterType.NASHVILLE,
        MagicFilterType.PIXAR,
        MagicFilterType.RISE,
        MagicFilterType.SIERRA,
        MagicFilterType.SUTRO,
        MagicFilterType.TOASTER2,
        MagicFilterType.VALENCIA,
        MagicFilterType.WALDEN,
        MagicFilterType.XPROII,
        MagicFilterType.CONTRAST,
        MagicFilterType.BRIGHTNESS,
        MagicFilterType.EXPOSURE,
        MagicFilterType.HUE,
        MagicFilterType.SATURATION,
        MagicFilterType.SHARPEN
    )
    private var curFilter: GPUImageFilter? = null
    private var leftFilter: GPUImageFilter? = null
    private var rightFilter: GPUImageFilter? = null
    private var width = 0
    private var height = 0
    private val fFrame = IntArray(1)
    private val fTexture = IntArray(1)
    private var curIndex = 0
    private val scroller: Scroller
    private var mListener: OnFilterChangeListener? = null
    
    private val mScreenWidth: Int =
        GLCameraxUtils.getApplicationContext().resources.displayMetrics.widthPixels

    fun setFilter(i: Int) {
        curIndex = i
        locked = true
        downX = -1
        needSwitch = true
        direction = -1
    }

    private fun initFilter() {
        curFilter = getFilter(curIndex)
        leftFilter = getFilter(leftIndex)
        rightFilter = getFilter(rightIndex)
    }

    private fun getFilter(index: Int): GPUImageFilter {
        var filter = MagicFilterFactory.initFilters(types[index])
        if (filter == null) {
            filter = GPUImageFilter()
        }
        return filter
    }

    fun init() {
        curFilter?.init()
        leftFilter?.init()
        rightFilter?.init()
    }

    fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES20.glGenFramebuffers(1, fFrame, 0)
        EasyGlUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height)
        onFilterSizeChanged(width, height)
    }

    private fun onFilterSizeChanged(width: Int, height: Int) {
        curFilter?.onInputSizeChanged(width, height)
        leftFilter?.onInputSizeChanged(width, height)
        rightFilter?.onInputSizeChanged(width, height)
        curFilter?.onDisplaySizeChanged(width, height)
        leftFilter?.onDisplaySizeChanged(width, height)
        rightFilter?.onDisplaySizeChanged(width, height)
    }

    val outputTexture: Int
        get() = fTexture[0]

    fun onDrawFrame(textureId: Int) {
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0])
        if (direction == 0 && offset == 0) {
            curFilter?.onDrawFrame(textureId)
        } else if (direction == 1) {
            onDrawSlideLeft(textureId)
        } else if (direction == -1) {
            onDrawSlideRight(textureId)
        }
        EasyGlUtils.unBindFrameBuffer()
    }

    private fun onDrawSlideLeft(textureId: Int) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.currX
            drawSlideLeft(textureId)
        } else {
            drawSlideLeft(textureId)
            if (locked) {
                if (needSwitch) {
                    reCreateRightFilter()
                    if (mListener != null) {
                        mListener?.onFilterChange(types[curIndex])
                    }
                }
                offset = 0
                direction = 0
                locked = false
            }
        }
    }

    private fun onDrawSlideRight(textureId: Int) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.currX
            drawSlideRight(textureId)
        } else {
            drawSlideRight(textureId)
            if (locked) {
                if (needSwitch) {
                    reCreateLeftFilter()
                    if (mListener != null) {
                        mListener?.onFilterChange(types[curIndex])
                    }
                }
                offset = 0
                direction = 0
                locked = false
            }
        }
    }

    private fun drawSlideLeft(textureId: Int) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        GLES20.glScissor(0, 0, offset, height)
        leftFilter?.onDrawFrame(textureId)
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        GLES20.glScissor(offset, 0, width - offset, height)
        curFilter?.onDrawFrame(textureId)
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
    }

    private fun drawSlideRight(textureId: Int) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        GLES20.glScissor(0, 0, width - offset, height)
        curFilter?.onDrawFrame(textureId)
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        GLES20.glScissor(width - offset, 0, offset, height)
        rightFilter?.onDrawFrame(textureId)
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
    }

    private fun reCreateRightFilter() {
        decreaseCurIndex()
        rightFilter?.destroy()
        rightFilter = curFilter
        curFilter = leftFilter
        leftFilter = getFilter(leftIndex)
        leftFilter?.init()
        leftFilter?.onDisplaySizeChanged(width, height)
        leftFilter?.onInputSizeChanged(width, height)
        needSwitch = false
    }

    private fun reCreateLeftFilter() {
        increaseCurIndex()
        leftFilter?.destroy()
        leftFilter = curFilter
        curFilter = rightFilter
        rightFilter = getFilter(rightIndex)
        rightFilter?.init()
        rightFilter?.onDisplaySizeChanged(width, height)
        rightFilter?.onInputSizeChanged(width, height)
        needSwitch = false
    }

    fun destroy() {
        curFilter?.destroy()
        leftFilter?.destroy()
        rightFilter?.destroy()
    }

    private val leftIndex: Int
        get() {
            var leftIndex = curIndex - 1
            if (leftIndex < 0) {
                leftIndex = types.size - 1
            }
            return leftIndex
        }
    private val rightIndex: Int
        get() {
            var rightIndex = curIndex + 1
            if (rightIndex >= types.size) {
                rightIndex = 0
            }
            return rightIndex
        }

    private fun increaseCurIndex() {
        curIndex++
        if (curIndex >= types.size) {
            curIndex = 0
        }
    }

    private fun decreaseCurIndex() {
        curIndex--
        if (curIndex < 0) {
            curIndex = types.size - 1
        }
    }

    private var downX = 0
    private var downTime: Long = 0
    // 0为静止,-1为向左滑,1为向右滑
    private var direction = 0
    private var offset = 0
    private var locked = false
    private var needSwitch = false

    fun onTouchEvent(event: MotionEvent) {
        if (locked) {
            return
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                downX = event.x.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                if (downX == -1) {
                    return
                }
                val curX = event.x.toInt()
                direction = if (curX > downX) {
                    1
                } else {
                    -1
                }
                offset = abs(curX - downX)
            }
            MotionEvent.ACTION_UP -> {
                if (downX == -1) {
                    return
                }
                if (offset == 0) {
                    return
                }
                locked = true
                downX = -1
                val isFastScroll = (System.currentTimeMillis() - downTime < 200 && offset > 50
                        || offset > mScreenWidth / 4)
                needSwitch = if (isFastScroll) {
                    scroller.startScroll(
                        offset,
                        0,
                        mScreenWidth - offset,
                        0,
                        100 * (1 - offset / mScreenWidth)
                    )
                    true
                } else {
                    scroller.startScroll(offset, 0, -offset, 0, 100 * (offset / mScreenWidth))
                    false
                }
            }
        }
    }

    fun setOnFilterChangeListener(listener: OnFilterChangeListener?) {
        mListener = listener
    }

    interface OnFilterChangeListener {
        fun onFilterChange(type: MagicFilterType?)
    }

    init {
        initFilter()
        scroller = Scroller(GLCameraxUtils.getApplicationContext())
    }
}