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
            dao.insertSingleChat(
                Chat(
                    isSelf = false,
                    text = text,
                    iuid = IUtils.getRandomIuidsForDebugging(1).first(),
                    timeCreated = System.currentTimeMillis(),
                    sourceIuid = sourceIuid
                )
            )
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

            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        chatList = dao.getAllChatsList(sourceIuid = sourceIuid)
                    )
                }
            }
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val chatList: List<Chat> = emptyList()
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