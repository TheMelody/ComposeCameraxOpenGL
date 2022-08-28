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
 * brightness value ranges from -1.0 to 1.0, with 0.0 as the normal level
 */
class GPUImageBrightnessFilter constructor(private var mBrightness: Float = 0.5f) :
    GPUImageFilter(
        NO_FILTER_VERTEX_SHADER, BRIGHTNESS_FRAGMENT_SHADER
    ) {
    private var mBrightnessLocation = 0
    public override fun onInit() {
        super.onInit()
        mBrightnessLocation = GLES20.glGetUniformLocation(program, "brightness")
    }

    public override fun onInitialized() {
        super.onInitialized()
        setBrightness(mBrightness)
    }

    fun setBrightness(brightness: Float) {
        mBrightness = brightness
        setFloat(mBrightnessLocation, mBrightness)
    }

    companion object {
        const val BRIGHTNESS_FRAGMENT_SHADER = "" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                " uniform sampler2D inputImageTexture;\n" +
                " uniform lowp float brightness;\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "     \n" +
                "     gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);\n" +
                " }"
    }
}