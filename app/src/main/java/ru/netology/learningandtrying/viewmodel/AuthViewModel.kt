package ru.netology.learningandtrying.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import ru.netology.learningandtrying.auth.AppAuth
import ru.netology.learningandtrying.model.Token

class AuthViewModel : ViewModel() {
    val state: LiveData<Boolean> = AppAuth.getInstance()
        .state
        .map { it != null }
        .asLiveData(Dispatchers.Default)

}