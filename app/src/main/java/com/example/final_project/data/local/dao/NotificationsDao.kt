package com.example.final_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.final_project.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface NotificationsDao {

    @Query("SELECT * FROM notifications WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getNotifications(ownerId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE ownerId = :ownerId AND isRead = 0")
    fun getUnreadCount(ownerId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE ownerId = :ownerId AND isRead = 0")
    suspend fun markAllRead(ownerId: String): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM notifications WHERE ownerId = :ownerId")
    suspend fun deleteAllForOwner(ownerId: String): Int

    @Query("DELETE FROM notifications")
    suspend fun clearAll(): Int
}
