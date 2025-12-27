package com.readboost.id.domain.repository

import com.readboost.id.data.local.dao.ArticleDao
import com.readboost.id.data.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArticleRepository(
    private val articleDao: ArticleDao
) {
    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    suspend fun getArticleById(articleId: Int): Article? = articleDao.getArticleById(articleId)

    fun getArticlesByCategory(category: String): Flow<List<Article>> = 
        articleDao.getArticlesByCategory(category)

    fun getArticlesByDifficulty(difficulty: String): Flow<List<Article>> = 
        articleDao.getArticlesByDifficulty(difficulty)

    fun getArticlesByDuration(maxDuration: Int): Flow<List<Article>> {
        // Filter by duration in memory
        // For better performance, you might want to add a query in DAO
        return articleDao.getAllArticles().map { articles ->
            articles.filter { it.duration <= maxDuration }
        }
    }

    suspend fun insertArticle(article: Article) = articleDao.insertArticle(article)

    suspend fun insertAllArticles(articles: List<Article>) = articleDao.insertAllArticles(articles)

    suspend fun updateArticle(article: Article) = articleDao.updateArticle(article)

    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)
}

