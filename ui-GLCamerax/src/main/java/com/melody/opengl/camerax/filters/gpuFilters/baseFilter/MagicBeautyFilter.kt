package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.readShaderFromRawResource
import com.melody.opengl.camerax.R
import android.opengl.GLES20

class MagicBeautyFilter : GPUImageFilter(
    NO_FILTER_VERTEX_SHADER,
    readShaderFromRawResource(R.raw.beauty)
) {
    private var mSingleStepOffsetLocation = 0
    private var mParamsLocation = 0
    private var mLevel = 0
    override fun onInit() {
        super.onInit()
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(program, "singleStepOffset")
        mParamsLocation = GLES20.glGetUniformLocation(program, "params")
        beautyLevel = 3 //beauty Level
    }

    private fun setTexelSize(w: Float, h: Float) {
        setFloatVec2(mSingleStepOffsetLocation, floatArrayOf(2.0f / w, 2.0f / h))
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        setTexelSize(width.toFloat(), height.toFloat())
    }

    var beautyLevel: Int
        get() = mLevel
        set(level) {
            mLevel = level
            when (level) {
                1 -> setFloat(mParamsLocation, 1.0f)
                2 -> setFloat(mParamsLocation, 0.8f)
                3 -> setFloat(mParamsLocation, 0.6f)
                4 -> setFloat(mParamsLocation, 0.4f)
                5 -> setFloat(mParamsLocation, 0.33f)
                else -> {}
            }
        }

    fun onBeautyLevelChanged() {
        beautyLevel = 3 //beauty level
    }
}