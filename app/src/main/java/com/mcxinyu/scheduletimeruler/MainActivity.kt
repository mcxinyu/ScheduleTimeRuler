package com.mcxinyu.scheduletimeruler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mcxinyu.scheduletimeruler.databinding.ActivityMainBinding
import com.mcxinyu.scheduletimeruler.model.ScheduleModel
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
        inflate.timeRuler.schedules = listOf(
            ScheduleModel(
                "120分钟",
                "2022-01-05T09:20:41~11:00:54",
                1641345641000,
                1641351654000
            ),
            ScheduleModel(
                "120分钟",
                "2022-01-05T11:30:54~14:00:28",
                1641353454000,
                1641362428000
            ),
            ScheduleModel(
                "120分钟",
                "2022-01-05T14:14:28~16:08:48",
                1641363268000,
                1641370128000
            ),
            ScheduleModel(
                "70分钟",
                "2022-01-05T16:48:48~17:00:43",
                1641372528000,
                1641373243000
            ),
        )
    }

    private fun testScale() {
        inflate.timeRuler.mode = ScaleTimeRulerView.Mode.MODE_UINT_1_HOUR
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