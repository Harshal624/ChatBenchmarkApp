package com.app.chatbenchmarkapp.ui.paging

import android.util.Log
import androidx.paging.PagingState
import com.app.chatbenchmarkapp.db.Chat
import com.app.chatbenchmarkapp.db.ChatDao
import kotlinx.coroutines.delay

class ChatsTimeCreatedPagingSource(
    private val dao: ChatDao,
    private val sourceIuid: String
) : androidx.paging.PagingSource<Long, Chat>() {

    private val TAG = "ChatsPagingSource"

    override val keyReuseSupported: Boolean
        get() = true

    override val jumpingSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Long, Chat>): Long? {
        val chat = state.anchorPosition?.let { anchorPos ->
            state.closestItemToPosition(anchorPos)
        }

        /*val chat = state.anchorPosition?.let { anchorPos ->
            state.closestPageToPosition(anchorPos).data.firstOrNull()
        }*/

        val value = chat?.timeCreated
        Log.d(TAG, "getRefreshKey: Anchor pos: ${state.anchorPosition}, closes chat: ${chat?.text}, closest chat created: $value")
        return value
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
                    dao.getChatListByOpp(sourceIuid, paramInput, limit = params.loadSize).asReversed()
                } else {
                    /**
                     * If type is refresh and input key is not null, then we know the load was called by getRefreshKey
                     * In that case we will perform 2 queries -> 1. Get older chats with current chat 2. Get newer chats
                     * Then we'll append the two lists and in this case the previous key will be timeCreate of 0th chat
                     *
                     * Raw:
                     * // get more chats time asc reverse and append. 0th chat -> timeCreate will be prev key
                    // "refresh" and also key is not null
                     *
                     */
                    if (isRefresh) {
                        val oldestChat = dao.getChatListByTc(sourceIuid, paramInput, limit = params.loadSize)
                        val newestChat = dao.getChatListByOpp(sourceIuid, paramInput, limit = params.loadSize).asReversed()
                        val finaList = newestChat + oldestChat
                        Log.d(TAG, "load: Final list: $finaList")
                        finaList
                    } else {
                        dao.getChatListByTc(sourceIuid, paramInput, limit = params.loadSize)
                    }
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
                if (isRefresh) {
                    chats.firstOrNull()?.timeCreated
                } else {
                    paramInput
                }
            }

            val itemsBefore: Int = if (chats.isEmpty()) {
                0
            } else {
                val time = chats.firstOrNull()?.timeCreated
                if (time == null) {
                    0
                } else {
                    dao.getNewerChatCountBeforeThisTime(sourceIuid = sourceIuid, timeCreated = time)
                }
            }

            val itemsAfter: Int = if (chats.isEmpty()) {
                0
            } else {
                val time = chats.lastOrNull()?.timeCreated
                if (time == null) {
                    0
                } else {
                    dao.getOldestChatCountAfterThisTime(sourceIuid = sourceIuid, timeCreated = time)
                }
            }

            Log.d(TAG, "load: Chat size: ${chats.size}, prev key: $prevKey, next key: $nextKey, afterCount: $itemsAfter, before count: $itemsBefore")
            return LoadResult.Page(
                data = chats,
                prevKey = prevKey,
                nextKey = nextKey,
                itemsAfter = itemsAfter,
                itemsBefore = itemsBefore
            )
        } catch (e: Exception) {
            Log.d(TAG, "load: exception: $e")
            return LoadResult.Error(e)
        }
    }
}