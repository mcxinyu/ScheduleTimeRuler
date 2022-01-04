package com.mcxinyu.scheduletimeruler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mcxinyu.scheduletimeruler.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)

        val calendar = Calendar.getInstance().apply {
//            add(Calendar.HOUR_OF_DAY, -1)
        }
//        inflate.timeRuler.cursorTimeValue = calendar.timeInMillis
        val start = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, 6)
        calendar.add(Calendar.MINUTE, 15)
        val end = calendar.timeInMillis
        inflate.timeRuler.setRange(start, end)

        inflate.timeRuler.mode = ScaleTimeRulerView.Mode.MODE_UINT_1_HOUR
    }
}