package com.example.testapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

const val channelID = "NotificationChannel"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //Führt Fallunterscheidung für die Art der Notification durch, und zeigt die Notification dann an
    override fun onMessageReceived(message: RemoteMessage) {

        super.onMessageReceived(message)
        var intent: Intent

        if(message.data["title"] == "Your Turn!" && MyApplication.ticTacToeOpen){
            return
        }

        intent = Intent(this, GameSelect::class.java)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        var notification: Notification
        if(message.data["title"] == "Your Turn!"){
            notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.ic_stat_appicon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                .build()
        }
        else {

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
            notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.ic_stat_appicon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                .setContentIntent(pendingIntent)
                .build()
        }

        notificationManager.notify(notificationID, notification)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager : NotificationManager){

        val channelName = "Spielesammlung"
        val channel = NotificationChannel(channelID, channelName, IMPORTANCE_HIGH).apply {

            description = "description"
            enableLights(true)
            lightColor = Color.MAGENTA
        }
        notificationManager.createNotificationChannel(channel)

    }
}