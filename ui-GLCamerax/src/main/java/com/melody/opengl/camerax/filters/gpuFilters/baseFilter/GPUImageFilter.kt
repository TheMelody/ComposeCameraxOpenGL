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
package com.melody.opengl.camerax.filters.gpuFilters.baseFilter

import com.melody.opengl.camerax.filters.gpuFilters.utils.TextureRotationUtil.getRotation
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils.loadProgram
import android.opengl.GLES20
import android.graphics.PointF
import com.melody.opengl.camerax.filters.gpuFilters.utils.OpenGlUtils
import com.melody.opengl.camerax.filters.gpuFilters.utils.Rotation
import com.melody.opengl.camerax.filters.gpuFilters.utils.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

open class GPUImageFilter constructor(
    vertexShader: String? = NO_FILTER_VERTEX_SHADER,
    fragmentShader: String? = NO_FILTER_FRAGMENT_SHADER
) {
    //用做记录操作顺序的列表
    private val mRunOnDraw: LinkedList<Runnable> = LinkedList()
    private val mVertexShader: String
    private val mFragmentShader: String
    var program //programId
            = 0
        protected set
    var attribPosition //position坐标
            = 0
        protected set
    var uniformTexture //Texture
            = 0
        protected set
    var attribTextureCoordinate //Texture坐标
            = 0
        protected set
    var intputWidth = 0
        protected set
    var intputHeight = 0
        protected set
    var isInitialized = false
        protected set
    protected var mGLCubeBuffer //顶点坐标buffer
            : FloatBuffer
    protected var mGLTextureBuffer //纹理坐标buffer
            : FloatBuffer
    protected var mOutputWidth = 0
    protected var mOutputHeight = 0
    open fun init() {
        onInit()
        this.isInitialized = true
        onInitialized()
    }

    protected open fun onInit() {
        program = loadProgram(mVertexShader, mFragmentShader)
        attribPosition = GLES20.glGetAttribLocation(program, "position")
        uniformTexture = GLES20.glGetUniformLocation(program, "inputImageTexture")
        attribTextureCoordinate = GLES20.glGetAttribLocation(
            program,
            "inputTextureCoordinate"
        )
        this.isInitialized = true
    }

    protected open fun onInitialized() {}
    fun destroy() {
        this.isInitialized = false
        GLES20.glDeleteProgram(program)
        onDestroy()
    }

    protected open fun onDestroy() {}
    open fun onInputSizeChanged(width: Int, height: Int) {
        intputWidth = width
        intputHeight = height
    }

    open fun onDrawFrame(
        textureId: Int, cubeBuffer: FloatBuffer,
        textureBuffer: FloatBuffer
    ): Int {
        GLES20.glUseProgram(program)
        runPendingOnDrawTasks()
        if (!this.isInitialized) {
            return OpenGlUtils.NOT_INIT
        }
        cubeBuffer.position(0)
        GLES20.glVertexAttribPointer(attribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer)
        GLES20.glEnableVertexAttribArray(attribPosition)
        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(
            attribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
            textureBuffer
        )
        GLES20.glEnableVertexAttribArray(attribTextureCoordinate)
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(uniformTexture, 0)
        }
        onDrawArraysPre()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(attribPosition)
        GLES20.glDisableVertexAttribArray(attribTextureCoordinate)
        onDrawArraysAfter()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return OpenGlUtils.ON_DRAWN
    }

    open fun onDrawFrame(textureId: Int): Int {
        GLES20.glUseProgram(program)
        runPendingOnDrawTasks()
        if (!this.isInitialized) return OpenGlUtils.NOT_INIT
        mGLCubeBuffer.position(0)
        GLES20.glVertexAttribPointer(attribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer)
        GLES20.glEnableVertexAttribArray(attribPosition)
        mGLTextureBuffer.position(0)
        GLES20.glVertexAttribPointer(
            attribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
            mGLTextureBuffer
        )
        GLES20.glEnableVertexAttribArray(attribTextureCoordinate)
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(uniformTexture, 0)
        }
        onDrawArraysPre()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(attribPosition)
        GLES20.glDisableVertexAttribArray(attribTextureCoordinate)
        onDrawArraysAfter()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return OpenGlUtils.ON_DRAWN
    }

    protected open fun onDrawArraysPre() {}
    protected open fun onDrawArraysAfter() {}
    protected fun runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run()
        }
    }

    protected fun setInteger(location: Int, intValue: Int) {
        runOnDraw { GLES20.glUniform1i(location, intValue) }
    }

    protected fun setFloat(location: Int, floatValue: Float) {
        runOnDraw { GLES20.glUniform1f(location, floatValue) }
    }

    protected fun setFloatVec2(location: Int, arrayValue: FloatArray?) {
        runOnDraw { GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue)) }
    }

    protected fun setFloatVec3(location: Int, arrayValue: FloatArray?) {
        runOnDraw { GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue)) }
    }

    protected fun setFloatVec4(location: Int, arrayValue: FloatArray?) {
        runOnDraw { GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue)) }
    }

    protected fun setFloatArray(location: Int, arrayValue: FloatArray) {
        runOnDraw { GLES20.glUniform1fv(location, arrayValue.size, FloatBuffer.wrap(arrayValue)) }
    }

    protected fun setPoint(location: Int, point: PointF) {
        runOnDraw {
            val vec2 = FloatArray(2)
            vec2[0] = point.x
            vec2[1] = point.y
            GLES20.glUniform2fv(location, 1, vec2, 0)
        }
    }

    protected fun setUniformMatrix3f(location: Int, matrix: FloatArray?) {
        runOnDraw { GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0) }
    }

    protected fun setUniformMatrix4f(location: Int, matrix: FloatArray?) {
        runOnDraw { GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0) }
    }

    protected fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) { mRunOnDraw.addLast(runnable) }
    }

    fun onDisplaySizeChanged(width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
    }

    companion object {
        const val NO_FILTER_VERTEX_SHADER = "" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                " \n" +
                "varying vec2 textureCoordinate;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = position;\n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}"
        const val NO_FILTER_FRAGMENT_SHADER = "" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                "uniform sampler2D inputImageTexture;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "}"
    }

    init {
        mVertexShader = vertexShader?: NO_FILTER_VERTEX_SHADER
        mFragmentShader = fragmentShader?: NO_FILTER_FRAGMENT_SHADER
        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0)
        mGLTextureBuffer =
            ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mGLTextureBuffer.put(getRotation(Rotation.NORMAL, false, true)).position(0)
    }
}