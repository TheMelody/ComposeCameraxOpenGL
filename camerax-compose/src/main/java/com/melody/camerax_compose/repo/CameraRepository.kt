package com.melody.camerax_compose.repo

import android.media.AudioAttributes
import android.net.Uri
import android.os.Environment
import com.melody.camerax_compose.R
import com.melody.camerax_compose.utils.RingtoneUtils
import com.melody.camerax_compose.utils.deleteFileUri
import com.melody.opengl.camerax.utils.GLCameraxUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * CameraRepository
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/4/20 17:49
 */
object CameraRepository {

    private const val TAG = "CameraRepository"

    private const val FILE_IMAGE_SUFFIX = ".jpg"

    private const val FILE_VIDEO_SUFFIX = ".mp4"

    /**
     * 文件名时间格式
     */
    private const val FILE_NAME_PATTERN = "yyyyMMdd_HHmmssSSS"

    /**
     * 删除视频文件
     */
    fun deleteVideoFile(uri: Uri) {
        GLCameraxUtils.getApplicationContext().deleteFileUri(uri)
    }

    /**
     * 拍摄的视频存储到/data/data/包名/movie/目录下面
     */
    fun getRecordVideoFileUri():Uri {
        val videoFileName = SimpleDateFormat(FILE_NAME_PATTERN, Locale.getDefault())
            .format(System.currentTimeMillis()) + FILE_VIDEO_SUFFIX
        val videoFolder = GLCameraxUtils.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return Uri.fromFile(File(videoFolder, videoFileName))
    }

    /**
     * 拍摄的视频存储到/data/data/包名/Pictures/目录下面
     */
    fun getTakePictureFileUri():Uri {
        val fileName = SimpleDateFormat(FILE_NAME_PATTERN, Locale.getDefault())
            .format(System.currentTimeMillis()) + FILE_IMAGE_SUFFIX

        // 拍照图片默认保存在内部存储，在作品发布页面，只有勾选了保存到相册，才会压缩保存到相册中
        return Uri.fromFile(File(
            GLCameraxUtils.getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + File.separator + fileName
        ))
    }


    /**
     * 拍照片
     */
    fun playShutterSound() {
        // 播放快门的声音
        RingtoneUtils.safePlaySound(
            rawRes = R.raw.shutter,
            usage = AudioAttributes.USAGE_MEDIA
        )
    }
}