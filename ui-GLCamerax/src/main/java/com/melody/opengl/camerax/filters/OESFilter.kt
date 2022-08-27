package com.melody.opengl.camerax.filters

import android.content.res.Resources
import android.opengl.GLES20
import android.opengl.GLES11Ext

open class OESFilter(mRes: Resources) : BaseFilter(mRes) {
    override fun onCreate() {
        createProgramByAssetsFile("shader/oes_base_vertex.sh", "shader/oes_base_fragment.sh")
    }

    override fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(hTexture, textureType)
    }

    override fun onSizeChanged(width: Int, height: Int) {}
}