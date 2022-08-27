package com.melody.ui.camerax

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.melody.opengl.camerax.GLCameraView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * CameraView 支持拍照和视频拍摄
 *
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/8/27 09:13
 */
/**
 * 将相机的生命周期绑定到 LifecycleOwner
 */
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener(
                {
                  continuation.resume(cameraProvider.get())
                }, ContextCompat.getMainExecutor(this)
            )
        }
    }

/**
 * 相机视图
 */
@Composable
fun CameraXView(
    modifier: Modifier,
    lensFacing: Int,
    content: @Composable (GLCameraView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val glCameraPreview = remember {
        GLCameraView(context, 9 / 16F)
    }

    val scope = remember {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        // 解除生命周期绑定，并从 CameraX 中移除
        cameraProvider.unbindAll()

        val preview = Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
        }.build()

        // 需要在preview.setSurfaceProvider前面调用
        glCameraPreview.switchCameraLensFacing(true,lensFacing)
        preview.setSurfaceProvider(glCameraPreview)

        // 设置前置、后置摄像头切换
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        kotlin.runCatching {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        }
    }

    Box(contentAlignment = Alignment.BottomCenter, modifier = modifier) {
        DisposableEffect(
            AndroidView(
                factory = { glCameraPreview },
                modifier = Modifier.fillMaxSize()
            )
        ) {
            onDispose {
                scope.launch {
                    glCameraPreview.switchCameraLensFacing(false,lensFacing)
                    val cameraProvider = context.getCameraProvider()
                    // 解除生命周期绑定，并从 CameraX 中移除
                    cameraProvider.unbindAll()
                }
            }
        }
        content(glCameraPreview)
    }
}

