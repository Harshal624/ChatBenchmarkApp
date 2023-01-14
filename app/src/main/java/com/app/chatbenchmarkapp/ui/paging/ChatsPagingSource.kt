package com.app.chatbenchmarkapp.ui.paging

import androidx.paging.PagingState
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao

class ChatsPagingSource(
    private val dao: ChatDao,
    private val sourceIuid: String
) : androidx.paging.PagingSource<Int, Chat>() {

    private val TAG = "ChatsPagingSource"

    override fun getRefreshKey(state: PagingState<Int, Chat>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Chat> {

        val page = params.key ?: 0
        val offset = page * params.loadSize

        val chats = dao.getPagedChatList(
            sourceIuid = sourceIuid,
            limit = params.loadSize,
            offset = offset
        )

        return LoadResult.Page(
            data = chats,
            prevKey = if (page == 0) null else page - 1,
            nextKey = if (chats.isEmpty()) null else page + 1
        )
    }
}