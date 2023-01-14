package com.app.chatbenchmarkapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {

    @Insert
    suspend fun insertChats(list: List<Chat>)

    @Query("SELECT * FROM chats WHERE sourceIuid = :sourceIuid")
    fun getAllChatsForSourceIuid(sourceIuid: String): LiveData<List<Chat>>

    @Query("SELECT COUNT(*) FROM chats")
    fun getTotalChatCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM chats where sourceIuid = :sourceIuid")
    fun getTotalChatCountOfSource(sourceIuid: String): LiveData<Int>
}