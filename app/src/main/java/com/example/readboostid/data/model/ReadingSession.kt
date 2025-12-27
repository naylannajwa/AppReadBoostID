// File: data/model/ReadingSession.kt
package com.readboost.id.data.model

class ReadingSession(
    val articleId: Int,
    val startTime: Long = System.currentTimeMillis(),
    var elapsedTime: Int = 0, // dalam detik
    var isPaused: Boolean = false,
    var isCompleted: Boolean = false
)