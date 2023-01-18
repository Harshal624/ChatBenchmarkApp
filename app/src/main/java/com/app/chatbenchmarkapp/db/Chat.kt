package com.app.chatbenchmarkapp.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [Index(value = ["sourceIuid", "timeCreated"])]
)
data class Chat @JvmOverloads constructor(
    @PrimaryKey
    val iuid: String,
    val text: String,
    val timeCreated: Long,
    val sourceIuid: String,
    val isSelf: Boolean,

    @Ignore
    val isNoticeView: Boolean = false
) {
    companion object {
        const val CHAT_ROOM_1 = "chat room 1"
        const val CHAT_ROOM_2 = "chat room 2"

        fun getNoticeChatView() = Chat(
            iuid = "notice",
            text = "This is notice view",
            timeCreated = 0L,
            sourceIuid = "",
            isSelf = true,
            isNoticeView = true
        )
    }
}
