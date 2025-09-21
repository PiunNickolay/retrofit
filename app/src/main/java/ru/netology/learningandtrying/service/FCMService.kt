package ru.netology.learningandtrying.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.learningandtrying.R
import ru.netology.learningandtrying.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val recipientId = message.data["recipientId"]?.toLongOrNull()
        val actionStr = message.data["action"] ?: return
        val content = message.data["content"] ?: return

        val myId = AppAuth.getInstance().state.value?.id

        when {
            recipientId == null || recipientId == myId -> {
                val action = runCatching { Action.valueOf(actionStr ?: "") }.getOrNull()
                when (action) {
                    Action.LIKE -> handleLike(Gson().fromJson(content, Like::class.java))
                    Action.NEW_POST -> handleNewPost(Gson().fromJson(content, NewPost::class.java))
                    null -> showNotification(content)
                }
            }
            recipientId == 0L || recipientId != myId -> {
                AppAuth.getInstance().sendPushToken()
            }
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token = token)
    }

    private fun showNotification(content: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleLike(like: Like) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    getString(
                        R.string.notification_user_liked,
                        like.userName,
                        like.postAuthor
                    )
                )
                .build()

            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }

    private fun handleNewPost(post: NewPost) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(post.text)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_new_post_title, post.authorName))
                .setContentText(post.text.take(40))
                .setStyle(bigTextStyle)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this)
                .notify(post.postId.hashCode(), notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "notifications"
    }
}


enum class Action {
    LIKE,
    NEW_POST
}

data class Like(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String
)


data class NewPost(
    val postId: Int,
    val authorId: Int,
    val authorName: String,
    val text: String,
    val published: String
)

data class PushMessage(
    val recipientId: Long?,
    val content: String
)
