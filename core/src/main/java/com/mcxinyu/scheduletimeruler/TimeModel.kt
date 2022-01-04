package com.mcxinyu.scheduletimeruler

import java.util.*

data class TimeModel(
    var startTimeValue: Long = Calendar.getInstance()
        .apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis,
    var endTimeValue: Long = Calendar.getInstance()
        .apply {
            set(Calendar.HOUR_OF_DAY, 24)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis,
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
)