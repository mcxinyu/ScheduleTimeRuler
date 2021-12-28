package com.mcxinyu.scheduletimeruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import androidx.annotation.ColorInt
import androidx.core.view.GestureDetectorCompat
import java.util.*
import kotlin.math.ceil
import kotlin.properties.Delegates

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class TimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {
    companion object {
        val TAG = TimeRulerView::class.java.simpleName
        const val STATUS_NONE = 0
        const val STATUS_DOWN = STATUS_NONE + 1
        const val STATUS_SCROLL = STATUS_DOWN + 1
        const val STATUS_SCROLL_FLING = STATUS_SCROLL + 1
        const val STATUS_ZOOM = STATUS_SCROLL_FLING + 1
    }

    private var scrollHappened: Boolean = false
    private var gestureDetectorCompat = GestureDetectorCompat(context, this)
    private var scaleGestureDetector = ScaleGestureDetector(context, this)
    private var scroller = Scroller(context)

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
    private lateinit var timeModel: TimeModel

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

    private var scaleRatio = 1.0f

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

        timeModel = TimeModel()
        currentTimeValue = timeModel.startTimeValue
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)

        minUnitPixel = maxTickSpace / timeModel.unitTimeValue
        maxUnitPixel = maxTickSpace

        unitPixel = minUnitPixel * scaleRatio
        tickSpacePixel = timeModel.unitTimeValue * unitPixel
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

        onDrawBaseline(canvas)

        onDrawTick(canvas)

        onDrawCursor(canvas)
    }

    private fun onDrawTick(canvas: Canvas) {
        val frontTimeRange = currentTimeValue - timeModel.startTimeValue
        val frontTimeOffset = frontTimeRange % timeModel.unitTimeValue
        val frontTimeValue = currentTimeValue - frontTimeOffset
        val frontPosition = height * cursorLinePosition - frontTimeOffset * unitPixel
        val frontCount =
            height * cursorLinePosition / (tickSpacePixel + normalTickWidth)

        //从游标线往前画
        for (i in 0 until ceil(frontCount.toDouble()).toInt()) {
            val timeValue = frontTimeValue - timeModel.unitTimeValue * i
            if (timeValue < timeModel.startTimeValue) {
                break
            }

            val x = width * baselinePosition - normalTickHeight
            val y = frontPosition - tickSpacePixel * i

            onDrawTickLine(canvas, x, y)
            onDrawTickText(canvas, x, y, timeValue)
        }

        val backTimeValue = frontTimeValue + timeModel.unitTimeValue
        val backPosition = frontPosition + tickSpacePixel
        val backCount = height * (1 - cursorLinePosition) / (tickSpacePixel + normalTickWidth)

        //从游标线往后画
        for (i in 0 until ceil(backCount.toDouble()).toInt()) {
            val timeValue = backTimeValue + timeModel.unitTimeValue * i
            if (timeValue > timeModel.endTimeValue) {
                break
            }

            val x = width * baselinePosition - normalTickHeight
            val y = backPosition + tickSpacePixel * i

            onDrawTickLine(canvas, x, y)
            onDrawTickText(canvas, x, y, timeValue)
        }
    }

    protected fun onDrawTickText(canvas: Canvas, x: Float, y: Float, timeValue: Long) {
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

    protected fun onDrawTickLine(canvas: Canvas, x: Float, y: Float) {
        if (showTick) {
            paint.color = normalTickColor
            paint.strokeWidth = normalTickWidth
            canvas.drawLine(x, y, width * baselinePosition, y, paint)
            paint.strokeWidth = 1f
        }
    }

    protected fun onDrawCursor(canvas: Canvas) {
        if (showCursorLine) {
            paint.color = cursorLineColor
            paint.strokeWidth = baselineWidth
            val top = height * cursorLinePosition
            canvas.drawLine(0f, top, width.toFloat(), top, paint)
            paint.strokeWidth = 1f
        }
    }

    protected fun onDrawBaseline(canvas: Canvas) {
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetectorCompat.onTouchEvent(event)
        return true
    }

    private var status: Int = STATUS_NONE

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        status = STATUS_ZOOM
        var scaleFactor = detector.scaleFactor
        unitPixel *= scaleFactor
        if (unitPixel > maxUnitPixel) {
            unitPixel = maxUnitPixel
            scaleFactor = 1f
        } else if (unitPixel < minUnitPixel) {
            unitPixel = minUnitPixel
            scaleFactor = 1f
        }

        onScale(timeModel, unitPixel)

        scaleRatio *= scaleFactor

        tickSpacePixel = timeModel.unitTimeValue * unitPixel

        invalidate()

        return unitPixel < maxUnitPixel || unitPixel > minUnitPixel
    }

    protected fun onScale(timeModel: TimeModel, unitPixel: Float) {

    }

    override fun onScaleBegin(detector: ScaleGestureDetector) = true

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onDown(e: MotionEvent): Boolean {
        if (status == STATUS_SCROLL_FLING) {
            scroller.forceFinished(true)
        } else {
            scrollHappened = false
        }
        status = STATUS_DOWN
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return performClick()
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float):
            Boolean {
        if (e2.pointerCount > 1) {
            return false
        }
        if (scaleGestureDetector.isInProgress) {
            return false
        }
        if (!scrollHappened) {
            scrollHappened = true
            return true
        }

        status = STATUS_SCROLL

        val increment = distanceY / unitPixel
        currentTimeValue += increment.toLong()

        var result = true
        if (currentTimeValue > timeModel.endTimeValue) {
            currentTimeValue = timeModel.endTimeValue
            result = false
        } else if (currentTimeValue < timeModel.startTimeValue) {
            currentTimeValue = timeModel.startTimeValue
            result = false
        }

        invalidate()

        return result
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float):
            Boolean {
        status = STATUS_SCROLL_FLING

        val startY = ((currentTimeValue - timeModel.startTimeValue) * unitPixel).toInt()
        val maxY = ((timeModel.endTimeValue - timeModel.startTimeValue) * unitPixel).toInt()

        scroller.fling(0, startY, 0, (-velocityY).toInt(), 0, 0, 0, maxY)

        invalidate()

        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val currY = scroller.currY
            currentTimeValue = timeModel.startTimeValue + (currY / unitPixel).toLong()
            if (currentTimeValue > timeModel.endTimeValue) {
                currentTimeValue = timeModel.endTimeValue
            } else if (currentTimeValue < timeModel.startTimeValue) {
                currentTimeValue = timeModel.startTimeValue
            }
            invalidate()
        } else {
            if (status == STATUS_SCROLL_FLING) {
                status = STATUS_DOWN
            }
        }
    }
}
