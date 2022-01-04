package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import com.mcxinyu.scheduletimeruler.model.ScheduleModel

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
class ScheduleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScaleTimeRulerView(context, attrs) {

    init {
        Log.d(TAG, "to-do")
    }

    var schedules: List<ScheduleModel> = listOf()
        set(value) {
            field = value
            invalidate()
        }

    private val rect = Rect()

    override fun onDraw(canvas: Canvas) {

        val start = cursorTimeValue - cursorLinePosition / millisecondUnitPixel

        val end = height / millisecondUnitPixel + start

        val dp16 = 16.toPx(context)

        rect.left = (baselinePosition + dp16).toInt()
        rect.right = (baselinePosition + dp16 * 10).toInt()

        for (schedule in schedules) {
            val top =
                (cursorLinePosition + (schedule.startTime - cursorTimeValue) * millisecondUnitPixel).toInt()
            val bottom =
                (cursorLinePosition + (schedule.endTime - cursorTimeValue) * millisecondUnitPixel).toInt()

            if (top > scrollY + height) {
                continue
            }
            if (bottom < scrollY) {
                continue
            }

            rect.top = top
            rect.bottom = bottom

            paint.color = schedule.color
            canvas.drawRect(rect, paint)
        }

        super.onDraw(canvas)
    }

    companion object {
        val TAG = ScheduleTimeRulerView::class.java.simpleName
    }
}