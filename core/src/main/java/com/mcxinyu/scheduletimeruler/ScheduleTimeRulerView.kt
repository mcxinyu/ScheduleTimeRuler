package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.res.ResourcesCompat
import com.mcxinyu.scheduletimeruler.model.CardModel
import com.mcxinyu.scheduletimeruler.model.CardPositionInfo
import kotlin.math.max

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class ScheduleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScaleTimeRulerView(context, attrs) {

    private var cardMargin: Float
    private var cardWidth: Float

    private var datas = mutableListOf<CardPositionInfo>()
    fun setDatas(list: List<CardModel>) {
        datas = list.map { CardPositionInfo(it) }.toMutableList()
        invalidate()
    }

    private val dp16 = 16.toPx(context)

    private val textPaint = TextPaint()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScheduleTimeRulerView)

        cardWidth = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardWidth,
            128.toPx(context)
        )
        cardMargin = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardMargin,
            16.toPx(context)
        )

        typedArray.recycle()

        textPaint.textAlign = Paint.Align.LEFT
        typeface?.let { textPaint.typeface = typeface }
    }

    override fun onDraw(canvas: Canvas) {

        val left = baselinePosition + cardMargin
        val right = left + cardWidth

        for (schedule in datas) {
            schedule.reset()

            val top =
                cursorLinePosition + (schedule.model.startTime - cursorTimeValue) * millisecondUnitPixel
            val bottom =
                cursorLinePosition + (schedule.model.endTime - cursorTimeValue) * millisecondUnitPixel

            if (top > scrollY + height) {
                continue
            }
            if (bottom < scrollY) {
                continue
            }

            schedule.apply {
                this.left = left
                this.top = top
                this.right = right
                this.bottom = bottom
            }
            onDrawCard(canvas, schedule.model, left, top, right, bottom)
        }

        super.onDraw(canvas)
    }

    protected open fun onDrawCard(
        canvas: Canvas,
        schedule: CardModel,
        left: Float, top: Float, right: Float, bottom: Float
    ) {
        //region draw range
        val drawable = ResourcesCompat.getDrawable(resources, schedule.background, null)
        drawable?.let {
            if (it is ColorDrawable) {
                textPaint.color = it.color
                canvas.drawRoundRect(left, top, right, bottom, dp16 / 4, dp16 / 4, textPaint)
            }
//            else {
//                val toBitmap = it.toBitmap()
//                val createScaledBitmap =
//                    Bitmap.createScaledBitmap(
//                        toBitmap,
//                        (right - left).toInt(),
//                        ((right - left) * toBitmap.height / toBitmap.width).toInt(),
//                        true
//                    )
//
//                canvas.drawBitmap(createScaledBitmap, left, top, textPaint)
//            }
        }
        //endregion

        val vertical = dp16 / 8

        //region draw title
        textPaint.textSize = tickTextSize
        textPaint.color = schedule.titleColor
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
        textPaint.textSize = tickTextSize * 0.9f
        textPaint.color = schedule.textColor
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

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onCardClickListener?.let { listener ->
            datas.firstOrNull {
                it.left < e.x && it.right > e.x && it.top < e.y && it.bottom > e.y
            }?.let {
                listener.onClick(it.model)
            }
        }

        return super.onSingleTapUp(e)
    }

    var onCardClickListener: OnCardClickListener? = null

    interface OnCardClickListener {
        fun onClick(model: CardModel)
    }

    companion object {
        val TAG = ScheduleTimeRulerView::class.java.simpleName
    }
}