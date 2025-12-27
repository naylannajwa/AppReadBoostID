package com.readboost.id.domain.repository

import com.readboost.id.data.local.dao.LeaderboardDao
import com.readboost.id.data.local.dao.NotesDao
import com.readboost.id.data.local.dao.UserProgressDao
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.data.model.Notes
import com.readboost.id.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

class UserDataRepository(
    private val notesDao: NotesDao,
    private val userProgressDao: UserProgressDao,
    private val leaderboardDao: LeaderboardDao
) {
    // Notes operations
    fun getAllNotes(): Flow<List<Notes>> = notesDao.getAllNotes()

    fun getNotesByArticle(articleId: Int): Flow<List<Notes>> = notesDao.getNotesByArticle(articleId)

    suspend fun getNoteById(noteId: Int): Notes? = notesDao.getNoteById(noteId)

    suspend fun insertNote(note: Notes): Long = notesDao.insertNote(note)

    suspend fun updateNote(note: Notes) = notesDao.updateNote(note)

    suspend fun deleteNote(note: Notes) = notesDao.deleteNote(note)

    // UserProgress operations
    fun getUserProgress(): Flow<UserProgress?> = userProgressDao.getUserProgress()

    suspend fun getUserProgressOnce(): UserProgress? = userProgressDao.getUserProgressSync()

    suspend fun insertUserProgress(userProgress: UserProgress) = 
        userProgressDao.insertUserProgress(userProgress)

    suspend fun updateUserProgress(userProgress: UserProgress) = 
        userProgressDao.updateUserProgress(userProgress)

    suspend fun addXP(xp: Int) = userProgressDao.addXP(xp)

    suspend fun updateStreak(streak: Int, date: Long) = 
        userProgressDao.updateStreak(streak, date)

    suspend fun addReadingTime(seconds: Int) = userProgressDao.addReadingTime(seconds)

    suspend fun updateDailyTarget(target: Int) = userProgressDao.updateDailyTarget(target)

    // Leaderboard operations
    fun getAllLeaderboard(): Flow<List<Leaderboard>> = leaderboardDao.getAllLeaderboard()

    fun getTopLeaderboard(limit: Int = 10): Flow<List<Leaderboard>> = 
        leaderboardDao.getTopLeaderboard(limit)

    suspend fun getLeaderboardByUserId(userId: Int): Leaderboard? = 
        leaderboardDao.getLeaderboardByUserId(userId)

    suspend fun insertLeaderboard(leaderboard: Leaderboard) = 
        leaderboardDao.insertLeaderboard(leaderboard)

    suspend fun insertAllLeaderboard(leaderboards: List<Leaderboard>) = 
        leaderboardDao.insertAllLeaderboard(leaderboards)

    suspend fun updateLeaderboard(leaderboard: Leaderboard) = 
        leaderboardDao.updateLeaderboard(leaderboard)

    suspend fun deleteLeaderboard(leaderboard: Leaderboard) = 
        leaderboardDao.deleteLeaderboard(leaderboard)

    // Combined operations for reading session completion
    suspend fun completeReadingSession(xp: Int) {
        // Add XP
        addXP(xp)
        
        // Update streak
        val progress = getUserProgressOnce()
        if (progress != null) {
            val today = System.currentTimeMillis()
            val lastReadDate = progress.lastReadDate
            val daysSinceLastRead = (today - lastReadDate) / (1000 * 60 * 60 * 24)
            
            val newStreak = if (daysSinceLastRead <= 1) {
                progress.streakDays + 1
            } else {
                1 // Reset streak if more than 1 day passed
            }
            
            updateStreak(newStreak, today)
        }
    }
}

