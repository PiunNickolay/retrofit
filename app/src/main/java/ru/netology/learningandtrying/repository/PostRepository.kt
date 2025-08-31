package ru.netology.learningandtrying.repository

import androidx.lifecycle.LiveData
import ru.netology.learningandtrying.dto.Post

interface PostRepository {
    fun getAllAsync(callback: GetAllCallback<List<Post>>)
    fun get(): List<Post>
    fun likeById(id: Long, likedByMe: Boolean, callback: GetAllCallback<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long, callback: GetAllCallback<Unit>)
    fun save(post: Post, callback: GetAllCallback<Post>)


    interface GetAllCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Throwable)
    }

}