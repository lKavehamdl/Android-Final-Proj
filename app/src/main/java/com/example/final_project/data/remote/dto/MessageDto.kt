package com.example.final_project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("receiver_id") val receiverId: String,
    @SerialName("content") val content: String? = null,
    @SerialName("message_type") val messageType: String = "text",
    @SerialName("song_id") val songId: String? = null,
    @SerialName("status") val status: String = "sent",
    @SerialName("created_at") val createdAt: String
)
