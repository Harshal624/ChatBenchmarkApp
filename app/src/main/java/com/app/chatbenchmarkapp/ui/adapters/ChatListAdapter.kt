package com.app.chatbenchmarkapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ItemChatLocalUserBinding
import com.app.chatbenchmarkapp.databinding.ItemChatRemoteUserBinding
import com.app.chatbenchmarkapp.db.Chat

class ChatListAdapter : ListAdapter<Chat, RecyclerView.ViewHolder>(chatDiffUtil()) {

    companion object {

        private const val VIEW_TYPE_REMOTE = 1
        private const val VIEW_TYPE_SELF = 2

        fun chatDiffUtil() = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.iuid == newItem.iuid
            }

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.timeCreated == newItem.timeCreated
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_REMOTE) {
            val binding = ItemChatRemoteUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ChatViewHolderRemote(binding)
        } else {
            val binding =
                ItemChatLocalUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ChatViewHolderLocal(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatViewHolderRemote) {
            holder.bind(getItem(position))
        } else {
            (holder as ChatViewHolderLocal).bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chat = getItem(position)

        return if (chat.isSelf) {
            VIEW_TYPE_SELF
        } else {
            VIEW_TYPE_REMOTE
        }
    }
}