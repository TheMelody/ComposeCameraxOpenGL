package com.melody.camerax_compose.extensions

import kotlinx.coroutines.Job

/**
 * JobExtensions
 * @author TheMelody
 * @email developer_melody@163.com
 * created 2022/8/27 11:35
 */
/**
 * 把Job添加进列表中
 */
fun Job.add(list: MutableList<Job>) {
    list.add(this)
}