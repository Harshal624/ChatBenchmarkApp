package com.app.chatbenchmarkapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val dao: ChatDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asLiveData()

    private val _seekbar1Progress = MutableStateFlow(1000)
    val seekBar1Progress = _seekbar1Progress.asLiveData()

    private val _seekbar2Progress = MutableStateFlow(1000)
    val seekbar2Progress = _seekbar2Progress.asLiveData()

    val totalChatCount = dao.getTotalChatCount()
    val totalChatCountChatRoom1 = dao.getTotalChatCountOfSource(sourceIuid = Chat.CHAT_ROOM_1)
    val totalChatCountChatRoom2 = dao.getTotalChatCountOfSource(sourceIuid = Chat.CHAT_ROOM_1)

    fun addChats(sourceIuid: String) {
        when (sourceIuid) {
            Chat.CHAT_ROOM_1 -> {
                showUserMessage("Adding ${_seekbar1Progress.value} chats")
            }
            else -> {
                showUserMessage("Adding ${_seekbar2Progress.value} chats")
            }
        }
    }

    fun onProgressBarChanged1(progress: Int) {
        _seekbar1Progress.value = progress * 1000
    }

    fun onProgressBarChanged2(progress: Int) {
        _seekbar2Progress.value = progress * 1000
    }

    fun onMessageShown() {
        _uiState.update { state ->
            state.copy(userMessage = null)
        }
    }

    private fun showUserMessage(msg: String?) {
        _uiState.update { state ->
            state.copy(userMessage = msg)
        }
    }

    data class UiState(
        val userMessage: String? = null
    )
}

class MainViewModelFactory(
    private val dao: ChatDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            dao = dao
        ) as T
    }
}