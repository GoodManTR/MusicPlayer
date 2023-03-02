package com.goodman.musicplayer.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.goodman.musicplayer.ApplicationClass.Companion.ACTION_DESTROY
import com.goodman.musicplayer.ApplicationClass.Companion.ACTION_NEXT
import com.goodman.musicplayer.ApplicationClass.Companion.ACTION_PLAY
import com.goodman.musicplayer.ApplicationClass.Companion.ACTION_PREVIOUS

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        var actionName = p1!!.action
        var serviceIntent: Intent = Intent(p0, MusicService::class.java)
        if (actionName != null) {
            if (actionName == ACTION_PLAY) {
                serviceIntent.putExtra("ActionName", "Play, Pause")
                p0!!.startService(serviceIntent)
            } else if (actionName == ACTION_NEXT) {
                serviceIntent.putExtra("ActionName", "Next")
                p0!!.startService(serviceIntent)
            } else if (actionName == ACTION_PREVIOUS) {
                serviceIntent.putExtra("ActionName", "Previous")
                p0!!.startService(serviceIntent)
            } else if (actionName == ACTION_DESTROY) {
                serviceIntent.putExtra("ActionName", "Destroy")
                p0!!.startService(serviceIntent)
            }
        }
    }
}