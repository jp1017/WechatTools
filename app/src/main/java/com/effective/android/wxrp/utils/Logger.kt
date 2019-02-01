package com.effective.android.wxrp.utils

import android.util.Log

object Logger {
    private const val TAG = "logger - "
    fun e(tag: String, msg: String) = Log.e("$TAG $tag", msg)
    fun w(tag: String, msg: String) = Log.w("$TAG $tag", msg)
    fun i(tag: String, msg: String) = Log.i("$TAG $tag", msg)
    fun d(tag: String, msg: String) = Log.d("$TAG $tag", msg)
    fun v(tag: String, msg: String) = Log.v("$TAG $tag", msg)
}
