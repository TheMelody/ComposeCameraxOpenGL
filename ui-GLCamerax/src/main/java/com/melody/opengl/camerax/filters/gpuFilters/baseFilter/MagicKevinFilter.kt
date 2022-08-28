package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.readShaderFromRawResource
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.loadTexture
import com.melody.opengl.camerax.utils.GLCameraxUtils.Companion.getApplicationContext
import com.melody.opengl.camerax.R
import android.opengl.GLES20
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils
import java.util.*

class MagicKevinFilter :
    GPUImageFilter(NO_FILTER_VERTEX_SHADER, readShaderFromRawResource(R.raw.kevin_new)) {
    private val inputTextureHandles = intArrayOf(-1)
    private val inputTextureUniformLocations = intArrayOf(-1)
    private var mGLStrengthLocation = 0
    override fun onDestroy() {
        super.onDestroy()
        GLES20.glDeleteTextures(1, inputTextureHandles, 0)
        Arrays.fill(inputTextureHandles, -1)
    }

    override fun onDrawArraysAfter() {
        var i = 0
        while (i < inputTextureHandles.size
            && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE
        ) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i + 3))
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            i++
        }
    }

    override fun onDrawArraysPre() {
        var i = 0
        while (i < inputTextureHandles.size
            && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE
        ) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i + 3))
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i])
            GLES20.glUniform1i(inputTextureUniformLocations[i], i + 3)
            i++
        }
    }

    override fun onInit() {
        super.onInit()
        for (i in inputTextureUniformLocations.indices) inputTextureUniformLocations[i] =
            GLES20.glGetUniformLocation(program, "inputImageTexture" + (2 + i))
        mGLStrengthLocation = GLES20.glGetUniformLocation(
            program,
            "strength"
        )
    }

    override fun onInitialized() {
        super.onInitialized()
        setFloat(mGLStrengthLocation, 1.0f)
        runOnDraw {
            inputTextureHandles[0] = loadTexture(getApplicationContext(), "filter/kelvinmap.png")
        }
    }
}