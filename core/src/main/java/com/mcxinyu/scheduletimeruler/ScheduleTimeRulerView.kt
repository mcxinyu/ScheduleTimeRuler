package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.mcxinyu.scheduletimeruler.ScheduleTimeRulerView

/**
 * @author [yuefeng](mailto:mcxinyu@foxmail.com) in 2021/12/24.
 */
class ScheduleTimeRulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScaleTimeRulerView(context, attrs) {

    init {
        Log.d(TAG, "to-do")
    }

    companion object {
        val TAG = ScheduleTimeRulerView::class.java.simpleName
    }
}