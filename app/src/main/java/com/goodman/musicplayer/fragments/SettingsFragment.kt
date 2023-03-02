package com.goodman.musicplayer.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.goodman.musicplayer.R
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.nterface.myTimerInterface
import com.goodman.musicplayer.services.MusicService
import com.goodman.musicplayer.services.TimerService
import com.goodman.musicplayer.services.countdownServiceRunning
import com.goodman.musicplayer.services.remainingSeconds
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

var SETTING_STOP_MUSIC = true

class SettingsFragment : PreferenceFragmentCompat(), myTimerInterface {

    private var timer: Timer? = null // timer to update timeText during the countdown
    lateinit var timerService: TimerService
    private var serviceIntent: Intent? = null
    private lateinit var timerPreference: ListPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prevThreadButton()


    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)  {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val aboutApp = preferenceManager.findPreference<Preference>("aboutApp")
        aboutApp!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                startActivity(intent)
            }
            false
        }

        timerPreference = preferenceManager.findPreference<ListPreference>("sleepTimer")!!
        timerPreference.value = "off"
        timerPreference.summary = "Off"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdateTimer()
    }


    private fun updateUI() {
//        if (!countdownServiceRunning) {
//            val timerMinutes = (time + 1) * 5
//            val progressText = "$timerMinutes minutes"
//            Log.d("TAG3434", timerMinutes.toString())
//            Log.d("TAG3434", progressText)
//        } else {
//            val asd = remainingSeconds / 60 / 5
//            Log.d("TAG3434", asd.toString())
//        }
    }

    private var timerConnection: ServiceConnection = object: ServiceConnection
    {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder: TimerService.TimerBinder = p1 as TimerService.TimerBinder
            timerService = binder.timerService
            timerService.setCallBack(this@SettingsFragment)
//            timerService.setUI(playerSeekBar, textCurrentTime, textDurationTime, textTitle, textArtist, smallSongName, smallSongArtist, imageAlbumArt, buttonPlay, smallImageAlbumArt, smallButtonPlay, smallButtonNext, favoriteButton)
//            timerService.setListner(this@MainActivity)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    private fun startUpdateTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // only the UI thread can edit the activity appearance
                requireActivity().runOnUiThread {
                    updateUI()
                    if (!countdownServiceRunning) {
                        stopUpdateTimer()
                    }
                    timerPreference.summary = timeStyled
                    if (secondsLeft!! <= 0 || !countdownServiceRunning) {
                        timerPreference.value = "off"
                        timerPreference.summary = "Off"
                    }
                }
            }
        }, 100, 1000)
    }

    private fun stopUpdateTimer() {
        timer?.cancel()
        timer = null
    }


    val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "sleepTimer") {
            val sleepTimer = preferenceManager.findPreference<ListPreference>("sleepTimer")
            Log.d("TAG3434", sleepTimer!!.value)
            if (countdownServiceRunning) {
                startUpdateTimer()
            }
            updateUI()
            if (sleepTimer!!.value == "off") {
                if (serviceIntent != null) {
                    requireContext().stopService(serviceIntent)
                    requireContext().unbindService(timerConnection)
                    serviceIntent = null
                }
                stopUpdateTimer()
            } else {
                if (serviceIntent != null) {
                    requireContext().stopService(serviceIntent)
                    requireContext().unbindService(timerConnection)
                    serviceIntent = null
                }
                stopUpdateTimer()
                val timeout = Date().time + (sleepTimer.value.toInt()) * 60 * 1000

                serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent!!.putExtra("timeout", timeout)
                requireContext().bindService(serviceIntent, timerConnection, Context.BIND_AUTO_CREATE)
                requireContext().startService(serviceIntent)

                Log.d("TAG3434", "timeout" + timeout.toString())
                startUpdateTimer()
            }
        }
    }

    private fun prevThreadButton() {
        sharedPreferencesThread = Thread() {
            run() {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(prefListener);
            }
        }
        sharedPreferencesThread.start()
    }

    lateinit var sharedPreferencesThread: Thread

    companion object {
        var secondsLeft: Int? = 100
        lateinit var timeStyled: String
    }

    override fun onTimerFnished() {
        requireContext().unbindService(timerConnection)
        requireContext().stopService(serviceIntent)
        serviceIntent = null
    }
}