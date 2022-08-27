package com.melody.opengl.camerax.filters

import android.content.res.Resources
import android.opengl.GLES20

class NoneFilter(res: Resources) : BaseFilter(res) {
    override fun onCreate() {
        createProgramByAssetsFile(
            "shader/base_vertex.sh",
            "shader/base_fragment.sh"
        )
    }

    /**
     * 背景默认为黑色
     */
    override fun onClear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    override fun onSizeChanged(width: Int, height: Int) {}
}