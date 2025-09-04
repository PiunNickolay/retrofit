package ru.netology.learningandtrying.model

import android.content.Context
import okio.IOException
import retrofit2.HttpException
import ru.netology.learningandtrying.R
import ru.netology.learningandtrying.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)

data class FeedModelState(
    val error: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
){
//    val isError: Boolean = error != null
//
//    fun errorToString(context: Context) : String = when(error){
//        is IOException -> context.getString(R.string.network_error)
//        is HttpException -> when (error.code()){
//            404 -> context.getString(R.string.error_not_found)
//            500 -> context.getString(R.string.error_server)
//            else -> context.getString(R.string.unknown_error)
//        }
//        else->context.getString(R.string.unknown_error)
//    }
}