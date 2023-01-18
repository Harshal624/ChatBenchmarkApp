package com.app.chatbenchmarkapp.ui.paging

import android.util.Log
import androidx.paging.PagingState
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao

class ChatsPagingSource(
    private val dao: ChatDao,
    private val sourceIuid: String
) : androidx.paging.PagingSource<Int, Chat>() {

    private val TAG = "ChatsPagingSource"

    override fun getRefreshKey(state: PagingState<Int, Chat>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            val page1 = state.closestPageToPosition(anchorPos)?.prevKey?.plus(1)
            val page2 = state.closestPageToPosition(anchorPos)?.nextKey?.minus(1)
            Log.d(TAG, "getRefreshKey: Anchor pos: $anchorPos, Page 1: $page1, page 2: $page2")
            page1 ?: page2
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Chat> {

        try {
            val page = params.key ?: 0
            val offset = page * params.loadSize

            Log.d(TAG, "load: Page: $page, Offset: $offset")

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
        } catch (e: Exception) {
            Log.d(TAG, "load: exception: $e")
            return LoadResult.Error(e)
        }
        
    }
}