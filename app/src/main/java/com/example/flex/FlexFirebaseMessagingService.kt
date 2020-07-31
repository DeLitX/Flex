package com.example.flex

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.flex.POJO.ChatMessage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*

class FlexFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        Log.e("firebaseToken", "New token $p0")
        val mRepository = Repository(application)

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("MessageCloud", p0.data.toString() + "  " + (p0.notification?.body ?: ""))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Flex channel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel("flex", name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val builder = NotificationCompat.Builder(this, "flex")
            .setSmallIcon(R.mipmap.flex_icon)
            .setContentTitle("Flex message")
            .setContentText(p0.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
        with(NotificationManagerCompat.from(this)) {
            notify((Calendar.getInstance().timeInMillis % 1000000).toInt(), builder.build())
        }
        val repository = Repository(application)
        val message = decodeStringToMessage(p0)
        message?.let {
            repository.receiveMessage(it)
        }
    }

    private fun decodeStringToMessage(value: RemoteMessage): ChatMessage? {
        val id: Long
        try {
            val json = JSONObject(value.data)
            id = json["msg_id"].toString().toLong()
        } catch (e: Exception) {
            return null
        }
        return value.notification?.body?.let {
            ChatMessage(
                text = it,
                id = id
            )
        }
    }
}