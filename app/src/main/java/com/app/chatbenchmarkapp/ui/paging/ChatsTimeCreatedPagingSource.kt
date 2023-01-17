package com.app.chatbenchmarkapp.ui.paging

import android.util.Log
import androidx.paging.PagingState
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao

class ChatsTimeCreatedPagingSource(
    private val dao: ChatDao,
    private val sourceIuid: String
) : androidx.paging.PagingSource<Long, Chat>() {

    private val TAG = "ChatsPagingSource"

    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Long, Chat>): Long? {
        val value = state.anchorPosition?.let { anchorPos ->
            state.closestItemToPosition(anchorPos)?.timeCreated
        }

        if (value != null) {
            Log.d(TAG, "getRefreshKey: Time created: $value, final time created: $value")
            return value
        }

        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Chat> {

        try {
            val isPrepend = params is LoadParams.Prepend
            val isAppend = params is LoadParams.Append
            val isRefresh = params is LoadParams.Refresh

            Log.d(
                TAG,
                "load: called, is prepend: $isPrepend, is append: $isAppend, is refresh: $isRefresh"
            )
            val paramInput = params.key

            val chats = if (paramInput != null) {
                Log.d(TAG, "load: subsequent load, key: $paramInput")

                if (isPrepend) {
                    dao.getChatListByOpp(sourceIuid, paramInput).asReversed()
                } else {
                    dao.getChatListByTc(sourceIuid, paramInput)
                }
            } else {

                // Initial load 
                Log.d(TAG, "load: initial load")
                dao.getChatListInitial(sourceIuid, limit = params.loadSize)
            }

            val nextKey = if (isPrepend) {
                paramInput
            } else {
                val isLastPage =
                    chats.size < params.loadSize || (chats.lastOrNull()?.timeCreated == params.key)
                if (isLastPage) {
                    null
                } else {
                    chats.lastOrNull()?.timeCreated?.let {
                        it - 1L
                    }
                }
            }

            val prevKey = if (isPrepend) {
                val isLastPage = chats.size < params.loadSize
                if (isLastPage) {
                    null
                } else {
                    chats.firstOrNull()?.timeCreated
                }
            } else {
                paramInput
            }

            Log.d(TAG, "load: Chat size: ${chats.size}, prev key: $prevKey, next key: $nextKey")
            return LoadResult.Page(
                data = chats,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Log.d(TAG, "load: exception: $e")
            return LoadResult.Error(e)
        }
    }
}