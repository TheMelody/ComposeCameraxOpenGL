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
import com.melody.opengl.camerax.utils.GLCameraxUtils
import java.io.File
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.nio.FloatBuffer

class TexturePictureEncoder : Runnable {
    // ----- accessed exclusively by encoder thread -----
    private var mInputWindowSurface: WindowSurface? = null
    private var mEglCore: EglCore? = null
    private var mInput: MagicCameraInputFilter? = null
    private var mTextureId = 0

    // ----- accessed by multiple threads -----
    @Volatile
    private var mHandler: EncoderHandler? = null
    private val mReadyFence = Object() // guards ready/running
    private var mReady = false
    private var mRunning = false
    private var filter: GPUImageFilter? = null
    private var gLCubeBuffer: FloatBuffer? = null
    private var gLTextureBuffer: FloatBuffer? = null

    class EncoderConfig(
        val path: String, val mWidth: Int, val mHeight: Int, val mBitRate: Int,
        val mEglContext: EGLContext
    ) {
        override fun toString(): String {
            return "EncoderConfig: " + mWidth + "x" + mHeight + " @" + mBitRate +
                    " to '" + path + "' ctxt=" + mEglContext
        }
    }

    fun updateSharedContext(sharedContext: EGLContext?) {
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext))
        }
    }

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
        mHandler?.apply {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE))
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
    private class EncoderHandler(encoder: TexturePictureEncoder, looper: Looper) : Handler(looper) {
        private val mWeakEncoder: WeakReference<TexturePictureEncoder>

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
                MSG_START_RECORDING -> encoder.handleStartCapture(obj as EncoderConfig)
                MSG_FRAME_AVAILABLE -> {
                    encoder.handleFrameAvailable()
                }
                MSG_SET_TEXTURE_ID -> encoder.handleSetTexture(inputMessage.arg1)
                MSG_UPDATE_SHARED_CONTEXT -> encoder.handleUpdateSharedContext(inputMessage.obj as EGLContext)
                MSG_QUIT -> Looper.myLooper()?.quit()
                else -> throw RuntimeException("Unhandled msg what=$what")
            }
        }

        init {
            mWeakEncoder = WeakReference(encoder)
        }
    }

    /**
     * Starts Capture.
     */
    private fun handleStartCapture(config: EncoderConfig) {
        Log.d(TAG, "handleStartRecording $config")
        prepareEncoder(
            config.mEglContext, config.mWidth, config.mHeight,
            config.path
        )
    }

    /**
     * Handles notification of an available frame.
     *
     *
     * The texture is rendered onto the encoder's input surface, along with a moving
     * box (just because we can).
     */
    private fun handleFrameAvailable() {
        Log.e("hero", "---setTextureId==$mTextureId")
        mShowFilter.textureId = mTextureId
        mShowFilter.draw()
        mInputWindowSurface?.swapBuffers()
        mInputWindowSurface?.saveFrame(File(mPicturePath))
        Looper.myLooper()?.quit()
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
        sharedContext: EGLContext,
        width: Int,
        height: Int,
        path: String
    ) {
        mPicturePath = path
        mVideoWidth = width
        mVideoHeight = height
        mEglCore = EglCore(sharedContext, EglCore.FLAG_RECORDABLE)
        mInputWindowSurface = WindowSurface(mEglCore, mPreviewWidth, mPreviewHeight, true)
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
    }

    fun startCapture(config: EncoderConfig?){
        Log.d(TAG, "Encoder: startCapture()")
        synchronized(mReadyFence) {
            if (mRunning) {
                Log.w(TAG, "Encoder thread already running")
                return
            }
            mRunning = true
            Thread(this, "TexturePictureEncoder").start()
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

    fun releaseEncoder() {
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

    private var mPicturePath:String = ""

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