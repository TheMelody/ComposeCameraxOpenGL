package com.melody.opengl.camerax.draw

import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.MotionEvent
import com.melody.opengl.camerax.encoder.TextureMovieEncoder
import com.melody.opengl.camerax.encoder.TexturePictureEncoder
import com.melody.opengl.camerax.filters.*
import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.MagicBeautyFilter2
import com.melody.opengl.camerax.model.IListenerRecordTime
import com.melody.opengl.camerax.utils.EasyGlUtils.bindFrameTexture
import com.melody.opengl.camerax.utils.EasyGlUtils.unBindFrameBuffer
import com.melody.opengl.camerax.utils.GLCameraxUtils
import com.melody.opengl.camerax.utils.MatrixUtils.flip
import com.melody.opengl.camerax.utils.MatrixUtils.getShowMatrix
import com.melody.opengl.camerax.utils.MatrixUtils.originalMatrix
import com.melody.opengl.camerax.widget.SlideGpuFilterGroup
import com.melody.opengl.camerax.widget.SlideGpuFilterGroup.OnFilterChangeListener
import jp.co.cyberagent.android.gpuimage.GLTextureView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraDrawer(resources: Resources) : GLTextureView.Renderer, IListenerRecordTime {
    private val OM: FloatArray

    /**显示画面的filter */
    private val showFilter: BaseFilter

    /**后台绘制的filter */
    private val drawFilter: BaseFilter

    /**绘制水印的filter组 */
    private val mBeFilter: GroupFilter
    private val mAfFilter: GroupFilter

    /**用于绘制美白效果的filter */
    private val mProcessFilter: BaseFilter

    /**美白的filter */
    private val mBeautyFilter: MagicBeautyFilter2?

    /**多种滤镜切换 */
    private val mSlideFilterGroup: SlideGpuFilterGroup

    /**
     * 视频录制显示当前的时间
     */
    private val _recordingTimeState = MutableStateFlow("00:00:00")
    val recordingTimeState: StateFlow<String>
        get() = _recordingTimeState.asStateFlow()

    var texture: SurfaceTexture? = null
        private set

    /**预览数据的宽高 */
    private var mPreviewWidth = 0
    private var mPreviewHeight = 0

    /**控件的宽高 */
    private var width = 0
    private var height = 0

    /**
     * 视频编码器
     */
    private var videoEncoder: TextureMovieEncoder? = null

    /**
     * 照片编码器
     */
    private var pictureEncoder: TexturePictureEncoder? = null

    /**
     * 点击拍视频
     */
    var recordingEnabled = false
        private set

    /**
     * 点击拍照片
     */
    var takePictureEnabled = false
        private set

    private var recordingStatus = 0
    private var savePath: String? = null
    private var textureID = 0
    private val fFrame = IntArray(1)
    private val fTexture = IntArray(1)
    private val SM = FloatArray(16) //用于显示的变换矩阵
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textureID = createTextureID()
        texture = SurfaceTexture(textureID)
        drawFilter.create()
        drawFilter.textureId = textureID
        mProcessFilter.create()
        showFilter.create()
        mBeFilter.create()
        mAfFilter.create()
        mBeautyFilter?.init()
        mSlideFilterGroup.init()
        recordingStatus = if (recordingEnabled) {
            RECORDING_RESUMED
        } else {
            RECORDING_OFF
        }
    }

    /**创建显示的texture */
    private fun createTextureID(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0) //第一个参数表示创建几个纹理对象，并将创建好的纹理对象放置到第二个参数中去
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        //清除遗留的
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
        /**创建一个帧染缓冲区对象 */
        GLES20.glGenFramebuffers(1, fFrame, 0)
        /**根据纹理数量 返回的纹理索引 */
        GLES20.glGenTextures(1, fTexture, 0)
        /**将生产的纹理名称和对应纹理进行绑定 */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0])
        /**根据指定的参数 生产一个2D的纹理 调用该函数前  必须调用glBindTexture以指定要操作的纹理 */
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mPreviewWidth, mPreviewHeight,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        useTexParameter()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        mProcessFilter.setSize(mPreviewWidth, mPreviewHeight)
        mBeFilter.setSize(mPreviewWidth, mPreviewHeight)
        mAfFilter.setSize(mPreviewWidth, mPreviewHeight)
        drawFilter.setSize(mPreviewWidth, mPreviewHeight)
        mBeautyFilter?.onDisplaySizeChanged(mPreviewWidth, mPreviewHeight)
        mBeautyFilter?.onInputSizeChanged(mPreviewWidth, mPreviewHeight)
        mSlideFilterGroup.onSizeChanged(mPreviewWidth, mPreviewHeight)
        getShowMatrix(SM, mPreviewWidth, mPreviewHeight, width, height)
        showFilter.matrix = SM
    }

    override fun onDrawFrame(gl: GL10?) {
        /**更新界面中的数据 */
        texture?.updateTexImage()
        bindFrameTexture(fFrame[0], fTexture[0])
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight)
        drawFilter.draw()
        unBindFrameBuffer()
        mBeFilter.textureId = fTexture[0]
        mBeFilter.draw()
        if (mBeautyFilter != null && mBeautyFilter.beautyFeatureEnable) {
            mBeautyFilter.onDrawFrame(mBeFilter.outputTexture)
            mProcessFilter.textureId = fTexture[0]
        } else {
            mProcessFilter.textureId = mBeFilter.outputTexture
        }
        mProcessFilter.draw()
        mSlideFilterGroup.onDrawFrame(mProcessFilter.outputTexture)
        mAfFilter.textureId = mSlideFilterGroup.outputTexture
        mAfFilter.draw()
        recording()
        /*绘制显示的filter*/
        GLES20.glViewport(0, 0, width, height)
        showFilter.textureId = mAfFilter.outputTexture
        showFilter.draw()
        if (recordingEnabled && recordingStatus == RECORDING_ON) {
            videoEncoder?.setTextureId(mAfFilter.outputTexture)
            videoEncoder?.frameAvailable(texture)
        } else if(takePictureEnabled) {
            takePictureEnabled = false
            pictureEncoder?.setTextureId(mAfFilter.outputTexture)
            pictureEncoder?.frameAvailable(texture)
        }
    }

    private fun recording() {
        if (takePictureEnabled) {
            pictureEncoder = TexturePictureEncoder()
            pictureEncoder?.setPreviewSize(mPreviewWidth, mPreviewHeight)
            pictureEncoder?.startCapture(
                TexturePictureEncoder.EncoderConfig(
                    path = savePath ?: "",
                    mWidth = mPreviewWidth,
                    mHeight = mPreviewHeight,
                    mBitRate = -1,
                    mEglContext = EGL14.eglGetCurrentContext()
                )
            )
        } else {
            if (recordingEnabled) {
                /*说明是录制状态*/
                when (recordingStatus) {
                    RECORDING_OFF -> {
                        videoEncoder = TextureMovieEncoder()
                        videoEncoder?.setRecordTimeListener(this)
                        videoEncoder?.setPreviewSize(mPreviewWidth, mPreviewHeight)
                        videoEncoder?.startRecording(
                            TextureMovieEncoder.EncoderConfig(
                                savePath?:"", mPreviewWidth, mPreviewHeight,
                                3500000, EGL14.eglGetCurrentContext()
                            )
                        )
                        recordingStatus = RECORDING_ON
                    }
                    RECORDING_RESUMED -> {
                        videoEncoder?.updateSharedContext(EGL14.eglGetCurrentContext())
                        videoEncoder?.resumeRecording()
                        recordingStatus = RECORDING_ON
                    }
                    RECORDING_ON, RECORDING_PAUSED -> {}
                    RECORDING_PAUSE -> {
                        videoEncoder?.pauseRecording()
                        recordingStatus = RECORDING_PAUSED
                    }
                    RECORDING_RESUME -> {
                        videoEncoder?.resumeRecording()
                        recordingStatus = RECORDING_ON
                    }
                    else -> throw RuntimeException("unknown recording status $recordingStatus")
                }
            } else {
                when (recordingStatus) {
                    RECORDING_ON, RECORDING_RESUMED, RECORDING_PAUSE, RECORDING_RESUME, RECORDING_PAUSED -> {
                        videoEncoder?.stopRecording()
                        recordingStatus = RECORDING_OFF
                        videoEncoder = null
                    }
                    RECORDING_OFF -> {}
                    else -> throw RuntimeException("unknown recording status $recordingStatus")
                }
            }
        }
    }

    /**设置预览效果的size */
    fun setPreviewSize(width: Int, height: Int) {
        if (mPreviewWidth != width || mPreviewHeight != height) {
            mPreviewWidth = width
            mPreviewHeight = height
        }
    }

    /**提供修改美白等级的接口 */
    fun changeBeautyLevel(level: Int) {
        mBeautyFilter?.beautyLevel = level.toFloat()
    }

    val beautyLevel: Float
        get() = mBeautyFilter?.beautyLevel?:0F

    /**根据摄像头设置纹理映射坐标 */
    fun setCameraId(id: Int) {
        drawFilter.flag = id
    }

    fun isRecording() = videoEncoder?.isRecording ?: false

    fun startRecord() {
        recordingEnabled = true
    }

    fun stopRecord() {
        recordingEnabled = false
    }

    fun startCapture() {
        takePictureEnabled = true
    }

    fun setSavePath(path: String?) {
        savePath = path
    }

    fun onPause(auto: Boolean) {
        if (auto) {
            videoEncoder?.pauseRecording()
            if (recordingStatus == RECORDING_ON) {
                recordingStatus = RECORDING_PAUSED
            }
            return
        }
        if (recordingStatus == RECORDING_ON) {
            recordingStatus = RECORDING_PAUSE
        }
    }

    fun onResume(auto: Boolean) {
        if (auto) {
            if (recordingStatus == RECORDING_PAUSED) {
                recordingStatus = RECORDING_RESUME
            }
            return
        }
        if (recordingStatus == RECORDING_PAUSED) {
            recordingStatus = RECORDING_RESUME
        }
    }

    private fun useTexParameter() {
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
    }

    /**
     * 触摸事件的传递
     */
    fun onTouch(event: MotionEvent) {
        mSlideFilterGroup.onTouchEvent(event)
    }

    /**
     * 滤镜切换的事件监听
     */
    fun setOnFilterChangeListener(listener: OnFilterChangeListener?) {
        mSlideFilterGroup.setOnFilterChangeListener(listener)
    }

    companion object {
        private const val RECORDING_OFF = 0
        private const val RECORDING_ON = 1
        private const val RECORDING_RESUMED = 2
        private const val RECORDING_PAUSE = 3
        private const val RECORDING_RESUME = 4
        private const val RECORDING_PAUSED = 5
    }

    init {
        // 初始化一个滤镜控制器
        showFilter = NoneFilter(resources)
        drawFilter = CameraFilter(resources)
        mProcessFilter = CameraDrawProcessFilter(resources)
        mBeFilter = GroupFilter(resources)
        mAfFilter = GroupFilter(resources)
        mBeautyFilter = MagicBeautyFilter2()
        mSlideFilterGroup = SlideGpuFilterGroup()
        OM = originalMatrix
        // 矩阵上下翻转
        flip(OM, false, false)
        showFilter.matrix = OM
    }

    /**
     * 通过回调获取TextureMovieEncoder里面当前视频拍摄的时间
     */
    override fun onUpdateRecordTime(time: Long) {
        _recordingTimeState.value = GLCameraxUtils.getTimeInfoFromNanoTime(time)
    }

    fun release() {
        setOnFilterChangeListener(null)
        videoEncoder?.setRecordTimeListener(null)
        pictureEncoder?.releaseEncoder()
        pictureEncoder = null
        videoEncoder = null
    }
}