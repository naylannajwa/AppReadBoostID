// Test file to verify ReadingSession class works
package com.readboost.id

import com.readboost.id.data.model.ReadingSession

fun testReadingSession() {
    val session = ReadingSession(articleId = 1)
    session.elapsedTime = 100
    session.isPaused = true
    session.isCompleted = false

    println("Session created successfully!")
    println("Article ID: ${session.articleId}")
    println("Elapsed Time: ${session.elapsedTime}")
    println("Is Paused: ${session.isPaused}")
    println("Is Completed: ${session.isCompleted}")
}
