package ru.netology.learningandtrying.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.learningandtrying.api.ApiService
import ru.netology.learningandtrying.dao.PostDao
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.entity.PostEntity
import java.io.IOException
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import ru.netology.learningandtrying.api.PostApi
import ru.netology.learningandtrying.entity.fromDtoToEntity
import ru.netology.learningandtrying.entity.toDto

class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.toDto()
    }

    override suspend fun getAllAsync() {
        val posts: List<Post> = ApiService.service.getAll()
        dao.insert(posts.fromDtoToEntity())
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean): Post {
       dao.likeById(id)
        return try {
            val update = if (likedByMe) {
                ApiService.service.dislikeById(id)
            } else {
                ApiService.service.likeById(id)
            }
            dao.insert(PostEntity.fromDto(update))
            update
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

    override suspend fun save(post: Post): Post {
        TODO("Not yet implemented")
    }


}
