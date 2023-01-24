package com.app.chatbenchmarkapp.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ItemChatLocalUserBinding
import com.app.chatbenchmarkapp.databinding.ItemChatRemoteUserBinding
import com.app.chatbenchmarkapp.databinding.ItemPlaceholderBinding
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.utils.IUtils

class EmptyPlaceHolder(
    private val binding: ItemPlaceholderBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind() {

    }
}