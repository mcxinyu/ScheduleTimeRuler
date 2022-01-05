package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import com.mcxinyu.scheduletimeruler.model.ScheduleModel
import kotlin.math.max

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class ScheduleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScaleTimeRulerView(context, attrs) {

    var schedules: List<ScheduleModel> = listOf()
        set(value) {
            field = value
            invalidate()
        }

    private val rect = Rect()
    private val dp16 = 16.toPx(context)

    private val textPaint = TextPaint()

    init {
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = tickTextSize * 0.9f
        typeface?.let { textPaint.typeface = typeface }
    }

    override fun onDraw(canvas: Canvas) {

        val left = baselinePosition + dp16
        val right = baselinePosition + dp16 * 10

        for (schedule in schedules) {
            val top =
                cursorLinePosition + (schedule.startTime - cursorTimeValue) * millisecondUnitPixel
            val bottom =
                cursorLinePosition + (schedule.endTime - cursorTimeValue) * millisecondUnitPixel

            if (top > scrollY + height) {
                continue
            }
            if (bottom < scrollY) {
                continue
            }

            onDrawSchedule(canvas, schedule, left, top, right, bottom)
        }

        super.onDraw(canvas)
    }

    protected open fun onDrawSchedule(
        canvas: Canvas,
        schedule: ScheduleModel,
        left: Float, top: Float, right: Float, bottom: Float
    ) {
        rect.left = left.toInt()
        rect.top = top.toInt()
        rect.right = right.toInt()
        rect.bottom = bottom.toInt()

        //region draw range
        textPaint.color = schedule.background
        canvas.drawRect(rect, textPaint)
        //endregion

        textPaint.color = schedule.textColor

        val vertical = dp16 / 8

        //region draw title
        val titleLayout = StaticLayout(
            schedule.title,
            textPaint,
            (right - left - dp16 / 2).toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
        if (schedule.title.isNotEmpty() && bottom - top >= titleLayout.height) {
            canvas.save()
            canvas.translate(
                left + dp16 / 4,
                if (bottom - max(0f, top) >= titleLayout.height + vertical * 2)
                    max(0f, top) + vertical
                else
                    bottom - titleLayout.height - vertical
            )
            titleLayout.draw(canvas)
            canvas.restore()
        }
        //endregion

        //region draw text
        val textLayout = StaticLayout(
            schedule.text,
            textPaint,
            (right - left - dp16 / 2).toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
        if (schedule.text.isNotEmpty() &&
            bottom - max(0f, top) >= titleLayout.height + textLayout.height
        ) {
            canvas.save()
            canvas.translate(left + dp16 / 4, max(0f, top) + titleLayout.height + vertical)
            textLayout.draw(canvas)
            canvas.restore()
        }
        //endregion
    }

    companion object {
        val TAG = ScheduleTimeRulerView::class.java.simpleName
    }
}