package com.app.chatbenchmarkapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ActivityChatRoomBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.ui.adapters.ChatListAdapter
import com.app.chatbenchmarkapp.utils.IUtils
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

    private lateinit var chatListAdapterForList: ChatListAdapter

    private lateinit var chatListAdapterForLiveData: ChatListAdapter

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

        setUpRecyclerViews(chatRoomType)

        viewModel.uiState.observe(this) { state ->
            if (state.isLoading) {
                binding.progressbar.visibility = View.VISIBLE
            } else {
                binding.progressbar.visibility = View.GONE
            }

            if (chatRoomType == ChatRoomType.SIMPLE_LIST && state.chatList.isNotEmpty()) {
                chatListAdapterForList.submitList(state.chatList)
            }

            state.onNewChatAdded?.let { msg ->
                showToast(msg)
                viewModel.onNewAddedActionPerformed()
            }

            state.message?.let {
                showToast(it)
                viewModel.onMessageShown()
            }
        }

        viewModel.chatLivedata?.observe(this) { list ->
            chatListAdapterForLiveData.submitList(list)
        }

        binding.btnSendChat.setOnClickListener {
            val text = binding.et.text.toString().trim()
            if (text.isBlank()) {
                return@setOnClickListener
            }

            viewModel.sendLocalChat(text = text)
            binding.et.text = null
        }
    }

    private fun setUpRecyclerViews(chatRoomType: ChatRoomType) {
        chatListAdapterForList = ChatListAdapter {

        }
        chatListAdapterForLiveData = ChatListAdapter {

        }

        binding.recyclerview.apply {
            val manager = LinearLayoutManager(this@ChatRoomActivity, RecyclerView.VERTICAL, false)
            setHasFixedSize(false)
            manager.stackFromEnd = true
            layoutManager = manager
            when (chatRoomType) {
                ChatRoomType.LIVE_DATA -> {
                    adapter = chatListAdapterForLiveData
                }
                ChatRoomType.SIMPLE_LIST -> {
                    adapter = chatListAdapterForList
                }
                ChatRoomType.PAGING -> {
                    // TODO Implement this later
                }
            }
        }

        chatListAdapterForLiveData.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart > 0) {
                    binding.recyclerview.scrollToPosition(positionStart)
                }
            }
        })

        chatListAdapterForList.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart > 0) {
                    binding.recyclerview.scrollToPosition(positionStart)
                }
            }
        })
    }
}