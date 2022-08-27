package com.melody.opengl.camerax.filters

import android.content.res.Resources
import com.melody.opengl.camerax.utils.MatrixUtils.originalMatrix
import android.util.SparseArray
import android.opengl.GLES20
import android.util.Log
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseFilter(var res: Resources) {
    /**
     * 程序句柄
     */
    private var program = 0

    /**
     * 顶点坐标句柄
     */
    private var hPosition = 0

    /**
     * 纹理坐标句柄
     */
    private var hCoord = 0

    /**
     * 总变换矩阵句柄
     */
    private var hMatrix = 0

    /**
     * 默认纹理贴图句柄
     */
    var hTexture = 0
        private set

    /**
     * 顶点坐标Buffer
     */
    private var verBuffer: FloatBuffer? = null

    /**
     * 纹理坐标Buffer
     */
    var texBuffer: FloatBuffer? = null

    /**
     * 索引坐标Buffer
     */
    open var flag = 0

    var matrix: FloatArray = oM.copyOf(16)

    // 默认使用Texture2D0
    val textureType = 0
    var textureId = 0

    //顶点坐标
    private val pos = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, 1.0f,
        1.0f, -1.0f
    )

    //纹理坐标
    private val coord = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )
    
    var bools: SparseArray<BooleanArray>? = null
        private set

    var ints: SparseArray<IntArray>? = null
        private set

    var floats: SparseArray<FloatArray>? = null
        private set

    /**
     * Buffer初始化
     */
    private fun initBuffer() {
        val a = ByteBuffer.allocateDirect(32)
        a.order(ByteOrder.nativeOrder())
        verBuffer = a.asFloatBuffer()
        verBuffer?.put(pos)
        verBuffer?.position(0)
        val b = ByteBuffer.allocateDirect(32)
        b.order(ByteOrder.nativeOrder())
        texBuffer = b.asFloatBuffer()
        texBuffer?.put(coord)
        texBuffer?.position(0)
    }

    /**
     * 清除画布
     */
    protected open fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    protected fun createProgramByAssetsFile(vertex: String?, fragment: String?) {
        createProgram(uRes(res, vertex), uRes(res, fragment))
    }

    protected fun createProgram(vertex: String?, fragment: String?) {
        program = uCreateGlProgram(vertex, fragment)
        hPosition = GLES20.glGetAttribLocation(program, "vPosition")
        hCoord = GLES20.glGetAttribLocation(program, "vCoord")
        hMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
        hTexture = GLES20.glGetUniformLocation(program, "vTexture")
    }
    //2
    /**
     * 绑定默认纹理
     */
    protected open fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(hTexture, textureType)
    }

    open val outputTexture: Int
        get() = -1

    open fun draw() {
        onClear()
        onUseProgram()
        onSetExpandData()
        onBindTexture()
        onDraw()
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(program)
    }

    /**
     * 设置其他扩展数据
     */
    protected fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(hMatrix, 1, false, matrix, 0)
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected fun onDraw() {
        GLES20.glEnableVertexAttribArray(hPosition)
        GLES20.glVertexAttribPointer(hPosition, 2, GLES20.GL_FLOAT, false, 0, verBuffer)
        GLES20.glEnableVertexAttribArray(hCoord)
        GLES20.glVertexAttribPointer(hCoord, 2, GLES20.GL_FLOAT, false, 0, texBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(hPosition)
        GLES20.glDisableVertexAttribArray(hCoord)
    }

    fun setSize(width: Int, height: Int) {
        onSizeChanged(width, height)
    }

    //========================================
    fun create() {
        onCreate()
    }

    protected abstract fun onCreate()
    protected abstract fun onSizeChanged(width: Int, height: Int)

    companion object {
        //============================================Getting And Setting
        const val tAG = "BaseFilter"

        /**
         * 单位矩阵
         */
        val oM = originalMatrix

        //通过路径加载Assets中的文本内容
        fun uRes(mRes: Resources, path: String?): String? {
            val result = StringBuilder()
            try {
                val `is` = mRes.assets.open(path!!)
                var ch: Int
                val buffer = ByteArray(1024)
                while (-1 != `is`.read(buffer).also { ch = it }) {
                    result.append(String(buffer, 0, ch))
                }
            } catch (e: Exception) {
                return null
            }
            return result.toString().replace("\\r\\n".toRegex(), "\n")
        }

        //创建GL程序
        fun uCreateGlProgram(vertexSource: String?, fragmentSource: String?): Int {
            val vertex = uLoadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            if (vertex == 0) return 0
            val fragment = uLoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragment == 0) return 0
            var program = GLES20.glCreateProgram()
            if (program != 0) {
                GLES20.glAttachShader(program, vertex)
                GLES20.glAttachShader(program, fragment)
                GLES20.glLinkProgram(program)
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    glError(1, "Could not link program:" + GLES20.glGetProgramInfoLog(program))
                    GLES20.glDeleteProgram(program)
                    program = 0
                }
            }
            return program
        }

        /**加载shader */
        fun uLoadShader(shaderType: Int, source: String?): Int {
            var shader = GLES20.glCreateShader(shaderType)
            if (0 != shader) {
                GLES20.glShaderSource(shader, source)
                GLES20.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    glError(1, "Could not compile shader:$shaderType")
                    glError(1, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader))
                    GLES20.glDeleteShader(shader)
                    shader = 0
                }
            }
            return shader
        }

        fun glError(code: Int, index: Any) {
            if (code != 0) {
                Log.e(tAG, "glError:$code---$index")
            }
        }
    }

    init {
        initBuffer()
    }
}