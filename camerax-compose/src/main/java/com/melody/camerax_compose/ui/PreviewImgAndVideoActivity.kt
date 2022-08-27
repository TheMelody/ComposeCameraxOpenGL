package com.melody.camerax_compose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PreviewImgAndVideoActivity
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 18:17
 */
class PreviewImgAndVideoActivity: ComponentActivity() {
    companion object {
        const val FILE_URI_STRING = "file_uri"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()){
                Column(modifier = Modifier.padding(15.dp).align(Alignment.Center)) {
                    Text(
                        text = "文件位置：",
                        style = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center)
                    )
                    Text(
                        text = intent?.getStringExtra(FILE_URI_STRING)?: "",
                        style = MaterialTheme.typography.body1.copy(fontSize = 13.sp, textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}