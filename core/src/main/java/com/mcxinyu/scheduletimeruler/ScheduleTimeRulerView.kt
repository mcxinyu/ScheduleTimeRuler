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

    var cardShowText: Boolean
    var cardShowTitle: Boolean
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

        cardShowTitle = typedArray.getBoolean(
            R.styleable.ScheduleTimeRulerView_strv_cardShowTitle,
            true
        )
        cardShowText = typedArray.getBoolean(
            R.styleable.ScheduleTimeRulerView_strv_cardShowText,
            true
        )
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

        var top = 0f
        var bottom = 0f
        var left = 0f
        var right = 0f

        if (orientation == 0) {
            bottom = baselinePosition - cardMargin
            top = bottom - cardWidth
        } else {
            left = baselinePosition + cardMargin
            right = left + cardWidth
        }

        textPaint.color = cardLineColor
        canvas.drawRect(left, 0f, right, height.toFloat(), textPaint)

        for (schedule in data) {
            schedule.reset()

            if (orientation == 0) {
                left =
                    cursorLinePosition + (schedule.model.startTime - cursorTimeValue) * millisecondUnitPixel
                right =
                    cursorLinePosition + (schedule.model.endTime - cursorTimeValue) * millisecondUnitPixel
            } else {
                top =
                    cursorLinePosition + (schedule.model.startTime - cursorTimeValue) * millisecondUnitPixel
                bottom =
                    cursorLinePosition + (schedule.model.endTime - cursorTimeValue) * millisecondUnitPixel
            }

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

    private val rect = Rect()

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
                        (if (orientation == 0) (right - left) else (bottom - top)) /
                                (cardFilmHoleHeight + cardFilmHoleGap - abs(cardFilmHoleOffset))
                    for (i in 0..count.toInt()) {
                        if (orientation == 0) {
                            val aLeft =
                                left + i * cardFilmHoleWidth + cardFilmHoleOffset + i * cardFilmHoleGap
                            val aRight = aLeft + cardFilmHoleHeight
                            var aTop = top + cardFilmHoleGap
                            var aBottom = aTop + cardFilmHoleWidth

                            path.addRect(
                                min(max(aLeft, left), right),
                                aTop,
                                min(aRight, right),
                                aBottom,
                                Path.Direction.CCW
                            )

                            aBottom = bottom - cardFilmHoleGap - cardFilmHoleWidth
                            aTop = aBottom + cardFilmHoleWidth

                            path.addRect(
                                min(max(aLeft, left), right),
                                aTop,
                                min(aRight, right),
                                aBottom,
                                Path.Direction.CCW
                            )
                        } else {
                            var aLeft = left + cardFilmHoleGap
                            var aRight = aLeft + cardFilmHoleWidth
                            val aTop =
                                top + i * cardFilmHoleHeight + cardFilmHoleOffset + i * cardFilmHoleGap
                            val aBottom = aTop + cardFilmHoleHeight

                            path.addRect(
                                aLeft,
                                min(max(aTop, top), bottom),
                                aRight,
                                min(aBottom, bottom),
                                Path.Direction.CCW
                            )

                            aLeft = right - cardFilmHoleWidth - cardFilmHoleGap
                            aRight = aLeft + cardFilmHoleWidth

                            path.addRect(
                                aLeft,
                                min(max(aTop, top), bottom),
                                aRight,
                                min(aBottom, bottom),
                                Path.Direction.CCW
                            )
                        }
                    }
                }

                canvas.drawPath(path, textPaint)
            } else {
                val toBitmap = it.toBitmap()
                val bitmap = if (orientation == 0)
                    Bitmap.createScaledBitmap(
                        toBitmap,
                        (toBitmap.width * (right - left) / toBitmap.height).toInt(),
                        (bottom - top).toInt(),
                        true
                    )
                else
                    Bitmap.createScaledBitmap(
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

        val margin = dp16 / 4
        var titleHeight = 0

        textPaint.textSize = tickTextSize

        textPaint.getTextBounds("一", 0, 1, rect)
        var yiWidth = rect.width()
        textPaint.getTextBounds(schedule.title, 0, schedule.title.length, rect)
        var textWidth = rect.width()

        if (cardShowTitle) {
            textPaint.color = schedule.titleColor

            val titleLayout = StaticLayout(
                schedule.title,
                textPaint,
                max(
                    0f,
                    right - left - margin * 2 -
                            if (orientation == 1 && cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth * 2
                            else 0f
                ).toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                0f,
                true
            )

            //horizontal
            if (schedule.title.isNotEmpty() &&
                orientation == 0 &&
                right - left - margin * 2 > yiWidth &&
                (bottom - top -
                        if (cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth * 2
                        else 0f) >= titleLayout.height
            ) {
                titleHeight = titleLayout.height

                val aWidth = min(textWidth, titleLayout.width) + margin * 2

                canvas.save()
                canvas.translate(
                    if (aWidth >= right) right - aWidth
                    else max(0f, left) + margin,
                    top +
                            if (cardSimulateFilmStyle) cardFilmHoleGap + cardFilmHoleWidth
                            else 0f
                )
                titleLayout.draw(canvas)
                canvas.restore()
            }
            //vertical
            if (schedule.title.isNotEmpty() && orientation == 1 && bottom - top >= titleLayout.height) {
                titleHeight = titleLayout.height

                canvas.save()
                canvas.translate(
                    left + margin +
                            if (cardSimulateFilmStyle) cardFilmHoleGap + cardFilmHoleWidth
                            else 0f,
                    if (bottom - max(0f, top) >= titleLayout.height + margin * 2)
                        max(0f, top) + margin
                    else
                        bottom - titleLayout.height - margin
                )
                titleLayout.draw(canvas)
                canvas.restore()
            }
        }

        //region draw text
        if (cardShowText) {
            textPaint.textSize = tickTextSize * 0.9f
            textPaint.color = schedule.textColor

            textPaint.getTextBounds("一", 0, 1, rect)
            yiWidth = rect.width()
            textPaint.getTextBounds(schedule.text, 0, schedule.text.length, rect)
            textWidth = rect.width()

            val textLayout = StaticLayout(
                schedule.text,
                textPaint,
                max(
                    0f,
                    min(right, right - left) - margin * 2 -
                            if (orientation == 1 && cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth * 2
                            else 0f
                ).toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                0f,
                true
            )
            //horizontal
            if (schedule.text.isNotEmpty() &&
                orientation == 0 &&
                right - left - margin * 2 > yiWidth &&
                (bottom - top - margin * 2 - titleHeight -
                        if (cardSimulateFilmStyle) cardFilmHoleGap * 2 + cardFilmHoleWidth * 2
                        else 0f) >= textLayout.height
            ) {
                val aWidth = min(textWidth, textLayout.width) + margin * 2

                canvas.save()
                canvas.translate(
                    if (aWidth >= right) right - aWidth
                    else max(0f, left) + margin,
                    top + titleHeight +
                            if (cardSimulateFilmStyle) cardFilmHoleGap + cardFilmHoleWidth
                            else 0f
                )
                textLayout.draw(canvas)
                canvas.restore()
            }
            //vertical
            if (schedule.text.isNotEmpty() &&
                orientation == 1 &&
                bottom - max(0f, top) >= titleHeight + textLayout.height
            ) {
                canvas.save()
                canvas.translate(
                    left + margin +
                            if (cardSimulateFilmStyle) cardFilmHoleGap + cardFilmHoleWidth
                            else 0f,
                    max(0f, top) + titleHeight + margin
                )
                textLayout.draw(canvas)
                canvas.restore()
            }
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