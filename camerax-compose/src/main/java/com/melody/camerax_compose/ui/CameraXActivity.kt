package com.melody.camerax_compose.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.melody.camerax_compose.ui.content.CameraScreen
import com.melody.opengl.camerax.utils.GLCameraxUtils

/**
 * CameraXActivity
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 10:56
 */
class CameraXActivity : ComponentActivity() {

    companion object {
        const val INTENT_CAPTURE_TYPE = "capture_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //TODO: Demo我就放这里初始化了，实际上可以在application中初始化
        GLCameraxUtils.init(application)
        val isTakePhoto = intent?.getBooleanExtra(INTENT_CAPTURE_TYPE, true) ?: true
        setContent {
            CameraScreen(
                isTakePhoto = isTakePhoto,
                onOpenPreviewFile = {
                    // TODO: 读者根据自己的需求自己看着处理跳转吧。。。。
                    startActivity(Intent(this,PreviewImgAndVideoActivity::class.java).apply {
                        putExtra(PreviewImgAndVideoActivity.FILE_URI_STRING,it.toString())
                    })
                    finish()
                },
                onNavigateUp = {
                    finish()
                }
            )
        }
    }
}

