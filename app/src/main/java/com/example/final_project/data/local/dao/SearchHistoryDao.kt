package com.example.final_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.final_project.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun observeHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SearchHistoryEntity): Long

    @androidx.room.Delete
    suspend fun delete(entry: SearchHistoryEntity): Int

    @Query("DELETE FROM search_history")
    suspend fun clearAll(): Int
}
