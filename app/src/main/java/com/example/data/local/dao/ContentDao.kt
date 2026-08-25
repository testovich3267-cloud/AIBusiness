package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM contents ORDER BY createdAt DESC")
    fun getAllContents(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM contents WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteContents(): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity): Long

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Query("DELETE FROM contents WHERE id = :id")
    suspend fun deleteContentById(id: Long)
}
