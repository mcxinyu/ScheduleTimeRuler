package com.mcxinyu.scheduletimerulerdemo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.mcxinyu.scheduletimeruler.ScaleTimeRulerView
import com.mcxinyu.scheduletimeruler.ScheduleTimeRulerView.OnCardClickListener
import com.mcxinyu.scheduletimeruler.TimeRulerView
import com.mcxinyu.scheduletimeruler.model.CardModel
import com.mcxinyu.scheduletimerulerdemo.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var inflate: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)

//        testBase()

        testScale()

        testSchedule()
    }

    @SuppressLint("SimpleDateFormat")
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private fun testSchedule() {
        val instance = Calendar.getInstance()

        inflate.timeRuler.setData(
            listOf(
                CardModel(
                    "噫噫噫",
                    "",
                    0,
                    0,
                    background = R.drawable.ic_launcher_background
                ).apply {
                    instance[Calendar.HOUR_OF_DAY] = 9
                    instance[Calendar.MINUTE] = 20
                    instance[Calendar.SECOND] = 40
                    startTime = instance.timeInMillis

                    instance[Calendar.HOUR_OF_DAY] = 11
                    instance[Calendar.MINUTE] = 0
                    instance[Calendar.SECOND] = 50
                    endTime = instance.timeInMillis

                    text =
                        "${simpleDateFormat.format(startTime)}~${simpleDateFormat.format(endTime)}"
                },
                CardModel("尔尔尔", "", 0, 0).apply {
                    instance[Calendar.HOUR_OF_DAY] = 11
                    instance[Calendar.MINUTE] = 30
                    instance[Calendar.SECOND] = 50
                    startTime = instance.timeInMillis

                    instance[Calendar.HOUR_OF_DAY] = 14
                    instance[Calendar.MINUTE] = 0
                    instance[Calendar.SECOND] = 20
                    endTime = instance.timeInMillis

                    text =
                        "${simpleDateFormat.format(startTime)}~${simpleDateFormat.format(endTime)}"
                },
                CardModel("伞伞伞", "", 0, 0).apply {
                    instance[Calendar.HOUR_OF_DAY] = 14
                    instance[Calendar.MINUTE] = 14
                    instance[Calendar.SECOND] = 20
                    startTime = instance.timeInMillis

                    instance[Calendar.HOUR_OF_DAY] = 16
                    instance[Calendar.MINUTE] = 8
                    instance[Calendar.SECOND] = 40
                    endTime = instance.timeInMillis

                    text =
                        "${simpleDateFormat.format(startTime)}~${simpleDateFormat.format(endTime)}"
                },
                CardModel("丝丝丝", "", 0, 0).apply {
                    instance[Calendar.HOUR_OF_DAY] = 16
                    instance[Calendar.MINUTE] = 48
                    instance[Calendar.SECOND] = 40
                    startTime = instance.timeInMillis

                    instance[Calendar.HOUR_OF_DAY] = 17
                    instance[Calendar.MINUTE] = 0
                    instance[Calendar.SECOND] = 40
                    endTime = instance.timeInMillis

//                    text =
//                        "${simpleDateFormat.format(startTime)}~${simpleDateFormat.format(endTime)}"
                },
            )
        )
        inflate.timeRuler.onCardClickListener = object : OnCardClickListener {
            override fun onClick(model: CardModel) {
//                Toast.makeText(this@MainActivity, model.title, Toast.LENGTH_SHORT).show()

//                val downtime = SystemClock.uptimeMillis()
//
//                val obtain1_0 = MotionEvent.obtain(downtime, downtime, MotionEvent.ACTION_MOVE, 0f, 0f, 0)
//                val obtain1_1 = MotionEvent.obtain(downtime, downtime, MotionEvent.ACTION_MOVE, 100f, 100f, 0)
//                inflate.timeRuler.onTouchEvent(obtain1_0)
//                inflate.timeRuler.onTouchEvent(obtain1_1)
//                obtain1_0.recycle()
//                obtain1_1.recycle()
            }
        }
        inflate.timeRuler.onScrollListener = object : TimeRulerView.OnScrollListener {
            override fun onScrollStateChanged(newState: Int) {
                Log.d(TAG, "newState $newState")
            }

            override fun onScrolled(dx: Int, dy: Int) {
                Log.d(TAG, "onScrolled $dx $dy")
            }
        }
    }

    private fun testScale() {
        inflate.timeRuler.scaleLevel = ScaleTimeRulerView.Level.LEVEL_UNIT_1_HOUR
        inflate.searchBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                inflate.timeRuler.setScale(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
//        inflate.timeRuler.onScrollListener = object : TimeRulerView.OnScrollListener {
//            override fun onScrollStateChanged(newState: Int) {
//            }
//
//            override fun onScrolled(dx: Int, dy: Int) {
//            }
//        }
        inflate.imageViewIn.setOnClickListener {
            val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(
                1f,
                when (inflate.timeRuler.scaleLevel) {
                    ScaleTimeRulerView.Level.LEVEL_UNIT_2_HOUR -> 40f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_1_HOUR -> 50f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_30_MIN -> 50f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_15_MIN -> 70f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_5_MIN -> 70f
                    else -> 70f
                }
            )
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 600
            valueAnimator.addUpdateListener {
                inflate.timeRuler.setScale(it.animatedValue as Float)
            }
            valueAnimator.start()
        }
        inflate.imageViewOut.setOnClickListener {
            val scaleLevel = inflate.timeRuler.scaleLevel
            val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(
                when (scaleLevel) {
                    ScaleTimeRulerView.Level.LEVEL_UNIT_2_HOUR -> 100f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_1_HOUR -> 100f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_30_MIN -> 100f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_15_MIN -> 100f
                    ScaleTimeRulerView.Level.LEVEL_UNIT_5_MIN -> 100f
                    else -> 100f
                },
                1f
            )
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 600
            valueAnimator.addUpdateListener {
                inflate.timeRuler.setScale(it.animatedValue as Float)
            }
            valueAnimator.start()
        }
    }

    private fun testBase() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, -1)
        }
        inflate.timeRuler.cursorTimeValue = calendar.timeInMillis
        val start = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, 6)
        calendar.add(Calendar.MINUTE, 15)
        val end = calendar.timeInMillis
        inflate.timeRuler.setRange(start, end)

//        inflate.timeRuler.onCursorListener = object : TimeRulerView.OnCursorListener {
//            override fun onProgressChanged(cursorTimeValue: Long) {
//                inflate.textViewCursor.text = simpleDateFormat2.format(cursorTimeValue)
//            }
//        }
//        inflate.textViewCursor.text = simpleDateFormat2.format(inflate.timeRuler.cursorTimeValue)
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }
}