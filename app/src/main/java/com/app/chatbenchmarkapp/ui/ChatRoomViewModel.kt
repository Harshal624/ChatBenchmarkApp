package com.app.chatbenchmarkapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.chatbenchmarkapp.MainViewModel
import com.app.chatbenchmarkapp.db.ChatDao

class ChatRoomViewModel(
    private val dao: ChatDao
) : ViewModel() {
}

class ChatRoomViewModelFactory(
    private val dao: ChatDao,
    private val sourceIuid: String,
    private val chatRoomType: ChatRoomType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatRoomViewModel(
            dao = dao
        ) as T
    }
}