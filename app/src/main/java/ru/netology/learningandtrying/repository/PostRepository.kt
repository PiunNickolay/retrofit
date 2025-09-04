package ru.netology.learningandtrying.repository

import androidx.lifecycle.LiveData
import ru.netology.learningandtrying.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAllAsync()
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
}