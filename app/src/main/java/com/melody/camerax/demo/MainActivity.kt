package com.melody.camerax.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.melody.camerax.demo.ui.theme.ComposeCameraxTheme
import com.melody.camerax_compose.ui.CameraXActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ComposeCameraxTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MainContent {
                        startCameraX(this@MainActivity,it)
                    }
                }
            }
        }
    }
}

private fun startCameraX(context: Context, isTakePhoto: Boolean) {
    context.startActivity(Intent(context, CameraXActivity::class.java).apply {
        putExtra(CameraXActivity.INTENT_CAPTURE_TYPE, isTakePhoto)
    })
}