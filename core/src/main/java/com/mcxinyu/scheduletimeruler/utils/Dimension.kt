package com.mcxinyu.scheduletimeruler

import android.content.Context
import android.util.TypedValue

fun Float.toPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

fun Int.toPx(context: Context): Float = this.toFloat().toPx(context)

fun Float.toPxForSp(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)

fun Int.toPxForSp(context: Context): Float = this.toFloat().toPxForSp(context)
