package com.mcxinyu.scheduletimeruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.mcxinyu.scheduletimeruler.ScheduleTimeRulerView

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2022/1/3.
 */
open class ScaleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TimeRulerView(context, attrs), ScaleGestureDetector.OnScaleGestureListener {

    var mode: Mode = Mode.MODE_UINT_1_MIN
        set(value) {
            if (field == value) {
                return
            }
            field = value
            var unitTimeValue = timeModel.unitTimeValue
            when (value) {
                Mode.MODE_UINT_1_MIN -> {
                    unitTimeValue = 60 * 1000
                }
                Mode.MODE_UINT_5_MIN -> {
                    unitTimeValue = 5 * 60 * 1000
                }
                Mode.MODE_UINT_15_MIN -> {
                    unitTimeValue = 15 * 60 * 1000
                }
                Mode.MODE_UINT_30_MIN -> {
                    unitTimeValue = 30 * 60 * 1000
                }
                Mode.MODE_UINT_1_HOUR -> {
                    unitTimeValue = 60 * 60 * 1000
                }
                Mode.MODE_UINT_2_HOUR -> {
                    unitTimeValue = 2 * 60 * 60 * 1000
                }
            }
            updateScaleInfo(unitTimeValue * 5, unitTimeValue)
        }

    /**
     * 每毫秒最大占有像素，由[maxTickSpace]分成一分钟毫秒数得到
     */
    protected var maxMillisecondUnitPixel: Float

    /**
     * 每毫秒最小占有像素
     */
    protected var minMillisecondUnitPixel: Float

    protected var scaleRatio = 1.0f

    private var scaleGestureDetector = ScaleGestureDetector(context, this)

    init {
        //[maxTickSpace]分成一分钟的毫秒数
        maxMillisecondUnitPixel = maxTickSpace / timeModel.unitTimeValue
        //[maxUnitPixel]缩小 360(分钟)倍
        minMillisecondUnitPixel = maxMillisecondUnitPixel / (60 * 3)
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)

        millisecondUnitPixel = maxMillisecondUnitPixel * scaleRatio
        tickSpacePixel = timeModel.unitTimeValue * millisecondUnitPixel
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        status = STATUS_ZOOM
        var scaleFactor = detector.scaleFactor
        millisecondUnitPixel *= scaleFactor
        if (millisecondUnitPixel > maxMillisecondUnitPixel) {
            millisecondUnitPixel = maxMillisecondUnitPixel
            scaleFactor = 1f
        } else if (millisecondUnitPixel < minMillisecondUnitPixel) {
            millisecondUnitPixel = minMillisecondUnitPixel
            scaleFactor = 1f
        }

        Log.d(
            TAG,
            """
            ${"%2f".format(maxMillisecondUnitPixel)}/${"%2f".format(millisecondUnitPixel)}=${
                "%2f".format(
                    maxMillisecondUnitPixel / millisecondUnitPixel
                )
            }
        """.trimIndent()
        )

        onScale(timeModel, millisecondUnitPixel)

        scaleRatio *= scaleFactor

        tickSpacePixel = timeModel.unitTimeValue * millisecondUnitPixel

        invalidate()

        return millisecondUnitPixel < maxMillisecondUnitPixel || millisecondUnitPixel > minMillisecondUnitPixel
    }

    protected fun onScale(timeModel: TimeModel, unitPixel: Float) {

    }

    override fun onScaleBegin(detector: ScaleGestureDetector) = true

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float):
            Boolean {
        if (e2.pointerCount > 1) {
            return false
        }
        if (scaleGestureDetector.isInProgress) {
            return false
        }
        if (status == STATUS_ZOOM) {
            return false
        }

        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float):
            Boolean {
        if (status == STATUS_ZOOM) {
            return false
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    /**
     * 缩放
     *
     * @param keyUnitTimeValue Long
     * @param unitTimeValue Long
     */
    protected fun updateScaleInfo(keyUnitTimeValue: Long, unitTimeValue: Long) {
        timeModel.keyUnitTimeValue = keyUnitTimeValue
        timeModel.unitTimeValue = unitTimeValue

        scaleRatio = (60 * 1000f) / timeModel.unitTimeValue

        invalidate()
    }

    companion object {
        val TAG = ScaleTimeRulerView::class.java.simpleName

        const val STATUS_ZOOM = STATUS_SCROLL_FLING + 1
    }

    enum class Mode {
        MODE_UINT_1_MIN,
        MODE_UINT_5_MIN,
        MODE_UINT_15_MIN,
        MODE_UINT_30_MIN,
        MODE_UINT_1_HOUR,
        MODE_UINT_2_HOUR,
    }
}