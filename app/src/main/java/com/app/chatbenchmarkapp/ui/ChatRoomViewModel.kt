package com.app.chatbenchmarkapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.chatbenchmarkapp.MainViewModel
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao
import com.app.chatbenchmarkapp.utils.IUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatRoomViewModel(
    private val dao: ChatDao,
    private val sourceIuid: String,
    private val chatRoomType: ChatRoomType
) : ViewModel() {

    fun sendLocalChat(text: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val timeStarted = System.currentTimeMillis()

            val row = dao.insertSingleChat(
                Chat(
                    isSelf = true,
                    text = text,
                    iuid = IUtils.getRandomIuidsForDebugging(),
                    timeCreated = System.currentTimeMillis(),
                    sourceIuid = sourceIuid
                )
            )

            // Chat of iuid already exists
            if (row == -1L) {
                _uiState.update { state ->
                    state.copy(message = "Failed to insert a chat")
                }
            } else {
                // Success
                _uiState.update { state ->
                    state.copy(onNewChatAdded = "Time took to insert a new chat: ${System.currentTimeMillis() - timeStarted} ms")
                }

                if (chatRoomType == ChatRoomType.SIMPLE_LIST) {
                    getAllChatList()
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asLiveData()

    val chatLivedata =
        if (chatRoomType == ChatRoomType.LIVE_DATA) dao.getAllChatsLiveData(sourceIuid = sourceIuid) else null

    init {
        if (chatRoomType == ChatRoomType.SIMPLE_LIST) {

            // Loading state
            _uiState.update { state ->
                state.copy(
                    isLoading = true
                )
            }

            getAllChatList()

        }
    }

    private fun getAllChatList() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    chatList = dao.getAllChatsList(sourceIuid = sourceIuid)
                )
            }
        }
    }


    fun onNewAddedActionPerformed() {
        _uiState.update { state ->
            state.copy(onNewChatAdded = null)
        }
    }

    fun onMessageShown() {
        _uiState.update { state ->
            state.copy(message = null)
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val chatList: List<Chat> = emptyList(),
        val onNewChatAdded: String? = null,
        val message: String? = null
    )
}

class ChatRoomViewModelFactory(
    private val dao: ChatDao,
    private val sourceIuid: String,
    private val chatRoomType: ChatRoomType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatRoomViewModel(
            dao = dao,
            sourceIuid = sourceIuid,
            chatRoomType = chatRoomType
        ) as T
    }
}