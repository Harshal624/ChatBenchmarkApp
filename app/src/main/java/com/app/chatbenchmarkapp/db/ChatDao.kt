package com.app.chatbenchmarkapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChats(list: List<Chat>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
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
}