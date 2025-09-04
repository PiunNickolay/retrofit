package ru.netology.learningandtrying.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import ru.netology.learningandtrying.db.AppDb
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.model.FeedModel
import ru.netology.learningandtrying.repository.PostRepository
import ru.netology.learningandtrying.repository.PostRepositoryRoomImpl
import ru.netology.learningandtrying.util.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.netology.learningandtrying.R
import ru.netology.learningandtrying.model.FeedModelState


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
    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(
            posts = it,
            empty = it.isEmpty()
        )
    }
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    private val _postsCreated = SingleLiveEvent<Unit>()
    val postsCreated: LiveData<Unit>
        get() = _postsCreated

    init {
        load()
    }

    fun load() {
        _state.value = FeedModelState(loading = true)
        viewModelScope.launch{
            try{
                repository.getAllAsync()
                _state.value = FeedModelState()
            }catch (_: Exception){
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                repository.save(it)
                _postsCreated.value = Unit
            }
            edited.value = empty
        }
    }

    val edited = MutableLiveData(empty)
    val draft = MutableLiveData<String?>()
    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                val post = data.value?.posts?.find { it.id == id } ?: return@launch
                repository.likeById(post.id, post.likedByMe)
            }catch (e: Exception){
                _errorEvent.value = getApplication<Application>()
                    .getString(R.string.network_error)
            }
        }
    }

    fun shareById(id: Long){
        TODO()
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _errorEvent.value = getApplication<Application>()
                    .getString(R.string.network_error)
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }

    fun refresh() {
        _state.value = FeedModelState(refreshing = true)
        viewModelScope.launch{
            try{
                repository.getAllAsync()
                _state.value = FeedModelState()
            }catch (_: Exception){
                _state.value = FeedModelState(error = true)
            }
        }
    }

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent
}