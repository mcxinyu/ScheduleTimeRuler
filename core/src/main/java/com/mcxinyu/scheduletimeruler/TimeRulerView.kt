package com.mcxinyu.scheduletimeruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import com.mcxinyu.scheduletimeruler.model.TimeModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.properties.Delegates


/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
open class TimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), GestureDetector.OnGestureListener {

    protected var typeface: Typeface? = null

    @ColorInt
    var tickTextColor: Int
    var tickTextSize: Float
    var showTickText: Boolean

    var showTick: Boolean
    var maxTickSpace: Float

    @ColorInt
    var keyTickColor: Int
    var keyTickWidth: Float
    var keyTickHeight: Float

    @ColorInt
    var cursorLineColor: Int
    var cursorLineWidth: Float
    var cursorLinePositionPercentage: Float
    var cursorLinePosition by Delegates.notNull<Float>()
    var showCursorText: Boolean
    var showCursorLine: Boolean

    @ColorInt
    var baselineOutDayColor: Int

    @ColorInt
    var baselineColor: Int
    var baselineWidth: Float
    var baselinePositionPercentage: Float
    var baselinePosition by Delegates.notNull<Float>()
    var showBaseline: Boolean

    private val paint = Paint()

    protected var timeModel = TimeModel()
        private set

    /**
     * 游标所在位置时间
     */
    var cursorTimeValue = System.currentTimeMillis()
        set(value) {
            field = when {
                value < timeModel.startTimeValue -> timeModel.startTimeValue
                value > timeModel.endTimeValue -> timeModel.endTimeValue
                else -> value
            }
            onCursorListener?.onProgressChanged(cursorTimeValue)
            invalidate()
        }

    /**
     * 每毫秒时间占用像素
     */
    protected var millisecondUnitPixel by Delegates.notNull<Float>()

    /**
     * 每格占用像素
     */
    protected var tickSpacePixel by Delegates.notNull<Float>()

    protected var scrollHappened: Boolean = false
    protected var gestureDetectorCompat = GestureDetectorCompat(context, this)
    protected var status: Int = OnScrollListener.STATUS_IDLE
        set(value) {
            field = value
            onScrollListener?.onScrollStateChanged(status)
        }

