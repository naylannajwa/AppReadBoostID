package com.readboost.id.data.local.dao

import androidx.room.*
import com.readboost.id.data.model.Leaderboard
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC, totalXP DESC")
    fun getAllLeaderboard(): Flow<List<Leaderboard>>

    @Query("SELECT * FROM leaderboard ORDER BY rank ASC, totalXP DESC LIMIT :limit")
    fun getTopLeaderboard(limit: Int = 10): Flow<List<Leaderboard>>

    @Query("SELECT * FROM leaderboard WHERE userId = :userId")
    suspend fun getLeaderboardByUserId(userId: Int): Leaderboard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(leaderboard: Leaderboard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLeaderboard(leaderboards: List<Leaderboard>)

    @Update
    suspend fun updateLeaderboard(leaderboard: Leaderboard)

    @Delete
    suspend fun deleteLeaderboard(leaderboard: Leaderboard)

    @Query("DELETE FROM leaderboard")
    suspend fun deleteAllLeaderboard()

    @Query("UPDATE leaderboard SET rank = :rank WHERE userId = :userId")
    suspend fun updateRank(userId: Int, rank: Int)

    @Query("UPDATE leaderboard SET totalXP = :xp WHERE userId = :userId")
    suspend fun updateXP(userId: Int, xp: Int)
}

