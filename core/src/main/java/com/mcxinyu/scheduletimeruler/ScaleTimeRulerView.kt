package com.mcxinyu.scheduletimeruler

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.FloatRange
import com.mcxinyu.scheduletimeruler.model.TimeModel
import kotlin.math.min

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2022/1/3.
 */
open class ScaleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TimeRulerView(context, attrs), ScaleGestureDetector.OnScaleGestureListener {

    var scaleLevel: Level = Level.LEVEL_UNIT_1_MIN
        set(value) {
            if (field == value) {
                return
            }
            field = value
            var unitTimeValue = timeModel.unitTimeValue
            when (value) {
                Level.LEVEL_UNIT_1_MIN -> {
                    unitTimeValue = 60 * 1000
                }
                Level.LEVEL_UNIT_5_MIN -> {
                    unitTimeValue = 5 * 60 * 1000
                }
                Level.LEVEL_UNIT_15_MIN -> {
                    unitTimeValue = 15 * 60 * 1000
                }
                Level.LEVEL_UNIT_30_MIN -> {
                    unitTimeValue = 30 * 60 * 1000
                }
                Level.LEVEL_UNIT_1_HOUR -> {
                    unitTimeValue = 60 * 60 * 1000
                }
                Level.LEVEL_UNIT_2_HOUR -> {
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
//        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleTimeRulerView)
//
//        val level = typedArray.getInt(R.styleable.ScaleTimeRulerView_strv_scaleLevel, 1)
//
////            scaleLevel = when (level) {
////                1 -> Level.LEVEL_UNIT_1_MIN
////                5 -> Level.LEVEL_UNIT_5_MIN
////                15 -> Level.LEVEL_UNIT_15_MIN
////                30 -> Level.LEVEL_UNIT_30_MIN
////                60 -> Level.LEVEL_UNIT_1_HOUR
////                120 -> Level.LEVEL_UNIT_2_HOUR
////                else -> Level.LEVEL_UNIT_1_MIN
////            }
//
//        typedArray.recycle()

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
        scale(detector.scaleFactor)

        return millisecondUnitPixel < maxMillisecondUnitPixel || millisecondUnitPixel > minMillisecondUnitPixel
    }

    private fun scale(scaleFactor: Float) {
        status = OnScrollListener.STATUS_ZOOM

        Log.d(TAG, "scaleFactor $scaleFactor")
        var factor = scaleFactor

        millisecondUnitPixel *= factor
        if (millisecondUnitPixel > maxMillisecondUnitPixel) {
            millisecondUnitPixel = maxMillisecondUnitPixel
            factor = 1f
        } else if (millisecondUnitPixel < minMillisecondUnitPixel) {
            millisecondUnitPixel = minMillisecondUnitPixel
            factor = 1f
        }

        scaleLevel = when {
            maxMillisecondUnitPixel / millisecondUnitPixel > 140 -> Level.LEVEL_UNIT_2_HOUR
            maxMillisecondUnitPixel / millisecondUnitPixel > 70 -> Level.LEVEL_UNIT_1_HOUR
            maxMillisecondUnitPixel / millisecondUnitPixel > 30 -> Level.LEVEL_UNIT_30_MIN
            maxMillisecondUnitPixel / millisecondUnitPixel > 11 -> Level.LEVEL_UNIT_15_MIN
            maxMillisecondUnitPixel / millisecondUnitPixel > 3 -> Level.LEVEL_UNIT_5_MIN
            else -> Level.LEVEL_UNIT_1_MIN
        }

        onScale(timeModel, millisecondUnitPixel)

        scaleRatio *= factor

        tickSpacePixel = timeModel.unitTimeValue * millisecondUnitPixel

        invalidate()
    }

    private var lastScale = 0f
    fun setScale(@FloatRange(from = 0.0, to = 1000.0) scale: Float) {
        var scaleFactor = 0.9f + scale / 1000f
        if (lastScale < scale) {
            scaleFactor = 1 + min(1000f, scale) / 1000f
        }
        scale(scaleFactor)
        lastScale = scale
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
        if (status == OnScrollListener.STATUS_ZOOM) {
            return false
        }

        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float):
            Boolean {
        if (status == OnScrollListener.STATUS_ZOOM) {
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
    }

    enum class Level {
        LEVEL_UNIT_1_MIN,
        LEVEL_UNIT_5_MIN,
        LEVEL_UNIT_15_MIN,
        LEVEL_UNIT_30_MIN,
        LEVEL_UNIT_1_HOUR,
        LEVEL_UNIT_2_HOUR,
    }
}