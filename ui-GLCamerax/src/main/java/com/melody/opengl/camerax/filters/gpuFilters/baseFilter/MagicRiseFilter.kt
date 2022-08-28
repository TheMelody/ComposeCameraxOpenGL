package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.readShaderFromRawResource
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.loadTexture
import com.melody.opengl.camerax.utils.GLCameraxUtils.Companion.getApplicationContext
import com.melody.opengl.camerax.R
import android.opengl.GLES20
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils
import java.util.*

class MagicRiseFilter :
    GPUImageFilter(NO_FILTER_VERTEX_SHADER, readShaderFromRawResource(R.raw.rise)) {
    private val inputTextureHandles = intArrayOf(-1, -1, -1)
    private val inputTextureUniformLocations = intArrayOf(-1, -1, -1)
    public override fun onDestroy() {
        super.onDestroy()
        GLES20.glDeleteTextures(inputTextureHandles.size, inputTextureHandles, 0)
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

    public override fun onInit() {
        super.onInit()
        for (i in inputTextureUniformLocations.indices) {
            inputTextureUniformLocations[i] =
                GLES20.glGetUniformLocation(program, "inputImageTexture" + (2 + i))
        }
    }

    public override fun onInitialized() {
        super.onInitialized()
        runOnDraw {
            inputTextureHandles[0] =
                loadTexture(getApplicationContext(), "filter/blackboard1024.png")
            inputTextureHandles[1] = loadTexture(getApplicationContext(), "filter/overlaymap.png")
            inputTextureHandles[2] = loadTexture(getApplicationContext(), "filter/risemap.png")
        }
    }
}