package com.melody.opengl.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Process
import android.util.Size
import android.view.MotionEvent
import android.view.Surface
import androidx.annotation.WorkerThread
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import com.melody.opengl.camerax.draw.CameraDrawer
import com.melody.opengl.camerax.widget.SlideGpuFilterGroup
import jp.co.cyberagent.android.gpuimage.GLTextureView
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@SuppressLint("ViewConstructor")
class GLCameraView(context: Context, private val ratio: Float) : GLTextureView(context),
    GLTextureView.Renderer, SurfaceProvider, SurfaceTexture.OnFrameAvailableListener {
    private val executor = Executors.newSingleThreadExecutor{ r ->
        object : Thread(r, "GLCameraViewPool") {
            override fun run() {
                // 统一使用后台线程的优先级，防止 "卡前台" 的问题出现
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                super.run()
            }
        }
    }

    private val mCameraDrawer:CameraDrawer = CameraDrawer(context.resources)

    /**
     * 是否来自前后摄像头切换
     */
    private var fromCameraLensFacing:Boolean = false

    /**
     * 左右水平滑动切换滤镜功能是否可用
     */
    var slideGpuFilterGroupEnable: Boolean = true

    // TODO:目前切换摄像头，画面会颠倒，暂时没有处理好
    fun switchCameraLensFacing(fromCameraLensFacing:Boolean, lensFacing:Int) {
        this.fromCameraLensFacing = fromCameraLensFacing
        if(fromCameraLensFacing) {
            mCameraDrawer.setCameraId(lensFacing)
        }
    }

    init {
        // 初始化OpenGL的相关信息
        // 设置版本
        setEGLContextClientVersion(2)
        // 设置Render
        setRenderer(this)
        // 主动调用渲染
        renderMode = RENDERMODE_WHEN_DIRTY
        // 保存Context当pause时
        preserveEGLContextOnPause = true
        // 相机距离
        cameraDistance = 100F
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mCameraDrawer.onSurfaceCreated(gl,config)
        mCameraDrawer.setPreviewSize(resources.displayMetrics.widthPixels,
            (resources.displayMetrics.widthPixels/ratio).toInt()
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mCameraDrawer.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        mCameraDrawer.onDrawFrame(gl)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    override fun onSurfaceRequested(request: SurfaceRequest) {
        val resetTexture = resetPreviewTexture(request.resolution) ?: return
        val surface = Surface(resetTexture)
        request.provideSurface(surface, executor) {
            surface.release()
            if(!fromCameraLensFacing) {
                // 注意：切换前置和后置摄像头的时候，不能release surfaceTexture
                surfaceTexture?.release()
            }
        }
    }


    @WorkerThread
    private fun resetPreviewTexture(size: Size): SurfaceTexture? {
        mCameraDrawer.texture?.setOnFrameAvailableListener(this)
        mCameraDrawer.texture?.setDefaultBufferSize(size.width, size.height)
        return mCameraDrawer.texture
    }

    fun getBeautyLevel(): Float {
        return mCameraDrawer.beautyLevel
    }

    fun changeBeautyLevel(level: Int) {
        queueEvent { mCameraDrawer.changeBeautyLevel(level) }
    }

    fun takeVideo(path:String?){
        if(mCameraDrawer.recordingEnabled) {
            stopRecord()
        } else {
            startRecord(path)
        }
    }

    fun takePicture(path: String?) {
        if(!mCameraDrawer.takePictureEnabled) {
            mCameraDrawer.setSavePath(path)
            mCameraDrawer.startCapture()
        }
    }

    private fun startRecord(path:String?) {
        mCameraDrawer.setSavePath(path)
        mCameraDrawer.startRecord()
    }

    private fun stopRecord() {
        mCameraDrawer.stopRecord()
    }

    fun isRecording() = mCameraDrawer.isRecording()

    fun getRecordingTimeState() = mCameraDrawer.recordingTimeState

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(null != event && !mCameraDrawer.recordingEnabled && slideGpuFilterGroupEnable){
            queueEvent { mCameraDrawer.onTouch(event) }
        }
        return true
    }

    fun setOnFilterChangeListener(listener: SlideGpuFilterGroup.OnFilterChangeListener?) {
        mCameraDrawer.setOnFilterChangeListener(listener)
    }

    override fun onDetachedFromWindow() {
        mCameraDrawer.release()
        super.onDetachedFromWindow()
    }

}