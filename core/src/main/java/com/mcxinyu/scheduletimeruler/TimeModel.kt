package com.mcxinyu.scheduletimeruler

import java.util.*

data class TimeModel(
    var startTimeValue: Long = 0,
    var endTimeValue: Long = 1,
    /**
     * 小格子
     * 普通时间点时间步长(毫秒)
     */
    var unitTimeValue: Long = 60 * 1000,
    /**
     * 大格子
     * 关键时间点时间步长(毫秒)
     */
    var keyUnitTimeValue: Long = unitTimeValue * 5,
) {
    init {
        val calendar = Calendar.getInstance()

        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        startTimeValue = calendar.timeInMillis

        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        endTimeValue = calendar.timeInMillis
    }
}