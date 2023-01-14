package com.app.chatbenchmarkapp.utils

import java.util.*
import java.util.concurrent.TimeUnit

object IUtils {
    fun getRandomIuidsForDebugging(): String {
        return IuidGenerator.getIuid("icm")
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

    private val randomWords = listOf<String>(
        "Hello",
        "Hey",
        "Good Morning",
        "Mouse",
        "Jack",
        "Kitchen",
        "Guy",
        "Minute",
        "Asked",
        "Precision",
        "Red Army",
        "Second",
        "Comrades",
        "Ryan",
        "However",
        "Vampire"
    )

    fun getRandomChat(): String {
        val words = mutableListOf<String>()
        val length = (1..12).random()
        for (i in 1..length) {
            words.add(randomWords.random())
        }
        return words.joinToString(separator = " ")
    }
}