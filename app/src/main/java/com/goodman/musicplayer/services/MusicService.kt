package com.goodman.musicplayer.services


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioManager.STREAM_MUSIC
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.webkit.WebView
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.goodman.musicplayer.*
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.activities.MainActivity.Companion.tinyDB
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.OnSongComplete
import com.goodman.musicplayer.nterface.actionPlaying
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.jtransforms.fft.DoubleFFT_1D
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

    var player: MediaPlayer? = null
    lateinit var songs: SongModel
    private val musicBind = MusicBinder()
    lateinit var  seekBar: SeekBar
    private val interval = 250
    lateinit var myOnSongComplete: OnSongComplete
    var playerState = STOPPED
    lateinit var mediaSessionCompat: MediaSessionCompat
    lateinit var notiArt: Bitmap
    var pauseCalledByTaskKiller = false
    var nameGetter: String = ""
    var artistGetter: String = ""
    lateinit var notificationManager: NotificationManager
    lateinit var actionPlaying: actionPlaying
    var tempSpeed by Delegates.notNull<Float>()


    // UI
    lateinit var end_point: TextView
    lateinit var song_service_title: TextView
    lateinit var song_service_artist: TextView
    lateinit var small_song_service_title: TextView
    lateinit var small_song_service_artist: TextView
    lateinit var image_Album_Art: ImageView
    lateinit var small_image_Album_Art: ImageView
    lateinit var play_button: ImageButton
    lateinit var small_play_button: ImageButton
    lateinit var small_next_button: ImageButton
    lateinit var favorite_button: ImageButton
    lateinit var start_point: TextView
    lateinit var web_view: WebView

    override fun onBind(p0: Intent?): IBinder? {
        return musicBind
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()
        tinyDB = TinyDB(this)
        currentSongData = SongModel(
            tinyDB.getLong("song_id"), tinyDB.getString("song_title"),
            tinyDB.getString("song_artist"), tinyDB.getString("song_data"),
            tinyDB.getLong("song_date"), tinyDB.getLong("song_album_id"),
            false)
        player = MediaPlayer()
        initMusic()
        mediaSessionCompat = MediaSessionCompat(this, "My Audio")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun initMusic() {
        player?.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player?.setAudioAttributes(AudioAttributes.Builder()
            .setFlags(CONTENT_TYPE_MUSIC)
            .setLegacyStreamType(CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(CONTENT_TYPE_MUSIC)
            .setLegacyStreamType(STREAM_MUSIC)
            .build());
        player?.setOnPreparedListener(this)
        player?.setOnCompletionListener(this)
        player?.setOnErrorListener(this)
    }


    override fun onUnbind(intent: Intent?): Boolean {
        player?.stop()
        player?.reset()
        player?.release()
        player = null
        return super.onUnbind(intent)
    }

    inner class MusicBinder: Binder() {
        val service: MusicService
        get() = this@MusicService
    }

    companion object {
        const val STOPPED = 0
        const val PAUSED = 1
        const val PLAYING = 2
        lateinit var currentSongData: SongModel
        var songPausedFromTimer = false
    }

    private fun playbackSpeedJoke() {
        val speed = PreferenceManager.getDefaultSharedPreferences(this).getFloat("playbackSpeed",
            0F
        )
        tempSpeed = speed
        player?.playbackParams = player?.playbackParams?.setSpeed(speed)!!;
        showNotification(R.drawable.ic_pause, tempSpeed, true)
        return
    }

    private fun audioPitch() {
        val pitch = PreferenceManager.getDefaultSharedPreferences(this).getFloat("audioPitch",0F)
        if (pitch in 0.5F..2.0F) {
            player?.playbackParams = player?.playbackParams?.setPitch(pitch)!!;
        }
    }

    override fun onPrepared(p0: MediaPlayer?) {
        playbackSpeedJoke()
        audioPitch()
        p0!!.start()
        val duration = p0.duration
        seekBar.max = duration
        seekBar.postDelayed(progressRunner, interval.toLong())

        end_point.text = String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong())-
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
        )

        mediaSessionCompat.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, nameGetter)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistGetter)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, notiArt)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player?.duration?.toLong()!!)
                .build()
        )

        showNotification(R.drawable.ic_pause, tempSpeed, true)
        if (isSongOpenedFromPreferences) {
            pauseSong()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent!!.getStringExtra("ActionName")
        if (actionName != null) {
            if (actionName == "Play, Pause") {
                actionPlaying.playPauseBtnClicked()
            } else if (actionName == "Next") {
                actionPlaying.nextBtnClicked()
            } else if (actionName == "Previous") {
                actionPlaying.prevBtnClicked()
            } else if ( actionName == "Destroy") {
                Log.d("TAG3434", "DESTROYING")
                if(player?.isPlaying!!) {
                    actionPlaying.playPauseBtnClicked()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playSong() {

        val bitmap = BitmapFactory.decodeResource(resources, com.goodman.musicplayer.R.drawable.asdas)
        player?.reset()

        val playSong = songs
        val current_songId = playSong.song_id
        val current_trackTitle = playSong.song_title
        val current_trackArtist = playSong.song_artist

        val image = HeaderAdapter.publicMainActivity.getAlbumCover(playSong.song_album_id);

        if (image == null) {
            image_Album_Art.setImageResource(com.goodman.musicplayer.R.drawable.asdas)
            small_image_Album_Art.setImageResource(com.goodman.musicplayer.R.drawable.asdas)
            notiArt = bitmap
        } else {
            image_Album_Art.setImageBitmap(image)
            small_image_Album_Art.setImageBitmap(image)
            notiArt = image!!
        }

        song_service_title.text = current_trackTitle
        song_service_artist.text = current_trackArtist

        small_song_service_title.text = current_trackTitle
        small_song_service_artist.text = current_trackArtist

        nameGetter = current_trackTitle
        artistGetter = current_trackArtist

        play_button.setImageResource(R.drawable.ic_pause)
        small_play_button.setImageResource(R.drawable.ic_pause)

        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, current_songId)
        player?.setDataSource(this, trackUri)

        player?.prepareAsync()
        progressRunner.run()
        favoriteThreadButton(songs)

        currentSongData = SongModel(current_songId, current_trackTitle, current_trackArtist, playSong.song_data, playSong.song_date, playSong.song_album_id, false)

        actionPlaying.setFavoriteButton()

        tinyDB.putLong("song_id", current_songId)
        tinyDB.putString("song_title", current_trackTitle)
        tinyDB.putString("song_artist", current_trackArtist)
        tinyDB.putString("song_data", playSong.song_data)
        tinyDB.putLong("song_date", playSong.song_date)
        tinyDB.putLong("song_album_id", playSong.song_album_id)

    }


    lateinit var myLyrics: TextView
    fun setUI(seekBar: SeekBar, start_int: TextView, end_int: TextView, service_title: TextView,
              service_artist: TextView, small_service_title: TextView, small_service_artist: TextView,
              album_art: ImageView,button_play: ImageButton , small_album_art: ImageView, small_button_play: ImageButton,
              small_button_next: ImageButton, favoriteButton: ImageButton, webView: WebView, lyrics: TextView)
    {

        Log.d("tag", "service_title.toString()")
        this.seekBar = seekBar
        start_point = start_int
        end_point = end_int
        song_service_title = service_title
        small_song_service_title = small_service_title
        song_service_artist = service_artist
        small_song_service_artist = small_service_artist
        image_Album_Art = album_art
        play_button = button_play
        small_image_Album_Art = small_album_art
        small_play_button = small_button_play
        small_next_button = small_button_next
        favorite_button = favoriteButton
        web_view = webView
        myLyrics = lyrics
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    player?.seekTo(p1)
                    if (player?.isPlaying!!) {
                        showNotification(R.drawable.ic_pause, tempSpeed, true)
                    } else {
                        showNotification(R.drawable.ic_play, 0F, false)
                    }

                }
                start_point.text = String.format(
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(p1.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(p1.toLong())-
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(p1.toLong()))
                )

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    val prefListener = OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "playbackSpeed") {
            if (player?.isPlaying!!) {
                playbackSpeedJoke()
            }
        } else if (key == "audioPitch") {
            audioPitch()
        }
    }

    private val progressRunner:Runnable = object: Runnable {
        override fun run() {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(prefListener);
            if (player != null) {
                if (player!!.isPlaying) {
                    showNotification(R.drawable.ic_pause, tempSpeed, true)
                    seekBar.progress = player!!.currentPosition
                    seekBar.postDelayed(this, interval.toLong())
                } else if (!player!!.isPlaying) {
                    showNotification(R.drawable.ic_play, 0F, false)
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    lateinit var networkThread: Thread

    @SuppressLint("SetJavaScriptEnabled")
    private fun favoriteThreadButton(song: SongModel) {
        networkThread = Thread {
            run {
                if (isNetworkAvailable()) {
                    MainActivity.lyricsInterface.lyrics(song)
                } else {
                    myLyrics.text = "No Connection"
                }
            }
        }
        networkThread.start()
    }

    fun setSong(songModel: SongModel) {
        songs = songModel
        playerState = PLAYING
        playSong()
    }

    fun getSongTitle(): String {
        return nameGetter
    }

    fun getSongArtist(): String {
        return artistGetter
    }

    override fun onCompletion(p0: MediaPlayer?) {
        myOnSongComplete.onSongComplete()
    }


    fun setListner(onSongComplete: OnSongComplete) {
        this.myOnSongComplete = onSongComplete
    }

    fun pauseSong() {
        player?.pause()
        playerState = PAUSED
        seekBar.removeCallbacks(progressRunner)

        play_button.setImageResource(R.drawable.ic_play)
        small_play_button.setImageResource(R.drawable.ic_play)

        if (!songPausedFromTimer) {
            showNotification(R.drawable.ic_play, 0F, false)
            songPausedFromTimer = false
        } else if (songPausedFromTimer) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

    }


    fun resumeSong() {
        player!!.start()
        playerState = PLAYING
        progressRunner.run()
        showNotification(R.drawable.ic_pause, tempSpeed, true)
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        Log.d("TAG3434", "MUSIC SERVICE ERROR")
        return false
    }

    fun setCallBack(actionPlaying: actionPlaying) {
        this.actionPlaying = actionPlaying
    }


    fun showNotification(playPauseButton: Int, playbackSpeed: Float, isPlaying: Boolean) {


        val intent = Intent(applicationContext, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val prevIntent: Intent = Intent(this,NotificationReceiver::class.java)
            .setAction(ApplicationClass.ACTION_PREVIOUS)
        val prevPendingIntent: PendingIntent = PendingIntent.getBroadcast(this,0,prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent: Intent = Intent(this,NotificationReceiver::class.java)
            .setAction(ApplicationClass.ACTION_PLAY)
        val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(this,0,pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent: Intent = Intent(this,NotificationReceiver::class.java)
            .setAction(ApplicationClass.ACTION_NEXT)
        val nextPendingIntent: PendingIntent = PendingIntent.getBroadcast(this,0,nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val destroyIntent: Intent = Intent(this,NotificationReceiver::class.java)
            .setAction(ApplicationClass.ACTION_DESTROY)
        val destroyPendingIntent: PendingIntent = PendingIntent.getBroadcast(this,0,destroyIntent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_1)
            .setContentIntent(pIntent)
            .setSmallIcon(R.drawable.ic_baseline_music_note)
            .setLargeIcon(notiArt)
            .setContentTitle(nameGetter)
            .setContentText(artistGetter)
            .addAction(R.drawable.ic_previous ,"Previous", prevPendingIntent)
            .addAction(playPauseButton ,"Play, Pause", pausePendingIntent)
            .addAction(R.drawable.ic_next ,"Next", nextPendingIntent)
            .addAction(R.drawable.ic_baseline_close ,"Destroy", destroyPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1,2)
                .setMediaSession(mediaSessionCompat.sessionToken))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()


        if (!isPlaying) {
            mediaSessionCompat.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, player!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build()
            )
        } else {
            mediaSessionCompat.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, player!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build()
            )
        }

        mediaSessionCompat.setCallback(object: MediaSessionCompat.Callback(){
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                player!!.seekTo(pos.toInt())
                val playBackStateNew = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, player!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build()
                mediaSessionCompat.setPlaybackState(playBackStateNew)
                seekBar.progress = pos.toInt()
            }

            override fun onPause() {
                super.onPause()
                actionPlaying.playPauseBtnClicked()
            }

            override fun onPlay() {
                super.onPlay()
                actionPlaying.playPauseBtnClicked()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                actionPlaying.nextBtnClicked()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                actionPlaying.prevBtnClicked()
            }
        })


        startForeground(5, notification)
//        notificationManager.notify(3, notification)

    }

    fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    Log.d("TAG3434", "TRUE")
                    return true
                }
            }
        }
        Log.d("TAG3434", "FALSE")
        return false
    }

    lateinit var notiThread: Thread
}