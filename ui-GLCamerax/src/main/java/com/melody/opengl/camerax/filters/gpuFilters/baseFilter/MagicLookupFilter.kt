package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.loadTexture
import com.melody.opengl.camerax.utils.GLCameraxUtils.Companion.getApplicationContext
import android.opengl.GLES20
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils

open class MagicLookupFilter(protected var table: String) :
    GPUImageFilter(NO_FILTER_VERTEX_SHADER, LOOKUP_FRAGMENT_SHADER) {
    var mLookupTextureUniform = 0
    var mLookupSourceTexture = OpenGlUtils.NO_TEXTURE
    override fun onInit() {
        super.onInit()
        mLookupTextureUniform = GLES20.glGetUniformLocation(program, "inputImageTexture2")
    }

    override fun onInitialized() {
        super.onInitialized()
        runOnDraw { mLookupSourceTexture = loadTexture(getApplicationContext(), table) }
    }

    override fun onDestroy() {
        super.onDestroy()
        val texture = intArrayOf(mLookupSourceTexture)
        GLES20.glDeleteTextures(1, texture, 0)
        mLookupSourceTexture = -1
    }

    override fun onDrawArraysAfter() {
        if (mLookupSourceTexture != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        }
    }

    override fun onDrawArraysPre() {
        if (mLookupSourceTexture != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupSourceTexture)
            GLES20.glUniform1i(mLookupTextureUniform, 3)
        }
    }

    companion object {
        const val LOOKUP_FRAGMENT_SHADER = "" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                " uniform sampler2D inputImageTexture;\n" +
                " uniform sampler2D inputImageTexture2; // lookup texture\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "     \n" +
                "     mediump float blueColor = textureColor.b * 63.0;\n" +
                "     \n" +
                "     mediump vec2 quad1;\n" +
                "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
                "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
                "     \n" +
                "     mediump vec2 quad2;\n" +
                "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
                "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
                "     \n" +
                "     highp vec2 texPos1;\n" +
                "     texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "     texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "     \n" +
                "     highp vec2 texPos2;\n" +
                "     texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "     texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "     \n" +
                "     lowp vec4 newColor1 = texture2D(inputImageTexture2, texPos1);\n" +
                "     lowp vec4 newColor2 = texture2D(inputImageTexture2, texPos2);\n" +
                "     \n" +
                "     lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
                "     gl_FragColor = vec4(newColor.rgb, textureColor.w);\n" +
                " }"
    }
}