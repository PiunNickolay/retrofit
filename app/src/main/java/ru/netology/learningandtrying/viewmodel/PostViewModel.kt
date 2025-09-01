package ru.netology.learningandtrying.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.learningandtrying.db.AppDb
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.model.FeedModel
import ru.netology.learningandtrying.repository.PostRepository
import ru.netology.learningandtrying.repository.PostRepositoryRoomImpl
import ru.netology.learningandtrying.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = 0L,
    likedByMe = false,
    likes = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryRoomImpl(
        AppDb.getIstance(application).postDao
    )
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data

    private val _postsCreated = SingleLiveEvent<Unit>()
    val postsCreated: LiveData<Unit>
        get() = _postsCreated

    init {
        load()
    }

    fun load() {
        _data.postValue(FeedModel(loading = true))
        object : PostRepository.GetAllCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = (FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Throwable) {
                _data.value = (FeedModel(error = e))
            }
        }
    }

    val edited = MutableLiveData(empty)
    val draft = MutableLiveData<String?>()
    fun likeById(id: Long) {
        val currentState = _data.value ?: return
        val posts = currentState.posts
        val post = posts.find { it.id == id } ?: return
        val likedByMe = post.likedByMe
        repository.likeById(id, likedByMe, object : PostRepository.GetAllCallback<Post> {
            override fun onSuccess(result: Post) {
                val refreshState = _data.value ?: return
                val updatedPosts = refreshState.posts.map {
                    if (it.id == result.id) result else it
                }
                _data.postValue(refreshState.copy(posts = updatedPosts))
            }


            override fun onError(e: Throwable) {
                _data.value
            }
        })
    }

    fun shareById(id: Long) = repository.shareById(id)

    fun removeById(id: Long) {
        val currentState = _data.value ?: return
        _data.postValue(currentState.copy(posts = currentState.posts.filter { it.id != id }))

        repository.removeById(id, object : PostRepository.GetAllCallback<Unit> {
            override fun onSuccess(result: Unit) {

            }

            override fun onError(error: Throwable) {
                _data.postValue(currentState)
            }

        })
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.GetAllCallback<Post> {
                override fun onSuccess(result: Post) {

                    _postsCreated.postValue(Unit)
                }

                override fun onError(e: Throwable) {
                    _data.value

                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }
}