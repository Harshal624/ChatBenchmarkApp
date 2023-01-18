package com.app.chatbenchmarkapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(list: List<Chat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleChat(chat: Chat): Long

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid ORDER BY timeCreated ASC")
    fun getAllChatsLiveData(sourceIuid: String): LiveData<List<Chat>>

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid ORDER BY timeCreated ASC")
    suspend fun getAllChatsList(sourceIuid: String): List<Chat>

    @Query("SELECT COUNT(*) FROM chats")
    fun getTotalChatCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM chats where sourceIuid = :sourceIuid")
    fun getTotalChatCountOfSource(sourceIuid: String): LiveData<Int>

    @Query("DELETE FROM chats WHERE sourceIuid = :sourceIuid")
    fun clearChatsOfSource(sourceIuid: String)

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid ORDER BY timeCreated DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedChatList(sourceIuid: String, limit: Int, offset: Int): List<Chat>

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid AND timeCreated <= :timeCreated ORDER BY timeCreated DESC")
    suspend fun getChatListByTc(sourceIuid: String, timeCreated: Long): List<Chat>

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid AND timeCreated > :timeCreated ORDER BY timeCreated ASC")
    suspend fun getChatListByOpp(sourceIuid: String, timeCreated: Long): List<Chat>

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid ORDER BY timeCreated DESC LIMIT :limit")
    suspend fun getChatListInitial(sourceIuid: String, limit: Int): List<Chat>
}