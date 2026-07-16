package com.example.final_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String?,
    val messageType: String, // 'text' or 'song'
    val songId: String?,
    val status: String, // 'sending', 'sent', 'read'
    val createdAt: String
)
