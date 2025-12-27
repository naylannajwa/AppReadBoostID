// File: data/model/Article.kt
package com.readboost.id.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "articles")
@Parcelize
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val duration: Int, // dalam menit
    val category: String, // Sains, Teknologi, Psikologi, dll
    val difficulty: String = "Dasar", // Dasar, Menengah, Lanjut
    val xp: Int = 10,
    val imageUrl: String? = null, // URL gambar artikel
    val createdAt: Long = System.currentTimeMillis() // Timestamp pembuatan
) : Parcelable