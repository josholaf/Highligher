package com.josholaf.highligher.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY name ASC")
    suspend fun getAllOnce(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun observeByFolder(folderId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE folderId = :folderId ORDER BY updatedAt DESC")
    suspend fun getByFolder(folderId: String): List<DocumentEntity>

    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: DocumentEntity)

    @Update
    suspend fun update(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights WHERE documentId = :documentId ORDER BY createdAt ASC")
    fun observeByDocument(documentId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HighlightEntity>>

    @Query("""
        SELECT * FROM highlights
        WHERE selectedText LIKE '%' || :query || '%'
           OR note LIKE '%' || :query || '%'
           OR IFNULL(sourceUrl, '') LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """)
    fun search(query: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE id = :id")
    suspend fun getById(id: String): HighlightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM highlights")
    fun observeCount(): Flow<Int>
}
