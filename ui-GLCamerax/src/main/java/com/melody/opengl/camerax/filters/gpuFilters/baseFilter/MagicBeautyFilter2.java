package com.melody.opengl.camerax.filters.gpuFilters.baseFilter;

import android.opengl.GLES20;

import com.melody.opengl.camerax.R;
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils;

public class MagicBeautyFilter2 extends GPUImageFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private int mBrightnessLocation;
    private float mToneLevel;
    private float mBeautyLevel;
    private float mBrightLevel;

    public MagicBeautyFilter2(){
        super(NO_FILTER_VERTEX_SHADER ,
                OpenGlUtils.readShaderFromRawResource(R.raw.beauty2));
    }

    protected void onInit() {
        // 测试效果的参数值
        /*mToneLevel = 0.5F;
        mBeautyLevel = 1F;
        mBrightLevel = 0.2F;*/
        super.onInit();
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        mBrightnessLocation = GLES20.glGetUniformLocation(getProgram(), "brightness");
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        setParams(mBeautyLevel, mToneLevel);
        setBrightLevel(mBrightLevel);
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public float getBeautyLevel(){
        return this.mBeautyLevel;
    }

    public boolean getBeautyFeatureEnable() {
        return mToneLevel > 0F || mBeautyLevel > 0F;
    }

    /**
     * 磨皮
     */
    public void setBeautyLevel(float beautyLevel) {
        this.mBeautyLevel = beautyLevel;
        setParams(beautyLevel, mToneLevel);
    }

    /**
     * 美白
     */
    public void setBrightLevel(float brightLevel) {
        this.mBrightLevel = brightLevel;
        setFloat(mBrightnessLocation, 0.6f * (-0.5f + brightLevel));
    }

    /**
     * 红润
     */
    public void setToneLevel(float toneLevel) {
        this.mToneLevel = toneLevel;
        setParams(mBeautyLevel, toneLevel);
    }

    public void setAllBeautyParams(float beauty,float bright,float tone) {
        setBeautyLevel(beauty);
        setBrightLevel(bright);
        setToneLevel(tone);
    }

    private void setParams(float beauty, float tone) {
        float[] vector = new float[4];
        vector[0] = 1.0f - 0.6f * beauty;
        vector[1] = 1.0f - 0.3f * beauty;
        vector[2] = 0.1f + 0.3f * tone;
        vector[3] = 0.1f + 0.3f * tone;
        setFloatVec4(mParamsLocation, vector);
    }
}
