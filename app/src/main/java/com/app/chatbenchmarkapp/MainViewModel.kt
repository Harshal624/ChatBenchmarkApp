package com.app.chatbenchmarkapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao
import com.app.chatbenchmarkapp.utils.IUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val dao: ChatDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asLiveData()

    private val _seekbar1Progress = MutableStateFlow(5)
    val seekBar1Progress = _seekbar1Progress.asLiveData()

    private val _seekbar2Progress = MutableStateFlow(1000)
    val seekbar2Progress = _seekbar2Progress.asLiveData()

    val totalChatCount = dao.getTotalChatCount()
    val totalChatCountChatRoom1 = dao.getTotalChatCountOfSource(sourceIuid = Chat.CHAT_ROOM_1)
    val totalChatCountChatRoom2 = dao.getTotalChatCountOfSource(sourceIuid = Chat.CHAT_ROOM_2)

    fun addChats(sourceIuid: String, bulkInsert: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            when (sourceIuid) {
                Chat.CHAT_ROOM_1 -> {
                    showUserMessage("Adding ${_seekbar1Progress.value} chats")
                    addRandomChats(sourceIuid, seekBar1Progress.value ?: 0, bulkInsert = bulkInsert)
                }
                else -> {
                    showUserMessage("Adding ${_seekbar2Progress.value} chats")
                    addRandomChats(sourceIuid, seekbar2Progress.value ?: 0, bulkInsert = bulkInsert)
                }
            }
        }
    }

    /**
     * @param count: How many chats needs to be added
     */
    private suspend fun addRandomChats(sourceIuid: String, count: Int, bulkInsert: Boolean) {
        val chatListToAdd = mutableListOf<Chat>()
        for (i in 1..count) {
            chatListToAdd.add(
                Chat(
                    text = IUtils.getRandomChat(),
                    iuid = IUtils.getRandomIuidsForDebugging(),
                    timeCreated = IUtils.getCurrentTimeInMicro(),
                    isSelf = (1..2).random() == 1,
                    sourceIuid = sourceIuid
                )
            )
        }
        val timeStarted = System.currentTimeMillis()
        if (bulkInsert) {
            dao.insertChats(chatListToAdd)
        } else {
            chatListToAdd.forEach {
                dao.insertSingleChat(it)
            }
        }
        showUserMessage("It took ${System.currentTimeMillis() - timeStarted} ms to insert ${chatListToAdd.size} chats, bulk ON: $bulkInsert")
    }

    fun onProgressBarChanged1(progress: Int) {
        _seekbar1Progress.value = progress * 10
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

    fun clearChats(sourceIuid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearChatsOfSource(sourceIuid = sourceIuid)

            if (sourceIuid == Chat.CHAT_ROOM_1) {
                showUserMessage("All chats cleared of chat tab 1")
            } else {
                showUserMessage("All chats cleared of chat tab 2")
            }
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