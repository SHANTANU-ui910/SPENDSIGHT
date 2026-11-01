package com.spendsight.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.spendsight.app.data.local.entity.MerchantPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantPatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pattern: MerchantPatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultPatterns(patterns: List<MerchantPatternEntity>)

    @Query("SELECT * FROM merchant_patterns WHERE merchantPattern = :normalizedMerchant LIMIT 1")
    suspend fun findExactPattern(normalizedMerchant: String): MerchantPatternEntity?

    @Query("""
        SELECT * FROM merchant_patterns 
        WHERE :merchantText LIKE '%' || merchantPattern || '%'
        ORDER BY hitCount DESC, lastMatchedTimestamp DESC 
        LIMIT 1
    """)
    suspend fun findBestFuzzyMatch(merchantText: String): MerchantPatternEntity?

    @Query("SELECT * FROM merchant_patterns ORDER BY hitCount DESC")
    fun getAllPatterns(): Flow<List<MerchantPatternEntity>>

    @Transaction
    suspend fun recordMerchantSelection(merchant: String, categoryId: Long) {
        val normalized = merchant.trim().uppercase()
        val existing = findExactPattern(normalized)
        if (existing != null) {
            insertOrUpdate(
                existing.copy(
                    categoryId = categoryId,
                    hitCount = existing.hitCount + 1,
                    lastMatchedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            insertOrUpdate(
                MerchantPatternEntity(
                    merchantPattern = normalized,
                    categoryId = categoryId,
                    hitCount = 1,
                    lastMatchedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }
}

