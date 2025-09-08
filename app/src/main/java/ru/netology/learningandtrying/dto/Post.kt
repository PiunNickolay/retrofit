package ru.netology.learningandtrying.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val authorAvatar: String = "",
) {
    var shareCount: Int = 0
    var viewCount: Int = 0
    var video: String? = null
}