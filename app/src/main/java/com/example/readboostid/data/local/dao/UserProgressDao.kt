package com.readboost.id.data.local.dao

import androidx.room.*
import com.readboost.id.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressSync(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(userProgress: UserProgress)

    @Update
    suspend fun updateUserProgress(userProgress: UserProgress)

    @Query("UPDATE user_progress SET totalXP = totalXP + :xp WHERE id = 1")
    suspend fun addXP(xp: Int)

    @Query("UPDATE user_progress SET streakDays = :streak, lastReadDate = :date WHERE id = 1")
    suspend fun updateStreak(streak: Int, date: Long)

    @Query("UPDATE user_progress SET totalReadingTime = totalReadingTime + :seconds WHERE id = 1")
    suspend fun addReadingTime(seconds: Int)

    @Query("UPDATE user_progress SET dailyTarget = :target WHERE id = 1")
    suspend fun updateDailyTarget(target: Int)
}

