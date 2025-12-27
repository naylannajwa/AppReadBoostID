// File: data/model/Notes.kt
package com.readboost.id.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Article::class,
            parentColumns = ["id"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("articleId")]
)
data class Notes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val articleId: Int,
    val content: String,
    val date: Long = System.currentTimeMillis()
)