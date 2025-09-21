package ru.netology.learningandtrying.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.learningandtrying.api.ApiService
import ru.netology.learningandtrying.dto.PushToken
import ru.netology.learningandtrying.model.Token

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<Token?>(null)
    val state = _state.asStateFlow()

    init {
        val id = prefs.getLong(ID_KEY, 0L)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            prefs.edit() { clear() }
        } else {
            _state.value = Token(id = id, token = token)
        }

        sendPushToken()
    }

    @Synchronized
    fun saveAuth(id: Long, token: String) {
        prefs.edit() {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = Token(id = id, token = token)
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit() { clear() }
        _state.value = null
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ApiService.service.sendPushToken(
                    PushToken(
                        token = token ?: FirebaseMessaging.getInstance().token.await()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"
        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
            "required initAuth() first"
        }

        fun initAuth(context: Context) {
            val appAuth = AppAuth(context.applicationContext)
            INSTANCE = appAuth
        }
    }
}