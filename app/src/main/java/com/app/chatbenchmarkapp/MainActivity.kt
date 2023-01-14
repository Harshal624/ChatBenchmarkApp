package com.app.chatbenchmarkapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.app.chatbenchmarkapp.databinding.ActivityMainBinding
import com.app.chatbenchmarkapp.db.AppDatabase
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.ui.ChatRoomActivity
import com.app.chatbenchmarkapp.ui.ChatRoomType
import com.app.chatbenchmarkapp.utils.showToast

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(AppDatabase.getDatabase(this).chatDao())
        )[MainViewModel::class.java]
        setUpSeekbars()
        observeUiState()
        viewModel.totalChatCount.observe(this) { count ->
            binding.tvTotalChats.text = "Total chats: $count"
        }

        viewModel.totalChatCountChatRoom1.observe(this) { count ->
            binding.tvNoOfChats.text = "No. of chats: $count"
        }

        viewModel.totalChatCountChatRoom2.observe(this) { count ->
            binding.tvNoOfChats2.text = "No. of chats: $count"
        }

        // Add chats in chat room 1
        binding.btnAddChats.setOnClickListener {
            viewModel.addChats(sourceIuid = Chat.CHAT_ROOM_1, bulkInsert = binding.switch1.isChecked)
        }

        // Add chats in chat room 2
        binding.btnAddChats2.setOnClickListener {
            viewModel.addChats(sourceIuid = Chat.CHAT_ROOM_2, bulkInsert = binding.switch2.isChecked)
        }

        binding.btnChatsLivedata.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_1)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.LIVE_DATA)
            startActivity(intent)
        }

        binding.btnChatsLivedata2.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_2)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.LIVE_DATA)
            startActivity(intent)
        }

        binding.btnChatsSimpleList.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_1)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.SIMPLE_LIST)
            startActivity(intent)
        }

        binding.btnChatsSimpleList2.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_2)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.SIMPLE_LIST)
            startActivity(intent)
        }

        binding.btnChatsPaging.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_1)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.PAGING)
            startActivity(intent)
        }

        binding.btnChatsPaging2.setOnClickListener {
            val intent = getChatRoomActivityIntent()
            intent.putExtra(ChatRoomActivity.KEY_SOURCE_IUID, Chat.CHAT_ROOM_2)
            intent.putExtra(ChatRoomActivity.KEY_CHAT_ROOM_TYPE, ChatRoomType.PAGING)
            startActivity(intent)
        }

        binding.btnClearChats.setOnClickListener {
            viewModel.clearChats(sourceIuid = Chat.CHAT_ROOM_1)
        }

        binding.btnClearChats2.setOnClickListener {
            viewModel.clearChats(sourceIuid = Chat.CHAT_ROOM_2)
        }

        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.text = "Bulk insert: ${if (isChecked) "ON" else "OFF"}"
        }

        binding.switch2.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.text = "Bulk insert: ${if (isChecked) "ON" else "OFF"}"
        }
    }

    private fun getChatRoomActivityIntent(): Intent {
        return Intent(this, ChatRoomActivity::class.java)
    }

    private fun setUpSeekbars() {
        binding.seekbarChat1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.onProgressBarChanged1(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.seekbarChat2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.onProgressBarChanged2(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        viewModel.seekBar1Progress.observe(this) { data ->
            binding.btnAddChats.text = "Add $data chats"
        }

        viewModel.seekbar2Progress.observe(this) { data ->
            binding.btnAddChats2.text = "Add $data chats"
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            state.userMessage?.let {
                showToast(it)
                viewModel.onMessageShown()
            }
        }
    }
}