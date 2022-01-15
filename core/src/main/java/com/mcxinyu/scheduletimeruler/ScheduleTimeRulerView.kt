package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.mcxinyu.scheduletimeruler.model.CardModel
import com.mcxinyu.scheduletimeruler.model.CardPositionInfo
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class ScheduleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScaleTimeRulerView(context, attrs) {

    var cardFilmHoleOffset: Float
    var cardFilmHoleGap: Float
    var cardFilmHoleHeight: Float
    var cardFilmHoleWidth: Float
    var cardSimulateFilmStyle: Boolean

    @ColorInt
    var cardLineColor: Int
    var cardMargin: Float
    var cardWidth: Float

    var data = mutableListOf<CardPositionInfo>()
        internal set

    open fun setData(list: List<CardModel>) {
        data = list.map { CardPositionInfo(it) }.toMutableList()
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
        cardLineColor = typedArray.getColor(
            R.styleable.ScheduleTimeRulerView_strv_cardLineColor,
            baselineOutDayColor
        )
        cardSimulateFilmStyle = typedArray.getBoolean(
            R.styleable.ScheduleTimeRulerView_strv_cardSimulateFilmStyle,
            true
        )
        cardFilmHoleWidth = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardFilmHoleWidth,
            16.toPx(context)
        )
        cardFilmHoleHeight = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardFilmHoleHeight,
            cardFilmHoleWidth / 16 * 9
        )
        cardFilmHoleGap = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardFilmHoleGap,
            cardFilmHoleHeight
        )
        cardFilmHoleOffset = typedArray.getDimension(
            R.styleable.ScheduleTimeRulerView_strv_cardFilmHoleOffset,
            -cardFilmHoleHeight / 2
        )

        typedArray.recycle()

        textPaint.textAlign = Paint.Align.LEFT
        typeface?.let { textPaint.typeface = typeface }
    }

    override fun onDrawTick(canvas: Canvas) {
        super.onDrawTick(canvas)

        val left = baselinePosition + cardMargin
        val right = left + cardWidth

        textPaint.color = cardLineColor
        canvas.drawRect(left, 0f, right, height.toFloat(), textPaint)

        for (schedule in data) {
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

                val path = Path()
                path.fillType = Path.FillType.EVEN_ODD
                path.addRect(left, top, right, bottom, Path.Direction.CW)

                if (cardSimulateFilmStyle && bottom - top > cardFilmHoleHeight) {
                    val count =
                        (bottom - top) /
                                (cardFilmHoleHeight + cardFilmHoleGap -
                                        abs(cardFilmHoleOffset))
                    for (i in 0..count.toInt()) {
                        val aLeft = left + cardFilmHoleGap
                        val aTop =
                            top + i * cardFilmHoleHeight + cardFilmHoleOffset + i * cardFilmHoleGap
                        val aBottom = aTop + cardFilmHoleHeight

                        path.addRect(
                            aLeft,
                            min(max(aTop, top), bottom),
                            aLeft + cardFilmHoleWidth,
                            min(aBottom, bottom),
                            Path.Direction.CCW
                        )

                        val aRight = right - cardFilmHoleGap

                        path.addRect(
                            aRight - cardFilmHoleWidth,
                            min(max(aTop, top), bottom),
                            aRight,
                            min(aBottom, bottom),
                            Path.Direction.CCW
                        )
                    }
                }

                canvas.drawPath(path, textPaint)
            } else {
                val toBitmap = it.toBitmap()
                val bitmap = Bitmap.createScaledBitmap(
                    toBitmap,
                    (right - left).toInt(),
                    ((right - left) * toBitmap.height / toBitmap.width).toInt(),
                    true
                )

                val path = Path()
//                path.fillType = Path.FillType.EVEN_ODD

                val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                textPaint.shader = shader

                canvas.drawRect(left, top, right, bottom, textPaint)

                textPaint.shader = null
            }
        }
        //endregion

        val vertical = dp16 / 8

        //region draw title
        textPaint.textSize = tickTextSize
        textPaint.color = schedule.titleColor
        val titleLayout = StaticLayout(
            schedule.title,
            textPaint,
            (right - left - dp16 / 2 -
                    if (cardSimulateFilmStyle) cardFilmHoleGap * 4 + cardFilmHoleWidth * 2
                    else 0f
                    ).toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
        if (schedule.title.isNotEmpty() && bottom - top >= titleLayout.height) {
            canvas.save()
            canvas.translate(
                left + dp16 / 4 +
                        if (cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth
                        else 0f,
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
            (right - left - dp16 / 2 -
                    if (cardSimulateFilmStyle) cardFilmHoleGap * 4 + cardFilmHoleWidth * 2
                    else 0f
                    ).toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
        val titleHeight = if (schedule.title.isNotEmpty()) titleLayout.height else 0
        if (schedule.text.isNotEmpty() &&
            bottom - max(0f, top) >= titleHeight + textLayout.height
        ) {
            canvas.save()
            canvas.translate(
                left + dp16 / 4 +
                        if (cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth
                        else 0f,
                max(0f, top) + titleHeight + vertical
            )
            textLayout.draw(canvas)
            canvas.restore()
        }
        //endregion
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onCardClickListener?.let { listener ->
            data.firstOrNull {
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