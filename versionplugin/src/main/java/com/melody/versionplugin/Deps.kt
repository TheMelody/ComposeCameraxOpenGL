package com.melody.versionplugin

/**
 * 依赖管理 和 配置类
 *
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/8/27 08:16
 */
object BuildVersion {
    const val compileSdkVersion = 32
    const val minSdkVersion = 21
    const val targetSdkVersion = 32
    const val versionCode = 100
    const val versionName = "v1.0.0"
    const val applicationId = "com.melody.camerax.demo"

    // versionPlugin的build.gradle文件里面的版本号要和这里的同步
    const val kotlinCompilerVersion = "1.6.21"
    const val jvmTarget = "11"
}

/**
 * 依赖
 */
object Dependencies {
    //最新版本 https://developer.android.com/jetpack/androidx/releases/compose
    private const val compose_version = "1.2.0-beta03"
    private const val retrofit_version = "2.9.0"
    private const val okhttp_version = "4.9.3"

    //最新版本 https://developer.android.google.cn/jetpack/androidx/releases/paging
    private const val paging_version = "3.1.0"
    private const val paging_compose_version = "1.0.0-alpha14"

    //最新版本 https://developer.android.google.cn/jetpack/androidx/releases/datastore
    private const val datastore_version = "1.0.0"

    //最新版本 https://github.com/google/accompanist
    private const val accompanist_version = "0.24.9-beta"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/lifecycle
    private const val lifecycle_version = "2.5.0-beta01"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/activity
    private const val activity_compose_version = "1.5.0-beta01"

    //最新版本 https://developer.android.com/jetpack/compose/navigation
    private const val nav_compose_version = "2.4.2"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/core
    private const val splash_screen_core = "1.0.0-beta02"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/startup
    private const val startup_runtime = "1.2.0-alpha01"

    //最新版本 https://coil-kt.github.io/coil/compose/
    private const val coil_compose = "2.1.0"

    //最新版本 https://developer.android.com/studio/build/multidex
    private const val multidex_version = "2.0.1"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/constraintlayout
    private const val constraint_compose = "1.0.0"

    //最新版本 https://developer.android.com/jetpack/androidx/releases/work
    private const val work_runtime_version = "2.7.1"

    //最新版本 https://x5.tencent.com/docs/access.html
    private const val tbs_sdk_version = "44181"

    //最新版本 https://square.github.io/leakcanary/getting_started/
    private const val leakcanary_version = "2.9.1"

    //最新版本 https://repo1.maven.org/maven2/com/tencent/bugly/crashreport/
    private const val crash_report_bugly_version = "4.0.0"

    //最新版本 https://repo1.maven.org/maven2/com/tencent/bugly/nativecrashreport/
    private const val native_crash_report_bugly_version = "3.9.2"

    //最新版本 https://github.com/google/ExoPlayer
    private const val exo_player_version = "2.17.1"

    // 最新版本 https://developer.android.com/jetpack/androidx/releases/camera
    private const val camerax_version = "1.1.0-beta03"

    // 最新版本 https://github.com/cats-oss/android-gpuimage
    private const val gpu_image_version = "2.1.0"

    // 最新版本 https://github.com/alibaba/ARouter/blob/master/README_CN.md
    private const val arouter_version = "1.5.2"

    // 最新版本 http://airbnb.io/lottie/#/android-compose
    private const val lottieVersion = "5.0.3"

    // 最新版本 https://mvnrepository.com/artifact/com.tencent.imsdk/imsdk-plus
    private const val tencent_im_version = "6.2.2363"

    // 最新版本 https://developer.android.com/jetpack/androidx/releases/viewpager2
    private const val view_page2_version = "1.1.0-beta01"

    // 最新版本 https://developer.android.com/jetpack/androidx/releases/coordinatorlayout#declaring_dependencies
    private const val coordinator_version = "1.2.0"

    private const val material_version = "1.6.1"

    private const val fragment_ktx_version = "1.5.0"

    const val core_ktx = "androidx.core:core-ktx:1.8.0"

    const val google_material = "com.google.android.material:material:$material_version"

    const val lifecycle_runtime_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
    const val lifecycle_viewmodel_compose = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
    const val fragment_ktx = "androidx.fragment:fragment-ktx:$fragment_ktx_version"
    const val activity_compose = "androidx.activity:activity-compose:$activity_compose_version"
    const val navigation_compose = "androidx.navigation:navigation-compose:$nav_compose_version"
    const val app_startup = "androidx.startup:startup-runtime:$startup_runtime"
    const val multidex = "androidx.multidex:multidex:$multidex_version"
    const val compose_runtime = "androidx.compose.runtime:runtime:$compose_version"
    // 需要一台9.0+的root设备
    //const val profile_installer = "androidx.profileinstaller:profileinstaller:$compose_version"
    const val runtime_livedata = "androidx.compose.runtime:runtime-livedata:$compose_version"
    const val compose_ui = "androidx.compose.ui:ui:$compose_version"
    const val compose_ui_util = "androidx.compose.ui:ui-util:$compose_version"
    const val compose_material = "androidx.compose.material:material:$compose_version"
    const val compose_ui_tool_preview = "androidx.compose.ui:ui-tooling-preview:$compose_version"
    const val compose_ui_tooling = "androidx.compose.ui:ui-tooling:$compose_version"
    const val data_store = "androidx.datastore:datastore-preferences:$datastore_version"
    const val accompanist_insets_ui = "com.google.accompanist:accompanist-insets-ui:$accompanist_version"
    const val accompanist_systemuicontroller = "com.google.accompanist:accompanist-systemuicontroller:$accompanist_version"
    const val accompanist_pager = "com.google.accompanist:accompanist-pager:$accompanist_version"
    const val accompanist_pager_indicators = "com.google.accompanist:accompanist-pager-indicators:$accompanist_version"
    const val accompanist_navigation_animation = "com.google.accompanist:accompanist-navigation-animation:$accompanist_version"
    const val accompanist_flow_layout = "com.google.accompanist:accompanist-flowlayout:$accompanist_version"
    const val accompanist_permissions = "com.google.accompanist:accompanist-permissions:$accompanist_version"
    const val constraintlayout_compose = "androidx.constraintlayout:constraintlayout-compose:$constraint_compose"
    const val work_runtime = "androidx.work:work-runtime-ktx:$work_runtime_version"
    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:$leakcanary_version"
    // CameraX相关依赖
    const val camera_core = "androidx.camera:camera-core:$camerax_version"
    const val camera_camera2 = "androidx.camera:camera-camera2:$camerax_version"
    const val camera_lifecycle = "androidx.camera:camera-lifecycle:$camerax_version"
    const val camera_view = "androidx.camera:camera-view:$camerax_version"
    const val camera_video = "androidx.camera:camera-video:$camerax_version"
    const val camera_extensions = "androidx.camera:camera-extensions:$camerax_version"
    const val gpu_image = "jp.co.cyberagent.android:gpuimage:$gpu_image_version"

    // ARouter
    const val arouter_api = "com.alibaba:arouter-api:$arouter_version"
    const val arouter_compiler = "com.alibaba:arouter-compiler:$arouter_version"

}