package com.mcxinyu.scheduletimeruler.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.mcxinyu.scheduletimeruler.R

/**
 *
 * @author <a href=mailto:mcxinyu@foxmail.com>yuefeng</a> in 2022/1/4.
 */
data class CardModel(
    var title: String = "",
    var text: String = "",
    var startTime: Long,
    var endTime: Long,
    @DrawableRes val background: Int = R.color.ltGray,
    @ColorInt val titleColor: Int = Color.DKGRAY,
    @ColorInt val textColor: Int = Color.GRAY
) {
    init {
        if (startTime > endTime) {
            val copy = startTime
            startTime = endTime
            endTime = copy
        }
    }
}

data class CardPositionInfo(
    val model: CardModel,
    var left: Float = -1f,
    var right: Float = -1f,
    var top: Float = -1f,
    var bottom: Float = -1f,
) {
    fun reset() {
        left = -1f
        right = -1f
        top = -1f
        bottom = -1f
    }
}