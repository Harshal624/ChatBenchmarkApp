package com.app.chatbenchmarkapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.chatbenchmarkapp.databinding.ItemChatLocalUserBinding
import com.app.chatbenchmarkapp.databinding.ItemChatRemoteUserBinding
import com.app.chatbenchmarkapp.databinding.ItemNoticeBinding
import com.app.chatbenchmarkapp.db.Chat

/**
 * @link: https://genicsblog.com/gouravkhunger/pagination-in-android-room-database-using-the-paging-3-library
 */
class ChatPagingAdapter : PagingDataAdapter<Chat, RecyclerView.ViewHolder>(
    ChatListAdapter.chatDiffUtil()
) {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatViewHolderRemote -> {
                holder.bind(getItem(position) ?: return)
            }
            is NoticeViewHolder -> {

            }
            else -> {
                (holder as ChatViewHolderLocal).bind(getItem(position) ?: return)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatListAdapter.VIEW_TYPE_REMOTE -> {
                val binding = ItemChatRemoteUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ChatViewHolderRemote(binding)
            }
            ChatListAdapter.VIEW_TYPE_NOTICE -> {
                val binding =
                    ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoticeViewHolder(binding)
            }
            else -> {
                val binding =
                    ItemChatLocalUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatViewHolderLocal(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chat = getItem(position)

        return if (chat?.isNoticeView == true) {
            ChatListAdapter.VIEW_TYPE_NOTICE
        } else if (chat?.isSelf == true) {
            ChatListAdapter.VIEW_TYPE_SELF
        } else {
            ChatListAdapter.VIEW_TYPE_REMOTE
        }
    }
}