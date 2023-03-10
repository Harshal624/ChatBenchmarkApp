package com.app.chatbenchmarkapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.R
import com.app.chatbenchmarkapp.databinding.ActivityChatRoomBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.ui.adapters.ChatListAdapter
import com.app.chatbenchmarkapp.ui.adapters.ChatPagingAdapter
import com.app.chatbenchmarkapp.ui.fragments.ChatFragment
import com.app.chatbenchmarkapp.utils.showToast
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ChatRoomType {
    LIVE_DATA, SIMPLE_LIST, PAGING
}

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val KEY_SOURCE_IUID = "key_source_iuid"
        const val KEY_CHAT_ROOM_TYPE = "key_chat_room_type"

        var SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE = false
    }

    private var toolbarClickListener: ToolbarClickListener? = null

    fun setToolbarClickListener(listener: ToolbarClickListener) {
        this.toolbarClickListener = listener
    }

    private var _binding: ActivityChatRoomBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatRoomViewModel

    private lateinit var chatListAdapterForList: ChatListAdapter

    private lateinit var chatListAdapterForLiveData: ChatListAdapter

    private lateinit var chatPagingAdapter: ChatPagingAdapter

    private var isPagingProgressBarShown = false

    private var isInitialLoadTasksFinished = false

    private lateinit var chatLayoutManager: LinearLayoutManager

    var isShow = true
    var scrollRange = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setOnClickListener {
            Log.d("ChatFragment", "onClicked: called")
            //toolbarClickListener?.onClicked()
            binding.appBar.setExpanded(true)
        }

        binding.appBar.addOnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                isShow = true
                binding.collapsingHeaderView.visibility = View.VISIBLE
                binding.tvPostedIn.visibility = View.GONE
            } else if (isShow) {
                isShow = false
                binding.collapsingHeaderView.visibility = View.GONE
                binding.tvPostedIn.visibility = View.VISIBLE
            }
        }

        val chatRoomType = intent.getSerializableExtra(KEY_CHAT_ROOM_TYPE) as ChatRoomType
        val chatRoomId = intent.getStringExtra(KEY_SOURCE_IUID)

        if (chatRoomType == ChatRoomType.LIVE_DATA) {
            //binding.progressbar.visibility = View.VISIBLE
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
                    //binding.progressbar.visibility = View.VISIBLE
                } else {

                   // binding.progressbar.visibility = View.GONE
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
           // binding.progressbar.visibility = View.GONE
            chatListAdapterForLiveData.submitList(list)
        }

        lifecycleScope.launch {
            viewModel.chatPagedData?.collectLatest {
                chatPagingAdapter.submitData(it)
            }
        }

       /* binding.btnSendChat.setOnClickListener {
            val text = binding.et.text.toString().trim()
            if (text.isBlank()) {
                showToast("Refreshing adapter")
                chatPagingAdapter.refresh()
                return@setOnClickListener
            }

            SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE = true
            viewModel.sendLocalChat(text = text)
            binding.et.text = null
        }

        binding.fabJumpToBottom.setOnClickListener {
            // In the main project, refresh is done before scrolling so doing it here to reproduce the scenario
            //chatPagingAdapter.refresh()
            binding.recyclerview.scrollToPosition(0)
        }

        binding.fabItemCount.setOnClickListener {
            when (chatRoomType) {
                ChatRoomType.LIVE_DATA -> showToast("Item count: ${chatListAdapterForLiveData.itemCount}")
                ChatRoomType.SIMPLE_LIST -> showToast("Item count: ${chatListAdapterForList.itemCount}")
                ChatRoomType.PAGING -> showToast("Item count: ${chatPagingAdapter.itemCount}, snapshot size: ${chatPagingAdapter.snapshot().size}")
            }
        }*/
    }

    private fun setUpRecyclerViews(chatRoomType: ChatRoomType) {

        val chatFragment = ChatFragment(object: ChatFragment.OnChatRecyclerviewScrollListener {
            override fun collapseAppbar() {
                binding.appBar.setExpanded(false)
            }

            override fun expandAppbar() {
                binding.appBar.setExpanded(true)
            }
        })
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, chatFragment)
        transaction.commit()

        chatListAdapterForList = ChatListAdapter {

        }
        chatListAdapterForLiveData = ChatListAdapter {

        }

        chatPagingAdapter = ChatPagingAdapter()

      /*  binding.recyclerview.apply {
            chatLayoutManager = LinearLayoutManager(this@ChatRoomActivity, RecyclerView.VERTICAL, false)
            setHasFixedSize(false)
            chatLayoutManager.stackFromEnd = chatRoomType != ChatRoomType.PAGING
            chatLayoutManager.reverseLayout = chatRoomType == ChatRoomType.PAGING
            layoutManager = chatLayoutManager
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
        }*/

      /*  when (chatRoomType) {
            ChatRoomType.LIVE_DATA -> {
                chatListAdapterForLiveData.registerAdapterDataObserver(object :
                    RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        if (SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE) {
                            if (positionStart > 0) {
                                binding.recyclerview.scrollToPosition(positionStart)
                            }
                            SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE = false
                        }
                    }
                })
            }
            ChatRoomType.SIMPLE_LIST -> {
                chatListAdapterForList.registerAdapterDataObserver(object :
                    RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        if (SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE) {
                            if (positionStart > 0) {
                                binding.recyclerview.scrollToPosition(positionStart)
                            }
                            SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE = false
                        }
                    }
                })
            }
            ChatRoomType.PAGING -> {

                chatPagingAdapter.registerAdapterDataObserver(object :
                    RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        if (SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE) {
                            if (positionStart == 0) {
                                binding.recyclerview.scrollToPosition(positionStart)
                            }
                            SHOULD_JUMP_TO_LATEST_CHAT_ON_NEW_MESSAGE = false
                        }
                    }
                })

                chatPagingAdapter.addLoadStateListener { state ->
                    when (state.refresh) {
                        is LoadState.Loading -> {
                            if (!isPagingProgressBarShown) {
                               // binding.progressbar.visibility = View.VISIBLE
                                isPagingProgressBarShown = true 
                            }
                        }
                        is LoadState.NotLoading -> {
                            if (!isInitialLoadTasksFinished) {
                                // Put tasks here to executed on UI thread when diffutis is done

                                //
                                isInitialLoadTasksFinished = true
                            }
                            //binding.progressbar.visibility = View.GONE
                        }
                        is LoadState.Error -> {
                          //  binding.progressbar.visibility = View.GONE
                        }
                    }
                }
            }
        }*/
    }

    interface ToolbarClickListener {
        fun onClicked()
    }
}