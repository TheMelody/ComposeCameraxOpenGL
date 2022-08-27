package com.melody.opengl.camerax.encoder

import com.melody.opengl.camerax.encoder.gles.EglCore
import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.MagicCameraInputFilter
import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.setThreadPriority
import android.util.Log
import com.melody.opengl.camerax.filters.BaseFilter
import com.melody.opengl.camerax.filters.NoneFilter
import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.GPUImageFilter
import com.melody.opengl.camerax.model.IListenerRecordTime
import com.melody.opengl.camerax.utils.GLCameraxUtils
import java.io.IOException
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.nio.FloatBuffer
import java.util.concurrent.Executors

class TextureMovieEncoder : Runnable {
    // ----- accessed exclusively by encoder thread -----
    private var mInputWindowSurface: WindowSurface? = null
    private var mEglCore: EglCore? = null
    private var mInput: MagicCameraInputFilter? = null
    private var mTextureId = 0
    private var mVideoEncoder: VideoEncoderCore? = null

    // ----- accessed by multiple threads -----
    @Volatile
    private var mHandler: EncoderHandler? = null
    private val mReadyFence = Object() // guards ready/running
    private var mReady = false
    private var mRunning = false
    private var filter: GPUImageFilter? = null
    private var gLCubeBuffer: FloatBuffer? = null
    private var gLTextureBuffer: FloatBuffer? = null
    private var baseTimeStamp: Long = -1 //第一帧的时间戳

    private var mIListenerRecordTime: IListenerRecordTime? = null

    fun setRecordTimeListener(listener:IListenerRecordTime?) {
        mIListenerRecordTime = listener
    }


    /**
     * Encoder configuration.
     *
     *
     * Object is immutable, which means we can safely pass it between threads without
     * explicit synchronization (and don't need to worry about it getting tweaked out from
     * under us).
     *
     *
     * TODO: make frame rate and iframe interval configurable?  Maybe use builder pattern
     * with reasonable defaults for those and bit rate.
     */
    class EncoderConfig(
        val path: String, val mWidth: Int, val mHeight: Int, val mBitRate: Int,
        val mEglContext: EGLContext
    ) {
        override fun toString(): String {
            return "EncoderConfig: " + mWidth + "x" + mHeight + " @" + mBitRate +
                    " to '" + path + "' ctxt=" + mEglContext
        }
    }

