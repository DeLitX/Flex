package com.example.flex

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.flex.POJO.ChatMessage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FlexFirebaseMessagingService: FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        Log.e("firebaseToken","New token $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("MessageCloud", p0.data.toString()+"  "+ (p0.notification?.body ?: ""))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Flex channel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("1", name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        var builder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Flex message")
            .setContentText(p0.data.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify((Calendar.getInstance().timeInMillis%1000000).toInt(), builder.build())
        }
        //val repository=Repository(application)
        //val message=p0.notification?.body.toString()
        //repository.receiveMessage(decodeStringToMessage(message))
    }
    /*private fun decodeStringToMessage(value:String):ChatMessage{

    }*/
}