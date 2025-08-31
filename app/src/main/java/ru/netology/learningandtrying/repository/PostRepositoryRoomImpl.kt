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

    override fun getAllAsync(callback: PostRepository.GetAllCallback<List<Post>>) {
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

    override fun likeById(id: Long, likedByMe: Boolean, callback: PostRepository.GetAllCallback<Post> ) {
        val call = if (likedByMe) {
            ApiService.service.dislikeById(id)
        } else {
            ApiService.service.likeById(id)
        }
        call.enqueue(object: Callback<Post>{
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful){
                    when(response.code()){
                        404->callback.onError(RuntimeException("Post not found"))
                        500->callback.onError(RuntimeException("Server error"))
                        else->callback.onError(RuntimeException("Error: ${response.code()}"))
                    }
                    return
                }
                val body = response.body()
                if (body == null){
                    callback.onError(RuntimeException("Body is null"))
                }else{
                    callback.onSuccess(body)
                }
            }

            override fun onFailure(call: Call<Post>, e: Throwable) {
                callback.onError(e)
            }

        })
    }

    override fun shareById(id: Long) {
        TODO()
    }

    override fun removeById(id: Long, callback: PostRepository.GetAllCallback<Unit>) {
        ApiService.service.delete(id)
            .enqueue(object: Callback<Unit>{
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful){
                        when (response.code()){
                            404->callback.onError(RuntimeException("Post not found"))
                            500->callback.onError(RuntimeException("Server error"))
                            else->callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }else {
                        callback.onSuccess(Unit)
                    }
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callback.onError(e)
                }

            })
    }

    override fun save(post: Post, callback: PostRepository.GetAllCallback<Post>) {
        ApiService.service.save(post)
            .enqueue(object: Callback<Post>{
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful){
                        when(response.code()){
                            404->callback.onError(RuntimeException("Post not found"))
                            500->callback.onError(RuntimeException("Server error"))
                            else->callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }
                    val body = response.body()
                    if (body == null){
                        callback.onError(RuntimeException("Body is null"))
                    }else{
                        callback.onSuccess(body)
                    }
                }

                override fun onFailure(call: Call<Post>, e: Throwable) {
                    callback.onError(e)
                }

            })
    }
}
