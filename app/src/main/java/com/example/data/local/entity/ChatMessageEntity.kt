package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val playbookTag: String? = null,
    val actionSuggestionJson: String? = null
)