    protected var scroller = Scroller(context)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerView)

        try {
            val font = typedArray.getResourceId(R.styleable.TimeRulerView_trv_font, 0)
            typeface = ResourcesCompat.getFont(context, font)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showBaseline =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showBaseline, true)
        baselinePositionPercentage =
            typedArray.getFloat(R.styleable.TimeRulerView_trv_baselinePosition, 0.25f)
        baselineColor =
            typedArray.getColor(R.styleable.TimeRulerView_trv_baselineColor, Color.LTGRAY)
        baselineOutDayColor =
            typedArray.getColor(
                R.styleable.TimeRulerView_trv_baselineOutDayColor,
                Color.parseColor("#FFFAFAFA")
            )
        baselineWidth = typedArray.getDimension(
            R.styleable.TimeRulerView_trv_baselineWidth,
            1.toPx(context)
        )

        showCursorLine =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showCursorLine, true)
        showCursorText =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showCursorText, true)
        cursorLinePositionPercentage =
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
                8.toPx(context)
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

        showTick =
            typedArray.getBoolean(R.styleable.TimeRulerView_trv_showTick, true)
        maxTickSpace =
            typedArray.getDimension(
                R.styleable.TimeRulerView_trv_minTickSpace,
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
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL_AND_STROKE
        typeface?.let { paint.typeface = typeface }
    }

    /**
     * 设置时间范围，接受同一天的时间
     *
     * @param start Long
     * @param end Long
     */
    fun setRange(start: Long, end: Long) {
        if (start >= end) {
            throw IllegalArgumentException("the start time must be greater than the end time")
        }

        val sCalendar = Calendar.getInstance().apply {
            time = Date(start)
        }
        val eCalendar = Calendar.getInstance().apply {
            time = Date(end)
        }

//        if (eCalendar.timeInMillis - sCalendar.timeInMillis > 1000 * 60 * 60 * 24) {
//            throw IllegalArgumentException("The difference between the start time and the end time cannot be more than 24 hours")
//        }

        timeModel.startTimeValue = start
        timeModel.endTimeValue = end

        cursorTimeValue = cursorTimeValue
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)

        cursorLinePosition = height * cursorLinePositionPercentage
        baselinePosition = width * baselinePositionPercentage

        millisecondUnitPixel = maxTickSpace / timeModel.unitTimeValue
        tickSpacePixel = timeModel.unitTimeValue * millisecondUnitPixel
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
        val contentHeight: Int = calculateContentWidth()
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

    protected fun calculateContentWidth(): Int {
        var tickValueHeight = 0
        if (showTickText) {
            //让字体不要溢出，这里应该动态换算基线左边还有位置显示文本么？
            paint.textSize = 20.toPx(context)
            val fontMetrics: Paint.FontMetrics = paint.getFontMetrics()
            val ceil = ceil((fontMetrics.bottom - fontMetrics.top).toDouble())
            tickValueHeight = ceil.toInt()
        }
        return ((keyTickHeight + tickValueHeight) / baselinePositionPercentage + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        onDrawBaseline(canvas)

        onDrawTick(canvas)

        onDrawCursor(canvas)
    }

    private val rect = Rect()

    /**
     * 画刻度
     * @param canvas Canvas
     */
    protected open fun onDrawTick(canvas: Canvas) {
        //在游标线以上包含的今日时间
        val frontTodayTimeRange = cursorTimeValue - timeModel.startTimeValue
        //可见的第一个刻度的时间在屏幕外的偏移量
        val frontFirstTimeOffset = frontTodayTimeRange % timeModel.unitTimeValue
        //向前走，距离游标线最近的刻度代表的时间
        val frontLastTimeOffsetValue = cursorTimeValue - frontFirstTimeOffset
        //向前走，距离游标线最近的刻度所在位置
        val frontLastTimePosition = cursorLinePosition - frontFirstTimeOffset * millisecondUnitPixel
        val frontCount = cursorLinePosition / tickSpacePixel

        //从游标线往前画
        for (i in 0..ceil(frontCount.toDouble()).toInt()) {
            val timeValue = frontLastTimeOffsetValue - timeModel.unitTimeValue * i
            if (timeValue < timeModel.startTimeValue) {
                break
            }

            val x = baselinePosition - keyTickHeight
            val y = frontLastTimePosition - tickSpacePixel * i

            onDrawTickBaseline(
                canvas,
                baselinePosition,
                y,
                baselinePosition + baselineWidth,
                min(y + tickSpacePixel, cursorLinePosition),
                timeValue
            )

            onDrawTickLine(canvas, x, y, timeValue)
            onDrawTickText(canvas, x, y, timeValue)
        }

        val backFirstTimeValue = frontLastTimeOffsetValue + timeModel.unitTimeValue
        val backFirstTimePosition = frontLastTimePosition + tickSpacePixel
        val backCount = (height - cursorLinePosition) / tickSpacePixel

        //从游标线往后画
        for (i in 0..ceil(backCount.toDouble()).toInt()) {
            val timeValue = backFirstTimeValue + timeModel.unitTimeValue * i

            val x = baselinePosition - keyTickHeight
            val y = backFirstTimePosition + tickSpacePixel * i

            onDrawTickBaseline(
                canvas,
                baselinePosition,
                min(y, cursorLinePosition),
                baselinePosition + baselineWidth,
                y + min(
                    tickSpacePixel,
                    (timeModel.endTimeValue - timeValue) * millisecondUnitPixel
                ),
                timeValue
            )
            if (timeValue <= timeModel.endTimeValue) {
                onDrawTickLine(canvas, x, y, timeValue)
                onDrawTickText(canvas, x, y, timeValue)
            }

            if (timeValue >= timeModel.endTimeValue) {
                break
            }
        }
    }

    protected open fun onDrawTickText(canvas: Canvas, x: Float, y: Float, timeValue: Long) {
        if (showTickText) {
            paint.color = tickTextColor
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = tickTextSize

            val text = onGetSimpleDateFormat().format(timeValue)

            paint.getTextBounds(text, 0, text.length, rect)
            val w = rect.width()
            val h = rect.height()

            canvas.drawText(text, x - w - h, y + h / 2, paint)
        }
    }

    protected open fun onDrawTickLine(canvas: Canvas, x: Float, y: Float, timeValue: Long) {
        if (showTick) {
            paint.color = keyTickColor
            paint.strokeWidth = keyTickWidth
            canvas.drawLine(x, y, baselinePosition, y, paint)
            paint.strokeWidth = 1f
        }
    }

    protected open fun onDrawCursor(canvas: Canvas) {
        if (showCursorLine) {
            paint.color = cursorLineColor
            paint.strokeWidth = cursorLineWidth
            canvas.drawLine(0f, cursorLinePosition, width.toFloat(), cursorLinePosition, paint)
            paint.strokeWidth = 1f
        }
        if (showCursorText) {
            val text = simpleDateFormat2.format(cursorTimeValue)

            paint.getTextBounds(text, 0, text.length, rect)

//            val x = width * baselinePosition - normalTickHeight

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(text, width / 2f, cursorLinePosition - rect.height(), paint)
        }
    }

    protected open fun onDrawTickBaseline(
        canvas: Canvas,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        timeValue: Long
    ) {
        if (showBaseline) {
            paint.color = baselineColor

            rect.set(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
            canvas.drawRect(rect, paint)
        }
    }

    protected open fun onDrawBaseline(canvas: Canvas) {
        if (showBaseline) {
            paint.color = baselineOutDayColor

            rect.set(
                baselinePosition.toInt(),
                0,
                (baselinePosition + baselineWidth).toInt(),
                height
            )
            canvas.drawRect(rect, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetectorCompat.onTouchEvent(event)
        if (status != OnScrollListener.STATUS_SCROLL_FLING) {
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                status = OnScrollListener.STATUS_IDLE
            }
        }
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        if (status == OnScrollListener.STATUS_SCROLL_FLING) {
            scroller.forceFinished(true)
        } else {
            scrollHappened = false
        }
        status = OnScrollListener.STATUS_DOWN
        return true
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean = performClick()

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float):
            Boolean {
        if (e2.pointerCount > 1) {
            return false
        }
        if (!scrollHappened) {
            scrollHappened = true
            return true
        }

        status = OnScrollListener.STATUS_SCROLL

        val increment = distanceY / millisecondUnitPixel
        cursorTimeValue += increment.toLong()

        var result = true
        if (cursorTimeValue > timeModel.endTimeValue) {
            cursorTimeValue = timeModel.endTimeValue
            result = false
        } else if (cursorTimeValue < timeModel.startTimeValue) {
            cursorTimeValue = timeModel.startTimeValue
            result = false
        }

        onScrollListener?.onScrolled(distanceX.toInt(), distanceY.toInt())

        invalidate()

        return result
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float):
            Boolean {
        status = OnScrollListener.STATUS_SCROLL_FLING

        val startY = ((cursorTimeValue - timeModel.startTimeValue) * millisecondUnitPixel).toInt()
        val maxY =
            ((timeModel.endTimeValue - timeModel.startTimeValue) * millisecondUnitPixel).toInt()

        scroller.fling(
            0, startY,
            -velocityX.toInt(), -velocityY.toInt(),
            0, 0,
            0, maxY
        )
        scroller.isFinished
        invalidate()

        return true
    }

    private var currX: Int? = null
    private var currY: Int? = null
    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val cX = scroller.currX
            val cY = scroller.currY
            cursorTimeValue = timeModel.startTimeValue + (cY / millisecondUnitPixel).toLong()
            if (cursorTimeValue > timeModel.endTimeValue) {
                cursorTimeValue = timeModel.endTimeValue
            } else if (cursorTimeValue < timeModel.startTimeValue) {
                cursorTimeValue = timeModel.startTimeValue
            }
            if (currY != null && currX != null) {
                onScrollListener?.onScrolled(scroller.currY - currY!!, scroller.currX - currX!!)
            }
            currX = scroller.currX
            currY = scroller.currY
            invalidate()
        } else {
            if (status == OnScrollListener.STATUS_SCROLL_FLING) {
                status = OnScrollListener.STATUS_IDLE
            }
        }
    }

    open fun onGetSimpleDateFormat() = simpleDateFormat

    companion object {
        val TAG = TimeRulerView::class.java.simpleName
    }

    var onCursorListener: OnCursorListener? = null

    interface OnCursorListener {
        fun onProgressChanged(cursorTimeValue: Long)
    }

    var onScrollListener: OnScrollListener? = null

    interface OnScrollListener {
        companion object {
            //0
            const val STATUS_IDLE = 0

            //1
            const val STATUS_DOWN = STATUS_IDLE + 1

            //2
            const val STATUS_SCROLL = STATUS_DOWN + 1

            //3
            const val STATUS_SCROLL_FLING = STATUS_SCROLL + 1

            //4
            const val STATUS_ZOOM = STATUS_SCROLL_FLING + 1
        }

        /**
         *
         * @param newState     The updated scroll state. One of [STATUS_IDLE],
         * [STATUS_DOWN] or [STATUS_SCROLL] or [STATUS_SCROLL_FLING] or STATUS_ZOOM
         */
        fun onScrollStateChanged(newState: Int)

        /**
         *
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        fun onScrolled(dx: Int, dy: Int)
    }
}

