package com.example.final_project.data.mapper

import com.example.final_project.data.local.entity.MessageEntity
import com.example.final_project.data.remote.dto.MessageDto
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.toMessageStatus
import com.example.final_project.domain.model.toMessageType

fun MessageDto.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        messageType = messageType,
        songId = songId,
        status = status,
        createdAt = createdAt
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        messageType = messageType.toMessageType(),
        songId = songId,
        status = status.toMessageStatus(),
        createdAt = createdAt
    )
}
