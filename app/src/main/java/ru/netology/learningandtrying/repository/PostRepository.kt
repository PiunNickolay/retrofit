package ru.netology.learningandtrying.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.learningandtrying.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllAsync()
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun insertNewPosts(posts: List<Post>)
}