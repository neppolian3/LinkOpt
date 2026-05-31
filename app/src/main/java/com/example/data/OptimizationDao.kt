package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OptimizationDao {
    @Query("SELECT * FROM profile_optimizations ORDER BY timestamp DESC")
    fun getAllOptimizations(): Flow<List<ProfileOptimizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptimization(optimization: ProfileOptimizationEntity): Long

    @Query("DELETE FROM profile_optimizations WHERE id = :id")
    suspend fun deleteOptimizationById(id: Int)

    @Query("SELECT * FROM linkedin_audits ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<LinkedInAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: LinkedInAuditEntity): Long

    @Query("DELETE FROM linkedin_audits WHERE id = :id")
    suspend fun deleteAuditById(id: Int)
}
