package com.app.chatbenchmarkapp.utils

import android.app.Activity
import android.widget.Toast

fun Activity.showToast(msg: String?) {
    msg?.let {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}