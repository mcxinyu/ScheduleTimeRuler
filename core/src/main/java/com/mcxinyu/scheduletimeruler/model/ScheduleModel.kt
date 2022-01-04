package com.mcxinyu.scheduletimeruler.model

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 *
 * @author <a href=mailto:mcxinyu@foxmail.com>yuefeng</a> in 2022/1/4.
 */
data class ScheduleModel(
    val title: String = "",
    val text: String = "",
    var startTime: Long,
    var endTime: Long,
    @ColorInt val color: Int = Color.LTGRAY,
    @ColorInt val textColor: Int = Color.BLACK
) {
    init {
        if (startTime > endTime) {
            val copy = startTime
            startTime = endTime
            endTime = copy
        }
    }
}