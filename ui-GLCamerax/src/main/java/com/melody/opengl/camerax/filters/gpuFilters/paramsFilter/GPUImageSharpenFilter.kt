/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.melody.opengl.camerax.filters.gpuFilters.paramsFilter

import android.opengl.GLES20
import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.GPUImageFilter

/**
 * Sharpens the picture. <br></br>
 * <br></br>
 * sharpness: from -4.0 to 4.0, with 0.0 as the normal level
 */
class GPUImageSharpenFilter constructor(private var mSharpness: Float = 2.5f) :
    GPUImageFilter(
        SHARPEN_VERTEX_SHADER, SHARPEN_FRAGMENT_SHADER
    ) {
    private var mSharpnessLocation = 0
    private var mImageWidthFactorLocation = 0
    private var mImageHeightFactorLocation = 0
    public override fun onInit() {
        super.onInit()
        mSharpnessLocation = GLES20.glGetUniformLocation(program, "sharpness")
        mImageWidthFactorLocation = GLES20.glGetUniformLocation(program, "imageWidthFactor")
        mImageHeightFactorLocation = GLES20.glGetUniformLocation(program, "imageHeightFactor")
        setSharpness(mSharpness)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        setFloat(mImageWidthFactorLocation, 1.0f / width)
        setFloat(mImageHeightFactorLocation, 1.0f / height)
    }

    fun setSharpness(sharpness: Float) {
        mSharpness = sharpness
        setFloat(mSharpnessLocation, mSharpness)
    }

    companion object {
        const val SHARPEN_VERTEX_SHADER = "" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                "\n" +
                "uniform float imageWidthFactor; \n" +
                "uniform float imageHeightFactor; \n" +
                "uniform float sharpness;\n" +
                "\n" +
                "varying vec2 textureCoordinate;\n" +
                "varying vec2 leftTextureCoordinate;\n" +
                "varying vec2 rightTextureCoordinate; \n" +
                "varying vec2 topTextureCoordinate;\n" +
                "varying vec2 bottomTextureCoordinate;\n" +
                "\n" +
                "varying float centerMultiplier;\n" +
                "varying float edgeMultiplier;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = position;\n" +
                "    \n" +
                "    mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);\n" +
                "    mediump vec2 heightStep = vec2(0.0, imageHeightFactor);\n" +
                "    \n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "    leftTextureCoordinate = inputTextureCoordinate.xy - widthStep;\n" +
                "    rightTextureCoordinate = inputTextureCoordinate.xy + widthStep;\n" +
                "    topTextureCoordinate = inputTextureCoordinate.xy + heightStep;     \n" +
                "    bottomTextureCoordinate = inputTextureCoordinate.xy - heightStep;\n" +
                "    \n" +
                "    centerMultiplier = 1.0 + 4.0 * sharpness;\n" +
                "    edgeMultiplier = sharpness;\n" +
                "}"
        const val SHARPEN_FRAGMENT_SHADER = "" +
                "precision highp float;\n" +
                "\n" +
                "varying highp vec2 textureCoordinate;\n" +
                "varying highp vec2 leftTextureCoordinate;\n" +
                "varying highp vec2 rightTextureCoordinate; \n" +
                "varying highp vec2 topTextureCoordinate;\n" +
                "varying highp vec2 bottomTextureCoordinate;\n" +
                "\n" +
                "varying highp float centerMultiplier;\n" +
                "varying highp float edgeMultiplier;\n" +
                "\n" +
                "uniform sampler2D inputImageTexture;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "    mediump vec3 textureColor = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
                "    mediump vec3 leftTextureColor = texture2D(inputImageTexture, leftTextureCoordinate).rgb;\n" +
                "    mediump vec3 rightTextureColor = texture2D(inputImageTexture, rightTextureCoordinate).rgb;\n" +
                "    mediump vec3 topTextureColor = texture2D(inputImageTexture, topTextureCoordinate).rgb;\n" +
                "    mediump vec3 bottomTextureColor = texture2D(inputImageTexture, bottomTextureCoordinate).rgb;\n" +
                "\n" +
                "    gl_FragColor = vec4((textureColor * centerMultiplier - (leftTextureColor * edgeMultiplier + rightTextureColor * edgeMultiplier + topTextureColor * edgeMultiplier + bottomTextureColor * edgeMultiplier)), texture2D(inputImageTexture, bottomTextureCoordinate).w);\n" +
                "}"
    }
}