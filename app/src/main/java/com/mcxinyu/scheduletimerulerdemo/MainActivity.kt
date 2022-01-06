package com.mcxinyu.scheduletimerulerdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mcxinyu.scheduletimeruler.ScaleTimeRulerView
import com.mcxinyu.scheduletimeruler.ScheduleTimeRulerView.OnCardClickListener
import com.mcxinyu.scheduletimeruler.model.CardModel
import com.mcxinyu.scheduletimerulerdemo.databinding.ActivityMainBinding
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

    private fun testSchedule() {
        inflate.timeRuler.setDatas(
            listOf(
                CardModel(
                    "噫噫噫",
                    "2022-01-05T09:20:41~11:00:54",
                    1641345641000,
                    1641351654000,
                ),
                CardModel(
                    "尔尔尔",
                    "2022-01-05T11:30:54~14:00:28",
                    1641353454000,
                    1641362428000,
                ),
                CardModel(
                    "伞伞伞",
                    "2022-01-05T14:14:28~16:08:48",
                    1641363268000,
                    1641370128000,
                ),
                CardModel(
                    "丝丝丝",
                    "2022-01-05T16:48:48~17:00:43",
                    1641372528000,
                    1641373243000,
                ),
            )
        )
        inflate.timeRuler.setOnCardClickListener(object : OnCardClickListener {
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
        })
    }

    private fun testScale() {
        inflate.timeRuler.scaleLevel = ScaleTimeRulerView.Level.LEVEL_UNIT_1_HOUR
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

//        inflate.timeRuler.setOnCursorListener(object : TimeRulerView.OnCursorListener {
//            override fun onProgressChanged(cursorTimeValue: Long) {
//                inflate.textViewCursor.text = simpleDateFormat2.format(cursorTimeValue)
//            }
//        })
//        inflate.textViewCursor.text = simpleDateFormat2.format(inflate.timeRuler.cursorTimeValue)
    }
}