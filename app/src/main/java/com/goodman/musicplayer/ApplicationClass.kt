package com.goodman.musicplayer

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.opengl.Visibility
import android.os.Build
import androidx.core.content.getSystemService
import com.goodman.musicplayer.activities.MainActivity

class ApplicationClass: Application() {

    companion object {
        var CHANNEL_ID_1 = "channel1"
        var ACTION_PREVIOUS = "actionprevious"
        var ACTION_NEXT = "actionnext"
        var ACTION_PLAY = "actionplay"
        var ACTION_DESTROY = "actiondestroy"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel(CHANNEL_ID_1,"Playback", NotificationManager.IMPORTANCE_MIN)
            channel.description = "Playback Controls"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC


            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }
    }
}