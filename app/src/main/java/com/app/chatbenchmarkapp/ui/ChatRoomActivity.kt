package com.app.chatbenchmarkapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.app.chatbenchmarkapp.MainViewModel
import com.app.chatbenchmarkapp.MainViewModelFactory
import com.app.chatbenchmarkapp.R
import com.app.chatbenchmarkapp.databinding.ActivityChatRoomBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.utils.showToast

enum class ChatRoomType {
    LIVE_DATA, SIMPLE_LIST, PAGING
}

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val KEY_SOURCE_IUID = "key_source_iuid"
        const val KEY_CHAT_ROOM_TYPE = "key_chat_room_type"
    }

    private var _binding: ActivityChatRoomBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatRoomType = intent.getSerializableExtra(KEY_CHAT_ROOM_TYPE) as ChatRoomType
        val chatRoomId = intent.getStringExtra(KEY_SOURCE_IUID)

        viewModel = ViewModelProvider(
            this,
            ChatRoomViewModelFactory(
                AppDatabase.getDatabase(this).chatDao(),
                sourceIuid = chatRoomId!!,
                chatRoomType = chatRoomType
            )
        )[ChatRoomViewModel::class.java]
    }
}