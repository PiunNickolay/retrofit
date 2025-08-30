package ru.netology.learningandtrying.repository


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

class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {

    override fun get(): List<Post> {
        val response = ApiService.service.getAll().execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Response code: ${response.code()} ${response.message()}")
        }
        return response.body() ?: throw RuntimeException("Body is null")
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        ApiService.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {

                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Response code: ${response.code()} ${response.message()}"))
                        return
                    }

                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }
                    callback.onSuccess(body)
                }

                override fun onFailure(call: Call<List<Post>>, throwable: Throwable) {
                    callback.onError(throwable)
                }

            })
    }

    override fun likeById(id: Long) {
        TODO()
    }

    override fun shareById(id: Long) {
        TODO()
    }

    override fun removeById(id: Long) {
        val response = ApiService.service.delete(id).execute()
        if (!response.isSuccessful) {
            throw RuntimeException ("Response code: ${response.code()} ${response.message()}")
        }
    }

    override fun save(post: Post) {
        val response = ApiService.service.save(post).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Response code: ${response.code()} ${response.message()}")
        }
    }
}
