package ru.netology.learningandtrying.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.netology.learningandtrying.R
import ru.netology.learningandtrying.api.ApiService
import ru.netology.learningandtrying.model.FeedModelState
import ru.netology.learningandtrying.model.PhotoModel
import java.io.File


private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = 0L,
    likedByMe = false,
    likes = 0
)
private val noPhoto = PhotoModel()

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryRoomImpl(
        AppDb.getIstance(application).postDao
    )
    val data: LiveData<FeedModel> = repository.data.map { list: List<Post> -> FeedModel(list, list.isEmpty()) }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _newPosts = MutableLiveData<List<Post>>(emptyList())
    val newPosts: LiveData<List<Post>> = _newPosts

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
            .map { posts ->
                _newPosts.postValue(posts)
                posts.size
            }
            .catch { _state.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default)
    }

    fun showNewPosts() {
        viewModelScope.launch {
            newPosts.value?.let {
                repository.insertNewPosts(it)
                _newPosts.value = emptyList()
            }
        }
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

    fun savePhoto(uri: Uri, file: File){
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto(){
        _photo.value = null
    }

    fun load() {
        _state.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                try {
                    repository.save(it, _photo.value?.file)
                    _postsCreated.value = Unit
                } catch (e: Exception) {
                    _errorEvent.value = getApplication<Application>()
                        .getString(R.string.network_error)
                }
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
            } catch (e: Exception) {
                _errorEvent.value = getApplication<Application>()
                    .getString(R.string.network_error)
            }
        }
    }

    fun shareById(id: Long) {
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
        viewModelScope.launch {
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent
}