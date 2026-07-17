package com.example.final_project.domain.model

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String?,
    val messageType: MessageType,
    val songId: String?,
    val status: MessageStatus,
    val createdAt: String
)

enum class MessageType {
    TEXT,
    SONG
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

fun String.toMessageType(): MessageType {
    return when (this.lowercase()) {
        "song" -> MessageType.SONG
        else -> MessageType.TEXT
    }
}

fun String.toMessageStatus(): MessageStatus {
    return when (this.lowercase()) {
        "sending" -> MessageStatus.SENDING
        "sent" -> MessageStatus.SENT
        "delivered" -> MessageStatus.DELIVERED
        "read" -> MessageStatus.READ
        else -> MessageStatus.SENT
    }
}
