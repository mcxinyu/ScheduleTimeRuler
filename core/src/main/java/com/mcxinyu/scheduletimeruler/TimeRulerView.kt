package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import java.util.*
import kotlin.math.ceil
import kotlin.properties.Delegates

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class TimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    @ColorInt
    private var tickTextColor: Int
    private var tickTextSize: Float
    private var showTickText: Boolean

    private var showTick: Boolean
    private var maxTickSpace: Float

    @ColorInt
    private var normalTickColor: Int
    private var normalTickWidth: Float
    private var normalTickHeight: Float

    @ColorInt
    private var keyTickColor: Int
    private var keyTickWidth: Float
    private var keyTickHeight: Float

    @ColorInt
    private var cursorLineColor: Int
    private var cursorLineWidth: Float
    private var cursorLinePosition: Float
    private var showCursorLine: Boolean

    @ColorInt
    private var baselineColor: Int
    private var baselineWidth: Float
    private var baselinePosition: Float
    private var showBaseline: Boolean

    private lateinit var paint: Paint
    private lateinit var infoModel: TimeModel

    /**
     * 游标所在位置时间
     */
    private var currentTimeValue = 0L

    private var minUnitPixel by Delegates.notNull<Float>()
    private var maxUnitPixel by Delegates.notNull<Float>()

    /**
     * 单位时间占用像素 区间 [[minUnitPixel], [maxUnitPixel]]
     */
    private var unitPixel by Delegates.notNull<Float>()

    private var tickSpacePixel by Delegates.notNull<Float>()

    private val scaleRatio = 1.0f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerView)
        showBaseline =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showBaseline, true)
        baselinePosition =
            typedArray.getFloat(R.styleable.TimeRulerView_trv_baselinePosition, 0.3f)
        baselineColor =
            typedArray.getColor(R.styleable.TimeRulerView_trv_baselineColor, Color.LTGRAY)
        baselineWidth = typedArray.getDimension(
            R.styleable.TimeRulerView_trv_baselineWidth,
            1.toPx(context)
        )

        showCursorLine =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showCursorLine, true)
        cursorLinePosition =
            typedArray.getFloat(R.styleable.TimeRulerView_trv_cursorLinePosition, 0.3f)
        cursorLineColor =
            typedArray.getColor(
                R.styleable.TimeRulerView_trv_cursorLineColor,
                Color.BLUE
            )
        cursorLineWidth = typedArray.getDimension(
            R.styleable.TimeRulerView_trv_cursorLineWidth,
            1.toPx(context)
        )

        keyTickHeight =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_keyTickHeight,
                16.toPx(context)
            )
        keyTickWidth =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_keyTickWidth,
                1.toPx(context)
            )
        keyTickColor =
            typedArray.getColor(
                R.styleable.TimeRulerView_trv_keyTickColor,
                Color.GRAY
            )

        normalTickHeight =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_normalTickHeight,
                8.toPx(context)
            )
        normalTickWidth =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_normalTickWidth,
                1.toPx(context)
            )
        normalTickColor =
            typedArray.getColor(
                R.styleable.TimeRulerView_trv_normalTickColor,
                Color.LTGRAY
            )

        showTick =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showTick, true)
        maxTickSpace =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_maxTickSpace,
                80.toPx(context)
            )

        showTickText =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showTickText, true)
        tickTextSize =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_tickTextSize,
                14.toPxForSp(context)
            )
        tickTextColor =
            typedArray.getColor(
                R.styleable.TimeRulerView_trv_tickTextColor,
                Color.DKGRAY
            )

        typedArray.recycle()

        initThing()
    }

    private fun initThing() {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL_AND_STROKE

        infoModel = TimeModel()
        currentTimeValue = infoModel.startTimeValue
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)

        minUnitPixel = maxTickSpace / infoModel.unitTimeValue
        maxUnitPixel = maxTickSpace

        unitPixel = minUnitPixel * scaleRatio
        tickSpacePixel = infoModel.unitTimeValue * unitPixel
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        setMeasuredDimension(
//            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), getHeightSize(
//                suggestedMinimumHeight, heightMeasureSpec
//            )
//        )
    }

    private fun getHeightSize(size: Int, heightMeasureSpec: Int): Int {
        var result = size
        val contentHeight: Int = calculateContentWidth(baselinePosition)
        val specMode = MeasureSpec.getMode(heightMeasureSpec)
        val specSize = MeasureSpec.getSize(heightMeasureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = if (size > contentHeight) size else contentHeight
            MeasureSpec.AT_MOST -> result = contentHeight
            MeasureSpec.EXACTLY -> result =
                if (specSize > contentHeight) specSize else contentHeight
        }
        return result
    }

    protected fun calculateContentWidth(baselinePositionProportion: Float): Int {
        var tickValueHeight = 0
        if (showTickText) {
            //让字体不要溢出，这里应该动态换算基线左边还有位置显示文本么？
            paint.setTextSize(20.toPx(context))
            val fontMetrics: Paint.FontMetrics = paint.getFontMetrics()
            val ceil = Math.ceil((fontMetrics.bottom - fontMetrics.top).toDouble())
            tickValueHeight = ceil.toInt()
        }
        return ((keyTickHeight + tickValueHeight) / baselinePositionProportion + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBaseline(canvas)

        drawTick(canvas)

        drawCursor(canvas)
    }

    private fun drawTick(canvas: Canvas) {
        val frontTimeRange = currentTimeValue - infoModel.startTimeValue
        val frontTimeOffset = frontTimeRange % infoModel.unitTimeValue
        val frontTimeValue = currentTimeValue - frontTimeOffset
        val frontPosition = height * cursorLinePosition - frontTimeOffset * unitPixel
        val frontCount =
            height * cursorLinePosition / (tickSpacePixel + normalTickWidth)

        //从游标线往前画
        for (i in 0 until ceil(frontCount.toDouble()).toInt()) {
            val timeValue = frontTimeValue - infoModel.unitTimeValue * i
            if (timeValue < infoModel.startTimeValue) {
                break
            }

            val x = width * baselinePosition - normalTickHeight
            val y = frontPosition - tickSpacePixel * i

            drawTickLine(canvas, x, y)
            drawTickText(canvas, x, y, timeValue)
        }

        val backTimeValue = frontTimeValue + infoModel.unitTimeValue
        val backPosition = frontPosition + tickSpacePixel
        val backCount = height * (1 - cursorLinePosition) / (tickSpacePixel + normalTickWidth)

        //从游标线往后画
        for (i in 0 until ceil(backCount.toDouble()).toInt()) {
            val timeValue = backTimeValue + infoModel.unitTimeValue * i
            if (timeValue > infoModel.endTimeValue) {
                break
            }

            val x = width * baselinePosition - normalTickHeight
            val y = backPosition + tickSpacePixel * i

            drawTickLine(canvas, x, y)
            drawTickText(canvas, x, y, timeValue)
        }
    }

    protected fun drawTickText(canvas: Canvas, x: Float, y: Float, timeValue: Long) {
        if (showTickText) {
            paint.color = tickTextColor
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = tickTextSize

            val text = simpleDateFormat.format(timeValue)
            Log.d(TAG, "simpleDateFormat2.format(timeValue) ${simpleDateFormat2.format(timeValue)}")

            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            val w = rect.width()
            val h = rect.height()

            canvas.drawText(text, x - w - h, y + h / 2, paint)
        }
    }

    protected fun drawTickLine(canvas: Canvas, x: Float, y: Float) {
        if (showTick) {
            paint.color = normalTickColor
            paint.strokeWidth = normalTickWidth
            canvas.drawLine(x, y, width * baselinePosition, y, paint)
            paint.strokeWidth = 1f
        }
    }

    protected fun drawCursor(canvas: Canvas) {
        if (showCursorLine) {
            paint.color = cursorLineColor
            paint.strokeWidth = baselineWidth
            val top = height * cursorLinePosition
            canvas.drawLine(0f, top, width.toFloat(), top, paint)
            paint.strokeWidth = 1f
        }
    }

    protected fun drawBaseline(canvas: Canvas) {
        if (showBaseline) {
            paint.color = baselineColor
            paint.strokeWidth = baselineWidth
            canvas.drawLine(
                width * baselinePosition,
                0f,
                width * baselinePosition,
                height.toFloat(),
                paint
            )
            paint.strokeWidth = 1f
        }
    }

    companion object {
        val TAG = TimeRulerView::class.java.simpleName
    }
}
