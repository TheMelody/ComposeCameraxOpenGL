package com.melody.opengl.camerax.utils

import android.opengl.Matrix

object MatrixUtils {
    /**矩阵操作的类型 */
    private const val TYPE_FITXY = 0
    private const val TYPE_CENTERCROP = 1
    private const val TYPE_CENTERINSIDE = 2
    private const val TYPE_FITSTART = 3
    private const val TYPE_FITEND = 4

    @JvmStatic
    fun getShowMatrix(
        matrix: FloatArray?,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val sWhView = viewWidth.toFloat() / viewHeight
            val sWhImg = imgWidth.toFloat() / imgHeight
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (sWhImg > sWhView) {
                Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1f, 1f, 1f, 3f)
            } else {
                Matrix.orthoM(projection, 0, -1f, 1f, -sWhImg / sWhView, sWhImg / sWhView, 1f, 3f)
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }

    fun getMatrix(
        matrix: FloatArray?, type: Int, imgWidth: Int, imgHeight: Int, viewWidth: Int,
        viewHeight: Int
    ) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (type == TYPE_FITXY) {
                Matrix.orthoM(projection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
                Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
            }
            val sWhView = viewWidth.toFloat() / viewHeight
            val sWhImg = imgWidth.toFloat() / imgHeight
            if (sWhImg > sWhView) {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection,
                        0,
                        -sWhView / sWhImg,
                        sWhView / sWhImg,
                        -1f,
                        1f,
                        1f,
                        3f
                    )
                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -sWhImg / sWhView,
                        sWhImg / sWhView,
                        1f,
                        3f
                    )
                    TYPE_FITSTART -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        1 - 2 * sWhImg / sWhView,
                        1f,
                        1f,
                        3f
                    )
                    TYPE_FITEND -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -1f,
                        2 * sWhImg / sWhView - 1,
                        1f,
                        3f
                    )
                }
            } else {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -sWhImg / sWhView,
                        sWhImg / sWhView,
                        1f,
                        3f
                    )
                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection,
                        0,
                        -sWhView / sWhImg,
                        sWhView / sWhImg,
                        -1f,
                        1f,
                        1f,
                        3f
                    )
                    TYPE_FITSTART -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        2 * sWhView / sWhImg - 1,
                        -1f,
                        1f,
                        1f,
                        3f
                    )
                    TYPE_FITEND -> Matrix.orthoM(
                        projection,
                        0,
                        1 - 2 * sWhView / sWhImg,
                        1f,
                        -1f,
                        1f,
                        1f,
                        3f
                    )
                }
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }

    fun getCenterInsideMatrix(
        matrix: FloatArray?,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val sWhView = viewWidth.toFloat() / viewHeight
            val sWhImg = imgWidth.toFloat() / imgHeight
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (sWhImg > sWhView) {
                Matrix.orthoM(projection, 0, -1f, 1f, -sWhImg / sWhView, sWhImg / sWhView, 1f, 3f)
            } else {
                Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1f, 1f, 1f, 3f)
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }

    fun rotate(m: FloatArray, angle: Float): FloatArray {
        Matrix.rotateM(m, 0, angle, 0f, 0f, 1f)
        return m
    }

    @JvmStatic
    fun flip(m: FloatArray, x: Boolean, y: Boolean): FloatArray {
        if (x || y) {
            Matrix.scaleM(
                m, 0,
                (if (x) -1 else 1.toFloat()) as Float, (if (y) -1 else 1.toFloat()) as Float, 1f
            )
        }
        return m
    }

    fun scale(m: FloatArray, x: Float, y: Float): FloatArray {
        Matrix.scaleM(m, 0, x, y, 1f)
        return m
    }

    @JvmStatic
    val originalMatrix: FloatArray
        get() = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
}