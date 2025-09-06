package ru.netology.learningandtrying.repository


import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.netology.learningandtrying.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.learningandtrying.api.ApiService
import ru.netology.learningandtrying.dao.PostDao
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.entity.PostEntity
import ru.netology.learningandtrying.entity.toDto
import ru.netology.learningandtrying.entity.fromDtoToEntity
import ru.netology.learningandtrying.error.AppError
import ru.netology.learningandtrying.error.NetworkError
import ru.netology.learningandtrying.error.UnknownError
import java.io.IOException

class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map { it.map { it.toDto() } }

    override suspend fun getAllAsync() {
        try {
            val posts = ApiService.service.getAll()
            dao.insert(posts.fromDtoToEntity())
        }catch (e: IOException){
            throw NetworkError
        }catch (e: Exception){
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean): Post {
       dao.likeById(id)
        return try {
            if (likedByMe) {
                ApiService.service.dislikeById(id)
            } else {
                ApiService.service.likeById(id)
            }
            dao.getById(id)?.toDto() ?: throw RuntimeException("Post not found locally")
        }catch (e: Exception){
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun removeById(id: Long) {
        val post = dao.getById(id) ?: return
        dao.removeById(id)
        try {
            ApiService.service.delete(id)
        }catch (e: Exception){
            dao.insert(post)
            throw e
        }
    }

    override suspend fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun save(post: Post) {
       try {
           val posts = ApiService.service.save(post)
           dao.insert(PostEntity.fromDto(posts))
       }catch (e: IOException){
           throw NetworkError
       }catch (e: Exception){
           throw UnknownError
       }
    }

    override fun getNewer(id: Long): Flow<List<Post>> = flow {
        val response = ApiService.service.getNewer(id)
        emit(response)
    }.catch { e -> throw AppError.from(e) }

    override suspend fun insertNewPosts(posts: List<Post>){
        dao.insert(posts.fromDtoToEntity())
    }
}
