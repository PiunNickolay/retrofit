package ru.netology.learningandtrying.repository


import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.netology.learningandtrying.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.learningandtrying.api.ApiService
import ru.netology.learningandtrying.dao.PostDao
import ru.netology.learningandtrying.dto.Attachment
import ru.netology.learningandtrying.dto.AttachmentType
import ru.netology.learningandtrying.dto.Media
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.entity.PostEntity
import ru.netology.learningandtrying.entity.toDto
import ru.netology.learningandtrying.entity.fromDtoToEntity
import ru.netology.learningandtrying.error.AppError
import ru.netology.learningandtrying.error.NetworkError
import ru.netology.learningandtrying.error.UnknownError
import java.io.File
import java.io.IOException

class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map { it.map { it.toDto() } }

    override suspend fun getAllAsync() {
        try {
            val posts = ApiService.service.getAll()
            dao.insert(posts.fromDtoToEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun removeById(id: Long) {
        val post = dao.getById(id) ?: return
        dao.removeById(id)
        try {
            ApiService.service.delete(id)
        } catch (e: Exception) {
            dao.insert(post)
            throw e
        }
    }

    override suspend fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun save(post: Post, photo: File?) {
        try {
            val media = photo?.let { saveMedia(it) }

            val postWithAttachment = media?.let {
                post.copy(
                    attachment = Attachment(it.id, AttachmentType.IMAGE)
                )
            } ?: post


            val posts = ApiService.service.save(postWithAttachment)
            dao.insert(PostEntity.fromDto(posts))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Media =
        ApiService.service.uploadFile(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(),
            )
        )

    override fun getNewer(id: Long): Flow<List<Post>> = flow {
        while (true) {
            val response = ApiService.service.getNewer(id)
            emit(response)
            delay(10_000)
        }
    }.catch { e -> throw AppError.from(e) }

    override suspend fun insertNewPosts(posts: List<Post>) {
        dao.insert(posts.fromDtoToEntity())
    }
}
