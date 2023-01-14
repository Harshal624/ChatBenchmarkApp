package com.app.chatbenchmarkapp.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ItemChatRemoteUserBinding
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.utils.IUtils

class ChatViewHolderRemote(
    private val binding: ItemChatRemoteUserBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(chat: Chat) {
        binding.tv.text = chat.text
        binding.tvTime.text = IUtils.getDateAndTime(chat.timeCreated)
    }
}