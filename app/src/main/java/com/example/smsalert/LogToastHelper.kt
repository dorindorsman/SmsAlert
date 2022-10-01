package com.example.smsalert

import android.content.Context
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LogToastHelper {

    fun showLogMsg(context: Context?, tag: String, msg: String) {
        val current: LocalDateTime = LocalDateTime.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val formatted: String = current.format(formatter)
        Log.d("$formatted $tag", "$msg")
    }

}