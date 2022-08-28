package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.readShaderFromRawResource
import com.melody.opengl.camerax.R
import android.opengl.GLES20

class MagicBeautyFilter2 : GPUImageFilter(
    NO_FILTER_VERTEX_SHADER,
    readShaderFromRawResource(R.raw.beauty2)
) {
    private var mSingleStepOffsetLocation = 0
    private var mParamsLocation = 0
    private var mBrightnessLocation = 0
    private var mToneLevel = 0f
    private var mBeautyLevel = 0f
    private var mBrightLevel = 0f
    override fun onInit() {
        // 测试效果的参数值
        /*mToneLevel = 0.5F;
        mBeautyLevel = 1F;
        mBrightLevel = 0.2F;*/
        super.onInit()
        mParamsLocation = GLES20.glGetUniformLocation(program, "params")
        mBrightnessLocation = GLES20.glGetUniformLocation(program, "brightness")
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(program, "singleStepOffset")
        setParams(mBeautyLevel, mToneLevel)
        setBrightLevel(mBrightLevel)
    }

    private fun setTexelSize(w: Float, h: Float) {
        setFloatVec2(mSingleStepOffsetLocation, floatArrayOf(2.0f / w, 2.0f / h))
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        setTexelSize(width.toFloat(), height.toFloat())
    }

    /**
     * 磨皮
     */
    var beautyLevel: Float
        get() = mBeautyLevel
        set(beautyLevel) {
            mBeautyLevel = beautyLevel
            setParams(beautyLevel, mToneLevel)
        }
    val beautyFeatureEnable: Boolean
        get() = mToneLevel > 0f || mBeautyLevel > 0f

    /**
     * 美白
     */
    fun setBrightLevel(brightLevel: Float) {
        mBrightLevel = brightLevel
        setFloat(mBrightnessLocation, 0.6f * (-0.5f + brightLevel))
    }

    /**
     * 红润
     */
    fun setToneLevel(toneLevel: Float) {
        mToneLevel = toneLevel
        setParams(mBeautyLevel, toneLevel)
    }

    fun setAllBeautyParams(beauty: Float, bright: Float, tone: Float) {
        beautyLevel = beauty
        setBrightLevel(bright)
        setToneLevel(tone)
    }

    private fun setParams(beauty: Float, tone: Float) {
        val vector = FloatArray(4)
        vector[0] = 1.0f - 0.6f * beauty
        vector[1] = 1.0f - 0.3f * beauty
        vector[2] = 0.1f + 0.3f * tone
        vector[3] = 0.1f + 0.3f * tone
        setFloatVec4(mParamsLocation, vector)
    }
}