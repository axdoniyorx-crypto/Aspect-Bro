package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threat_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ThreatRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ThreatRecordEntity): Long

    @Query("DELETE FROM threat_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM threat_records")
    suspend fun clearAllRecords()

    @Query("SELECT * FROM blacklisted_hashes")
    fun getAllBlacklistedHashes(): Flow<List<BlacklistHashEntity>>

    @Query("SELECT * FROM blacklisted_hashes WHERE LOWER(sha256) = LOWER(:hash) LIMIT 1")
    suspend fun findBlacklistedByHash(hash: String): BlacklistHashEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistHash(entity: BlacklistHashEntity)

    @Query("DELETE FROM blacklisted_hashes WHERE sha256 = :sha256")
    suspend fun deleteBlacklistHash(sha256: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllBlacklist(list: List<BlacklistHashEntity>)
}
