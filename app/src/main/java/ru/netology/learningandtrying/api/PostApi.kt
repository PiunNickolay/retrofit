package ru.netology.learningandtrying.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.learningandtrying.BuildConfig
import ru.netology.learningandtrying.BuildConfig.BASE_URL
import ru.netology.learningandtrying.dto.Post
import java.util.concurrent.TimeUnit


private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }
    )
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface PostApi {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun delete(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id:Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id:Long): Post

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id:Long): List<Post>

}

object ApiService {
    val service by lazy {
        retrofit.create<PostApi>()
    }
}