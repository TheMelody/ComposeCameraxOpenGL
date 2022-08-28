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
 * exposure: The adjusted exposure (-10.0 - 10.0, with 0.0 as the default)
 */
class GPUImageExposureFilter constructor(private var mExposure: Float = 0.8f) :
    GPUImageFilter(
        NO_FILTER_VERTEX_SHADER, EXPOSURE_FRAGMENT_SHADER
    ) {
    private var mExposureLocation = 0
    public override fun onInit() {
        super.onInit()
        mExposureLocation = GLES20.glGetUniformLocation(program, "exposure")
    }

    public override fun onInitialized() {
        super.onInitialized()
        setExposure(mExposure)
    }

    fun setExposure(exposure: Float) {
        mExposure = exposure
        setFloat(mExposureLocation, mExposure)
    }

    companion object {
        const val EXPOSURE_FRAGMENT_SHADER = "" +
                " varying highp vec2 textureCoordinate;\n" +
                " \n" +
                " uniform sampler2D inputImageTexture;\n" +
                " uniform highp float exposure;\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "     highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "     \n" +
                "     gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);\n" +
                " } "
    }
}