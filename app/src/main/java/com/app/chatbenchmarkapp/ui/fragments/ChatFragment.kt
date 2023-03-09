package com.app.chatbenchmarkapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.FragmentChatBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.ui.ChatRoomActivity
import com.app.chatbenchmarkapp.ui.ChatRoomType
import com.app.chatbenchmarkapp.ui.ChatRoomViewModel
import com.app.chatbenchmarkapp.ui.ChatRoomViewModelFactory
import com.app.chatbenchmarkapp.ui.adapters.ChatPagingAdapter
import com.app.chatbenchmarkapp.utils.IuidGenerator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment(
    private val listener: OnChatRecyclerviewScrollListener
) : Fragment() {

    private val TAG = "ChatFragment"

    private lateinit var chatPagingAdapter: ChatPagingAdapter

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatLayoutManager: LinearLayoutManager
    private lateinit var viewModel: ChatRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater)
        return binding.root
    }

    private var isInitialScrollStateInitialized = false

    private var shouldExpandTheTitleOnScrollComplete = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ChatRoomViewModelFactory(
                AppDatabase.getDatabase(requireContext()).chatDao(),
                sourceIuid = Chat.CHAT_ROOM_1,
                chatRoomType = ChatRoomType.PAGING
            )
        )[ChatRoomViewModel::class.java]

        chatPagingAdapter = ChatPagingAdapter()
        chatLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true)

        binding.recyclerview.apply {
            setHasFixedSize(false)
            chatLayoutManager.stackFromEnd = false
            chatLayoutManager.reverseLayout = true
            layoutManager = chatLayoutManager
            adapter = chatPagingAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Log.d(TAG, "onScrollStateChanged: State IDLE")
                        if (shouldExpandTheTitleOnScrollComplete) {
                            val isLastChatVisible = IuidGenerator.isLastElementVisible(chatLayoutManager, chatPagingAdapter.itemCount)
                            if (isLastChatVisible) {
                                recyclerView.post {
                                    listener.expandAppbar()
                                }
                            }
                            shouldExpandTheTitleOnScrollComplete = false
                        }
                    }
                }
                
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isInitialScrollStateInitialized) {
                        Log.d(TAG, "onScrolled: called")
                        // If there are fewer chats no need to collapse
                        val hasFewerChats = chatPagingAdapter.itemCount < 7
                        val lastCompletely = chatLayoutManager.findLastVisibleItemPosition()
                        if (lastCompletely == chatPagingAdapter.itemCount || hasFewerChats) {
                            listener.expandAppbar()
                        } else {
                            listener.collapseAppbar()
                        }
                        isInitialScrollStateInitialized = true
                    }
                }
            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatPagedData?.collectLatest {
                Log.d(TAG, "onViewCreated: submit data called")
                chatPagingAdapter.submitData(it)
            }
        }

        (requireActivity() as ChatRoomActivity).setToolbarClickListener(object: ChatRoomActivity.ToolbarClickListener {
            override fun onClicked() {
                Log.d(TAG, "onClicked: called")
                shouldExpandTheTitleOnScrollComplete = true
                binding.recyclerview.smoothScrollToPosition(chatPagingAdapter.itemCount)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnChatRecyclerviewScrollListener {
        fun collapseAppbar()
        fun expandAppbar()
    }
}