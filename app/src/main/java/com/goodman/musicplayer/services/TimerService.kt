package com.goodman.musicplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import com.goodman.musicplayer.activities.MainActivity.Companion.musicService
import com.goodman.musicplayer.fragments.SETTING_STOP_MUSIC
import com.goodman.musicplayer.fragments.SettingsFragment.Companion.secondsLeft
import com.goodman.musicplayer.fragments.SettingsFragment.Companion.timeStyled
import com.goodman.musicplayer.nterface.actionPlaying
import com.goodman.musicplayer.nterface.myTimerInterface
import java.util.*

var countdownServiceRunning = false
var remainingTimeText = "--:--:--"
var remainingSeconds = 0

class TimerService : Service() {
    private var timeout = Date(0)
    private val timer = Timer() // timer to update the notification during the countdown
    private val timerBind = TimerBinder()
    lateinit var myTimerInterface: myTimerInterface

    override fun onBind(intent: Intent): IBinder {
        return timerBind
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timeout = Date(intent?.getLongExtra("timeout", 0) ?: 0)
        Log.d("TAG3434", "SERVICE TIMER $timeout")
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                update()
            }
        }, 0, 1000)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        countdownServiceRunning = true
    }

    inner class TimerBinder: Binder() {
        val timerService: TimerService
        get() = this@TimerService
    }

    override fun onDestroy() {
        countdownServiceRunning = false
        timer.cancel()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }


    fun setCallBack(myTimerInterface: myTimerInterface) {
        this.myTimerInterface = myTimerInterface
    }



    fun update() {
        val seconds = (timeout.time - Date().time).toInt() / 1000
        Log.d("TAG3434", seconds.toString())
        secondsLeft = seconds
        if (seconds > 0) {
            // build time string of the remaining time
            val remainingTime = StringBuilder()
            if (seconds / 3600 > 0) {
                remainingTime.append(seconds / 3600)
                remainingTime.append(":")
            }
            remainingTime.append(String.format("%02d:%02d", seconds / 60 % 60, seconds % 60))

            // update global time variables
            remainingTimeText = remainingTime.toString()
            timeStyled = remainingTimeText
            remainingSeconds = seconds
        } else {
            // timer endend
            myTimerInterface.onTimerFnished()
            MusicService.songPausedFromTimer = true
            musicService.pauseSong()
            stopSelf()
        }
    }
}