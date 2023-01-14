package com.app.chatbenchmarkapp.utils

import java.util.*
import java.util.concurrent.TimeUnit

object IUtils {
    fun getRandomIuidsForDebugging(count: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..count) {
            list.add(IuidGenerator.getIuid("icm"))
        }
        return list
    }

    @JvmStatic
    fun getCurrentTimeInMicro(): Long {
        var microTime = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis())
        microTime += IUtils.getRandomNumber(999).toLong()
        return microTime
    }

    fun getRandomNumber(high: Int): Int {
        val r = Random()
        return r.nextInt(high)
    }
}