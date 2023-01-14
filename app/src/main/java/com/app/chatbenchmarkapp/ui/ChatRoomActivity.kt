package com.app.chatbenchmarkapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ActivityChatRoomBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.ui.adapters.ChatListAdapter
import com.app.chatbenchmarkapp.ui.adapters.ChatPagingAdapter
import com.app.chatbenchmarkapp.utils.IUtils
import com.app.chatbenchmarkapp.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

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

    private lateinit var chatPagingAdapter: ChatPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatRoomType = intent.getSerializableExtra(KEY_CHAT_ROOM_TYPE) as ChatRoomType
        val chatRoomId = intent.getStringExtra(KEY_SOURCE_IUID)

        if (chatRoomType == ChatRoomType.LIVE_DATA) {
            binding.progressbar.visibility = View.VISIBLE
        }

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

            if (chatRoomType == ChatRoomType.SIMPLE_LIST) {
                if (state.isLoading) {
                    binding.progressbar.visibility = View.VISIBLE
                } else {

                    binding.progressbar.visibility = View.GONE
                }
            }

            if (chatRoomType == ChatRoomType.SIMPLE_LIST && state.chatList.isNotEmpty()) {
                chatListAdapterForList.submitList(state.chatList)
            }

            state.onNewChatAdded?.let { msg ->
                showToast(msg)
                chatPagingAdapter.refresh()
                viewModel.onNewAddedActionPerformed()
            }

            state.message?.let {
                showToast(it)
                viewModel.onMessageShown()
            }
        }

        viewModel.chatLivedata?.observe(this) { list ->
            binding.progressbar.visibility = View.GONE
            chatListAdapterForLiveData.submitList(list)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.chatPagedData?.collectLatest {
                chatPagingAdapter.submitData(it)
            }
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

        chatPagingAdapter = ChatPagingAdapter()

        binding.recyclerview.apply {
            val manager = LinearLayoutManager(this@ChatRoomActivity, RecyclerView.VERTICAL, false)
            setHasFixedSize(false)
            manager.stackFromEnd = chatRoomType != ChatRoomType.PAGING
            manager.reverseLayout = chatRoomType == ChatRoomType.PAGING
            layoutManager = manager
            adapter = when (chatRoomType) {
                ChatRoomType.LIVE_DATA -> {
                    chatListAdapterForLiveData
                }
                ChatRoomType.SIMPLE_LIST -> {
                    chatListAdapterForList
                }
                ChatRoomType.PAGING -> {
                    chatPagingAdapter
                }
            }
        }

        chatPagingAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.recyclerview.scrollToPosition(positionStart)
                }
            }
        })

        chatListAdapterForLiveData.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart > 0) {
                    binding.recyclerview.scrollToPosition(positionStart)
                }
            }
        })

        chatListAdapterForList.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart > 0) {
                    binding.recyclerview.scrollToPosition(positionStart)
                }
            }
        })
    }
}