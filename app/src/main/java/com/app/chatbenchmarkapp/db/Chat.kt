package com.app.chatbenchmarkapp.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [Index(value = ["sourceIuid", "timeCreated"])]
)
data class Chat(
    @PrimaryKey
    val iuid: String,
    val text: String,
    val timeCreated: Long,
    val sourceIuid: String,
    val isSelf: Boolean
) {
    companion object {
        const val CHAT_ROOM_1 = "chat room 1"
        const val CHAT_ROOM_2 = "chat room 2"
    }
}
