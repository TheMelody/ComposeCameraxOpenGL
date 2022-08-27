package com.melody.opengl.camerax.filters

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils

/**
 * 水印的Filter
 */
class WaterMarkFilter : NoFilter {
    private var x = 0
    private var y = 0
    private var w = 0
    private var h = 0
    private var width = 0
    private var height = 0
    private var mBitmap: Bitmap? = null
    private var mGifBitmap: Bitmap? = null
    private var mFilter: NoFilter
    var mMatrix: Matrix? = null
    var mGifDecoder: GifDecoder? = null

    constructor(mRes: Resources?) : super(mRes) {
        mFilter = object : NoFilter(mRes) {
            override fun onClear() {}
        }
    }

    private var mIsGif = false
    private var mGifId = 0
    private var mRotateDegree = 0
    private var mResources: Resources? = null

    constructor(res: Resources?, isGif: Boolean, bitRes: Int, rotateDegree: Float) : super(res) {
        mResources = res
        mGifId = bitRes
        mIsGif = isGif
        mRotateDegree = rotateDegree.toInt()
        mFilter = object : NoFilter(mRes) {
            override fun onClear() {}
        }
    }

    fun setWaterMark(bitmap: Bitmap?) {
        if (mBitmap != null && mBitmap?.isRecycled == false) {
            mBitmap?.recycle()
            mBitmap = null
        }
        if (mGifBitmap != null && mGifBitmap?.isRecycled == false) {
            mGifBitmap?.recycle()
            mGifBitmap = null
        }
        mBitmap = bitmap
    }

    private var mStartTime: Long = 0
    private var mEndTime: Long = 0
    fun setShowTime(startTime: Long, endTime: Long) {
        mStartTime = startTime
        mEndTime = endTime
    }

    private val mRotationMatrix = FloatArray(16)
    override fun draw() {
        super.draw()
        GLES20.glViewport(
            x,
            y,
            if (w == 0) mBitmap!!.width else w,
            if (h == 0) mBitmap!!.height else h
        )
        blendFunc()
        mFilter.draw()
    }

    override fun draw(time: Long) {
        super.draw()
        if (mIsGif) {
            createTexture()
        }
        if (time > mStartTime && time < mEndTime) {
            val i = (mBitmap!!.width * 1.15).toInt()
            val i1 = (mBitmap!!.height * 1.15).toInt()
            GLES20.glViewport(x, y, if (w == 0) i else w, if (h == 0) i1 else h)
            blendFunc()
            mFilter.draw()
        }
    }

    private fun blendFunc() {
        GLES20.glEnable(GLES20.GL_BLEND)
        //      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(
            GLES20.GL_ONE,
            GLES20.GL_ONE_MINUS_SRC_ALPHA
        ) //使用这个混合算法可以合成带透明度的贴纸。参考：https://www.jianshu.com/p/2fb9d90b57f0
    }

    override fun onCreate() {
        super.onCreate()
        mFilter.create()
        if (mIsGif) {
            mGifDecoder = GifDecoder()
            val inputStream = mResources!!.openRawResource(mGifId)
            mGifDecoder!!.read(inputStream)
            mMatrix = Matrix()
            mMatrix!!.postRotate(mRotateDegree.toFloat())
        }
        createTexture()
    }

    private val textures = IntArray(1)
    private fun createTexture() {
        if (mBitmap != null) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            if (!mIsGif) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
            } else {
                mGifBitmap = mGifDecoder?.nextBitmap()
                if (mGifBitmap != null) {
                    GLUtils.texImage2D(
                        GLES20.GL_TEXTURE_2D, 0, Bitmap.createBitmap(
                            mGifBitmap!!,
                            0,
                            0,
                            mGifBitmap!!.width,
                            mGifBitmap!!.height,
                            mMatrix,
                            true
                        ), 0
                    )
                }
            }
            //对画面进行矩阵旋转
//            MatrixUtils.flip(mFilter.getMatrix(),false,true);
            mFilter.textureId = textures[0]
        }
    }

    override fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        mFilter.setSize(width,height)
    }

    fun setPosition(x: Int, y: Int, width: Int, height: Int) {
        this.x = x
        this.y = y
        w = width
        h = height
    }
}