    /**
     * Tells the video recorder to start recording.  (Call from non-encoder thread.)
     *
     *
     * Creates a new thread, which will create an encoder using the provided configuration.
     *
     *
     * Returns after the recorder thread has started and is ready to accept Messages.  The
     * encoder may not yet be fully configured.
     */
    fun startRecording(config: EncoderConfig?) {
        Log.d(TAG, "Encoder: startRecording()")
        synchronized(mReadyFence) {
            if (mRunning) {
                Log.w(TAG, "Encoder thread already running")
                return
            }
            mRunning = true
            Thread(this, "TextureMovieEncoder").start()
            while (!mReady) {
                try {
                    mReadyFence.wait()
                } catch (ie: InterruptedException) {
                    // ignore
                }
            }
        }
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_START_RECORDING, config))
        }
    }

    /**
     * Tells the video recorder to stop recording.  (Call from non-encoder thread.)
     *
     *
     * Returns immediately; the encoder/muxer may not yet be finished creating the movie.
     *
     */
    fun stopRecording() {
        // looper.quit
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_QUIT))
        }
        // 需要将耗时操作移动到gl线程外面，否则OpenGL ES所在的线程被阻塞或者被挂起，导致渲染设备上下文丢失（直接表现就是：画面不会再动，无法继续绘制）
        Executors.newSingleThreadExecutor { r ->
            object : Thread(r, "StopRecordingPool") {
                override fun run() {
                    setThreadPriority(THREAD_PRIORITY_BACKGROUND)
                    super.run()
                }
            }
        }.apply {
            submit {
                handleStopRecording()
            }
            shutdown()
        }
    }

    fun pauseRecording() {
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_PAUSE))
        }
    }

    fun resumeRecording() {
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_RESUME))
        }
    }

    /**
     * Returns true if recording has been started.
     */
    val isRecording: Boolean
        get() {
            synchronized(mReadyFence) { return mRunning }
        }

    /**
     * Tells the video recorder to refresh its EGL surface.  (Call from non-encoder thread.)
     */
    fun updateSharedContext(sharedContext: EGLContext?) {
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext))
        }
    }

    /**
     * Tells the video recorder that a new frame is available.  (Call from non-encoder thread.)
     *
     *
     * This function sends a message and returns immediately.  This isn't sufficient -- we
     * don't want the caller to latch a new frame until we're done with this one -- but we
     * can get away with it so long as the input frame rate is reasonable and the encoder
     * thread doesn't stall.
     *
     *
     * TODO: either block here until the texture has been rendered onto the encoder surface,
     * or have a separate "block if still busy" method that the caller can execute immediately
     * before it calls updateTexImage().  The latter is preferred because we don't want to
     * stall the caller while this thread does work.
     */
    fun frameAvailable(st: SurfaceTexture?) {
        synchronized(mReadyFence) {
            if (!mReady) {
                return
            }
        }
        val transform = FloatArray(16)
        st?.getTransformMatrix(transform)
        val timestamp = st?.timestamp?:0L
        if (timestamp == 0L) {
            // Seeing this after device is toggled off/on with power button.  The
            // first frame back has a zero timestamp.
            //
            // MPEG4Writer thinks this is cause to abort() in native code, so it's very
            // important that we just ignore the frame.
            Log.w(TAG, "HEY: got SurfaceTexture with timestamp of zero")
            return
        }
        if(isRecording){
            mHandler?.apply {
                sendMessage(
                    obtainMessage(
                        MSG_FRAME_AVAILABLE,
                        (timestamp shr 32).toInt(),
                        timestamp.toInt(),
                        transform
                    )
                )
            }
        }

    }

    /**
     * Tells the video recorder what texture name to use.  This is the external texture that
     * we're receiving camera previews in.  (Call from non-encoder thread.)
     */
    fun setTextureId(id: Int) {
        synchronized(mReadyFence) {
            if (!mReady) {
                return
            }
        }
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_SET_TEXTURE_ID, id, 0, null))
        }
    }

    /**
     * Encoder thread entry point.  Establishes Looper/Handler and waits for messages.
     *
     *
     * @see Thread.run
     */
    override fun run() {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND)
        // Establish a Looper for this thread, and define a Handler for it.
        Looper.prepare()
        synchronized(mReadyFence) {
            mIListenerRecordTime?.onUpdateRecordTime(0L)
            mHandler = Looper.myLooper()?.let { EncoderHandler(this, it) }
            mReady = true
            mReadyFence.notify()
        }
        Looper.loop()
        Log.d(TAG, "Encoder thread exiting")
        synchronized(mReadyFence) {
            mRunning = false
            mReady = mRunning
            mHandler = null
        }
    }

    /**
     * Handles encoder state change requests.  The handler is created on the encoder thread.
     */
    private class EncoderHandler(encoder: TextureMovieEncoder,looper: Looper) : Handler(looper) {
        private val mWeakEncoder: WeakReference<TextureMovieEncoder>

        // runs on encoder thread
        override fun handleMessage(inputMessage: Message) {
            val what = inputMessage.what
            val obj = inputMessage.obj
            val encoder = mWeakEncoder.get()
            if (encoder == null) {
                Log.w(TAG, "EncoderHandler.handleMessage: encoder is null")
                return
            }
            when (what) {
                MSG_START_RECORDING -> encoder.handleStartRecording(obj as EncoderConfig)
                MSG_STOP_RECORDING -> encoder.handleStopRecording()
                MSG_FRAME_AVAILABLE -> {
                   /* val timestamp = inputMessage.arg1.toLong() shl 32 or
                            (inputMessage.arg2.toLong() and 0xffffffffL)*/
                    encoder.handleFrameAvailable(/*obj as FloatArray, timestamp*/)
                }
                MSG_SET_TEXTURE_ID -> encoder.handleSetTexture(inputMessage.arg1)
                MSG_UPDATE_SHARED_CONTEXT -> encoder.handleUpdateSharedContext(inputMessage.obj as EGLContext)
                MSG_QUIT -> Looper.myLooper()?.quit()
                MSG_PAUSE -> encoder.handlePauseRecording()
                MSG_RESUME -> encoder.handleResumeRecording()
                else -> throw RuntimeException("Unhandled msg what=$what")
            }
        }

        init {
            mWeakEncoder = WeakReference(encoder)
        }
    }

    /**
     * Starts recording.
     */
    private fun handleStartRecording(config: EncoderConfig) {
        Log.d(TAG, "handleStartRecording $config")
        prepareEncoder(
            config.mEglContext, config.mWidth, config.mHeight, config.mBitRate,
            config.path
        )
    }

    /**
     * Handles notification of an available frame.
     *
     *
     * The texture is rendered onto the encoder's input surface, along with a moving
     * box (just because we can).
     *
     *
     * @param transform The texture transform, from SurfaceTexture.
     * @param timestampNanos The frame's timestamp, from SurfaceTexture.
     */
    private fun handleFrameAvailable(/*transform: FloatArray, timestampNanos: Long*/) {
        val drainEncoderResult = kotlin.runCatching {
            mVideoEncoder?.drainEncoder(false)
        }
        if(drainEncoderResult.isFailure){
            return
        }
        Log.e("hero", "---setTextureId==$mTextureId")
        mShowFilter.textureId = mTextureId
        mShowFilter.draw()
        if (baseTimeStamp == -1L) {
            baseTimeStamp = System.nanoTime()
            mVideoEncoder?.startRecord()
        }
        val nano = System.nanoTime()
        val recordTimeNanoTime = nano - baseTimeStamp - pauseDelayTime
        mIListenerRecordTime?.onUpdateRecordTime(recordTimeNanoTime)
        Log.v(
            "TextureMovieEncoder",
            "TimeStampVideo=" + recordTimeNanoTime
                    + ";nanoTime=" + nano
                    + ";baseTimeStamp=" + baseTimeStamp
                    + ";pauseDelay=" + pauseDelayTime
        )
        mInputWindowSurface?.setPresentationTime(recordTimeNanoTime)
        mInputWindowSurface?.swapBuffers()
    }

    var pauseDelayTime: Long = 0
    var onceDelayTime: Long = 0
    private fun handlePauseRecording() {
        onceDelayTime = System.nanoTime()
        mVideoEncoder?.pauseRecording()
    }

    private fun handleResumeRecording() {
        onceDelayTime = System.nanoTime() - onceDelayTime
        pauseDelayTime += onceDelayTime
        mVideoEncoder?.resumeRecording()
    }

    /**
     * Handles a request to stop encoding.
     */
    private fun handleStopRecording() {
        Log.d(TAG, "handleStopRecording")
        kotlin.runCatching {
            mVideoEncoder?.drainEncoder(true)
        }
        mVideoEncoder?.stopAudRecord()
        releaseEncoder()
    }

    /**
     * Sets the texture name that SurfaceTexture will use when frames are received.
     */
    private fun handleSetTexture(id: Int) {
        //Log.d(TAG, "handleSetTexture " + id);
        mTextureId = id
    }

    /**
     * Tears down the EGL surface and context we've been using to feed the MediaCodec input
     * surface, and replaces it with a new one that shares with the new context.
     *
     *
     * This is useful if the old context we were sharing with went away (maybe a GLSurfaceView
     * that got torn down) and we need to hook up with the new one.
     */
    private fun handleUpdateSharedContext(newSharedContext: EGLContext) {
        Log.d(TAG, "handleUpdatedSharedContext $newSharedContext")

        // Release the EGLSurface and EGLContext.
        mInputWindowSurface?.releaseEglSurface()
        mInput?.destroy()
        mEglCore?.release()

        // Create a new EGLContext and recreate the window surface.
        mEglCore = EglCore(newSharedContext, EglCore.FLAG_RECORDABLE)
        mInputWindowSurface?.recreate(mEglCore)
        mInputWindowSurface?.makeCurrent()

        // Create new programs and such for the new context.
        mInput = MagicCameraInputFilter()
        mInput?.init()
        filter = null
        if (filter != null) {
            filter?.init()
            filter?.onInputSizeChanged(mPreviewWidth, mPreviewHeight)
            filter?.onDisplaySizeChanged(mVideoWidth, mVideoHeight)
        }
    }

    private fun prepareEncoder(
        sharedContext: EGLContext, width: Int, height: Int, bitRate: Int,
        path: String
    ) {
        mVideoEncoder = try {
            VideoEncoderCore(width, height, bitRate, path)
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
        mVideoWidth = width
        mVideoHeight = height
        mEglCore = EglCore(sharedContext, EglCore.FLAG_RECORDABLE)
        mInputWindowSurface = WindowSurface(mEglCore, mVideoEncoder?.inputSurface, true)
        mInputWindowSurface?.makeCurrent()
        mInput = MagicCameraInputFilter()
        mInput?.init()
        filter = null
        if (filter != null) {
            filter?.init()
            filter?.onInputSizeChanged(mPreviewWidth, mPreviewHeight)
            filter?.onDisplaySizeChanged(mVideoWidth, mVideoHeight)
        }
        mShowFilter.create()
        baseTimeStamp = -1
    }

    private fun releaseEncoder() {
        mVideoEncoder?.release()
        if (mInputWindowSurface != null) {
            mInputWindowSurface?.release()
            mInputWindowSurface = null
        }
        if (mInput != null) {
            mInput?.destroy()
            mInput = null
        }
        if (mEglCore != null) {
            mEglCore?.release()
            mEglCore = null
        }
        if (filter != null) {
            filter?.destroy()
            filter = null
            //type = MagicFilterType.NONE;
        }
    }

    //private MagicFilterType type = MagicFilterType.NONE;
    private val mShowFilter: BaseFilter = NoneFilter(GLCameraxUtils.getApplicationContext().resources)

    //    public void setFilter(MagicFilterType type) {
    //        this.type = type;
    //    }
    private var mPreviewWidth = -1
    private var mPreviewHeight = -1
    private var mVideoWidth = -1
    private var mVideoHeight = -1
    fun setPreviewSize(width: Int, height: Int) {
        mPreviewWidth = width
        mPreviewHeight = height
    }

    fun setTextureBuffer(gLTextureBuffer: FloatBuffer?) {
        this.gLTextureBuffer = gLTextureBuffer
    }

    fun setCubeBuffer(gLCubeBuffer: FloatBuffer?) {
        this.gLCubeBuffer = gLCubeBuffer
    }

    companion object {
        private const val TAG = ""
        private const val VERBOSE = false
        private const val MSG_START_RECORDING = 0
        private const val MSG_STOP_RECORDING = 1
        private const val MSG_FRAME_AVAILABLE = 2
        private const val MSG_SET_TEXTURE_ID = 3
        private const val MSG_UPDATE_SHARED_CONTEXT = 4
        private const val MSG_QUIT = 5
        private const val MSG_PAUSE = 6
        private const val MSG_RESUME = 7
    }
}