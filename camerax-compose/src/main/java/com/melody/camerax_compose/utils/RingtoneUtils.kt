package com.melody.camerax_compose.utils

import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.annotation.RawRes
import com.melody.opengl.camerax.utils.GLCameraxUtils

/**
 * RingtoneUtils
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/4/30 12:38
 */
object RingtoneUtils {

    /**
     * 播放RAW下面的一段音频
     */
    fun safePlaySound(@RawRes rawRes:Int,usage:Int) {
        val applicationContext = GLCameraxUtils.getApplicationContext()
        val ringtone = kotlin.runCatching {
            RingtoneManager.getRingtone(
                applicationContext,
                Uri.parse("android.resource://${applicationContext.packageName}/${rawRes}")
            ).apply {
                audioAttributes = AudioAttributes.Builder().setUsage(usage).build()
            }
        }
        if(ringtone.isSuccess) {
            ringtone.getOrNull()?.play()
        }
    }
}