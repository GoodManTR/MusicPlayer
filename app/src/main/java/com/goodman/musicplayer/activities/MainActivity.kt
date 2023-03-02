package com.goodman.musicplayer.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.marginTop
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.goodman.musicplayer.*
import com.goodman.musicplayer.AlbumsFrag.Companion.albumInfoList
import com.goodman.musicplayer.AlbumsFrag.Companion.albumSongInfoList
import com.goodman.musicplayer.ArtistsFrag.Companion.artistInfoList
import com.goodman.musicplayer.ArtistsFrag.Companion.artistSongInfoList
import com.goodman.musicplayer.ArtistsFrag.Companion.myArtistsRecylerAdapter
import com.goodman.musicplayer.adapters.PlayerVPAdapter
import com.goodman.musicplayer.adapters.VPAdapter
import com.goodman.musicplayer.data.Favorite
import com.goodman.musicplayer.data.FavoritesViewModel
import com.goodman.musicplayer.data.PlaylistDatabaseViewModel
import com.goodman.musicplayer.data.PlaylistEntity
import com.goodman.musicplayer.fragments.*
import com.goodman.musicplayer.fragments.FavoritesFrag.Companion.favoriteSongInfoList
import com.goodman.musicplayer.fragments.FavoritesFrag.Companion.myFavoriteSongListAdapter
import com.goodman.musicplayer.fragments.LyricsFragment.Companion.setLyricsInterface
import com.goodman.musicplayer.fragments.PiecesFragment.Companion.songInfoList
import com.goodman.musicplayer.models.ArtistsModel
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.LyricsInterface
import com.goodman.musicplayer.nterface.OnSongComplete
import com.goodman.musicplayer.nterface.actionPlaying
import com.goodman.musicplayer.services.MusicService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lyrics.*
import kotlinx.android.synthetic.main.fragment_player.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList


class MainActivity: AppCompatActivity(), SearchView.OnQueryTextListener, OnSongComplete, actionPlaying,
    LyricsInterface {

    companion object {
        var isSearched = false
        lateinit var songModel: SongModel
        var playerintent: Intent? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var musicService: MusicService
        lateinit var lastSong: SongModel
        lateinit var mFavoritesViewModel: FavoritesViewModel
        lateinit var mPlaylistDatabaseViewModel: PlaylistDatabaseViewModel
        @SuppressLint("StaticFieldLeak")
        lateinit var tinyDB: TinyDB
        lateinit var lyricsInterface: LyricsInterface
        lateinit var myLyricSong: SongModel
        lateinit var lyricURL: String
    }


//    private lateinit var mPlaylistViewModel: PlaylistViewModel




    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createUI()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        lyricsInterface = this

        if( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_AUDIO), 123)
        } else {
            createApp()
        }
    }

    fun createApp() {
        Log.d("TAG3461", "Goodman Music Player Starting...")
        sheetFunc()
        createTabs()
        marquee()

        navigation.itemIconTintList = null;

        supportFragmentManager.beginTransaction().replace(R.id.settingsFrameLayout, SettingsFragment()).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.searchFrameLayout, SearchFragment()).commitNow()

        mFavoritesViewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]
        mPlaylistDatabaseViewModel = ViewModelProvider(this)[PlaylistDatabaseViewModel::class.java]

        setMusicConnection()
        if(playerintent == null) {
            playerintent = Intent(this, MusicService::class.java)
            bindService(playerintent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playerintent)
        }
        bindService(playerintent, musicConnection, Context.BIND_AUTO_CREATE)


    }

    lateinit var musicConnection: ServiceConnection
    fun setMusicConnection() {
        musicConnection = object: ServiceConnection
        {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder: MusicService.MusicBinder = p1 as MusicService.MusicBinder
                musicService = binder.service
                musicService.setCallBack(this@MainActivity)
                musicService.setUI(playerSeekBar, textCurrentTime, textDurationTime, textTitle, textArtist, smallSongName, smallSongArtist, imageAlbumArt, buttonPlay, smallImageAlbumArt, smallButtonPlay, smallButtonNext, favoriteButton, myLyricsWebView, lyricsTextView)
                musicService.setListner(this@MainActivity)
                if (tinyDB.getString("song_title") != "") {
                    getSharedPreferences()
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {

            }
        }
    }

    fun createUI() {
        val nightModeFlags: Int = this.resources
            .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val window: Window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(com.goodman.musicplayer.R.color.colorBackground)))
                supportActionBar!!.elevation = 0F
                window.statusBarColor = Color.parseColor("#000000")
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(com.goodman.musicplayer.R.color.tabs_color)))
                supportActionBar!!.elevation = 0F
                window.statusBarColor = Color.parseColor("#F2F2F2")
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    fun marquee() {
        smallSongName.isSelected = true
        smallSongArtist.isSelected = true
    }


    class MyJavaScriptInterface() {

        fun similarity(s1: String, s2: String): Double {
            var longer = s1
            var shorter = s2
            if (s1.length < s2.length) { // longer should always have greater length
                longer = s2
                shorter = s1
            }
            val longerLength = longer.length
            return if (longerLength == 0) {
                1.0 /* both strings are zero length */
            } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
        }


        fun editDistance(s1: String, s2: String): Int {
            var s1 = s1
            var s2 = s2
            s1 = s1.lowercase(Locale.getDefault())
            s2 = s2.lowercase(Locale.getDefault())
            val costs = IntArray(s2.length + 1)
            for (i in 0..s1.length) {
                var lastValue = i
                for (j in 0..s2.length) {
                    if (i == 0) costs[j] = j else {
                        if (j > 0) {
                            var newValue = costs[j - 1]
                            if (s1[i - 1] != s2[j - 1]) newValue = Math.min(
                                Math.min(newValue, lastValue),
                                costs[j]
                            ) + 1
                            costs[j - 1] = lastValue
                            lastValue = newValue
                        }
                    }
                }
                if (i > 0) costs[s2.length] = lastValue
            }
            return costs[s2.length]
        }

        @JavascriptInterface
        fun processHTML(html: String?) {
            val document: Document = Jsoup.parse(html)

            val elements: Elements = document.select("mini-song-card").select("a[href]")
            val names: Elements = document.select(".mini_card-title_and_subtitle").select(".mini_card-title")
            val artists: Elements = document.select(".mini_card-subtitle")

            val tagNames: ArrayList<String> = ArrayList()
            val songNames: ArrayList<String> = ArrayList()
            val songArtists: ArrayList<String> = ArrayList()

            val nameSimilarities: ArrayList<Double> = ArrayList()
            val artistSimilarities: ArrayList<Double> = ArrayList()

            for (name in names) {
                songNames.add(name.html())
            }

            for (artist in artists) {
                songArtists.add(artist.html().replace("<!---->", "").replace("&amp;", "&"))
            }

            for (element in elements) {
                tagNames.add(element.attr("href"))
            }

            for (songName in songNames) {
                nameSimilarities.add(similarity(songName, myLyricSong.song_title))
                Log.d("TAG5555", similarity(songName, myLyricSong.song_title).toString())
            }

            for (songArtist in songArtists) {
                if (myLyricSong.song_artist == "<unknown>") {
                    artistSimilarities.add(similarity(songArtist, myLyricSong.song_title))
                } else {
                    artistSimilarities.add(similarity(songArtist, myLyricSong.song_artist))
                }
            }

            val maxValueOfName = nameSimilarities.max()
            val maxValueOfArtist = artistSimilarities.max()

            val indexOfMaxName = nameSimilarities.indexOf(maxValueOfName)
            val indexOfMaxArtist = artistSimilarities.indexOf(maxValueOfArtist)


            lyricURL = ""
            if (indexOfMaxName != indexOfMaxArtist) {
                lyricURL = tagNames[indexOfMaxArtist]
            }
            if (indexOfMaxName == indexOfMaxArtist) {
                lyricURL = tagNames[indexOfMaxName]
            }

            lyricsInterface.lyrics2(lyricURL)


        }
    }
    class MyJavaScriptInterface2() {

        companion object {
            val myLyrics: ArrayList<String> = ArrayList()
        }

        @JavascriptInterface
        fun processHTML(html: String?) {
            val document: Document = Jsoup.parse(html)
            val lyrics: Elements = document.select(".Lyrics__Container-sc-1ynbvzw-6")

            val outputSettings = Document.OutputSettings()
            outputSettings.prettyPrint(false)


            myLyrics.clear()
            for (lyric in lyrics) {
                lyric.select("div").remove()
                myLyrics.add(Jsoup.clean(lyric.html(), "", Whitelist.relaxed().removeTags("a")
                    .removeTags("span").removeTags("div").removeTags("img"),
                    outputSettings).replace("<br>", "\n") + "\n")
            }

            Log.d("TAG3436", myLyrics.toString())
            setLyricsInterface.setLyrics(myLyrics)

        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun lyrics(songModel: SongModel) {
        myLyricsWebView.post {
            myLyricSong = songModel
            Log.d("TAG3434", "WEB")
            Log.d("TAG3434", songModel.song_title)
            var url = ""
            url = "https://genius.com/search?q=${songModel.song_title.replace(" ","%20")}"
            Log.d("TAG3434", url)



            myLyricsWebView.settings.javaScriptEnabled = true
            myLyricsWebView.settings.domStorageEnabled = true;
            myLyricsWebView.settings.loadsImagesAutomatically = true
            myLyricsWebView.settings.builtInZoomControls = true
            myLyricsWebView.addJavascriptInterface(MyJavaScriptInterface(), "HTMLOUT")

            myLyricsWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    /* This call inject JavaScript into the page which just finished loading. */
                    myLyricsWebView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
                }
            }

            myLyricsWebView.loadUrl(url)
        }
    }
    override fun lyrics2(url: String) {
        Log.d("TAG3435", "LYRIC2")
        myLyricsWebView.post {
            myLyricsWebView.removeJavascriptInterface("HTMLOUT")
            myLyricsWebView.addJavascriptInterface(MyJavaScriptInterface2(), "HTMLOUT")
            myLyricsWebView.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    /* This call inject JavaScript into the page which just finished loading. */
                    myLyricsWebView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
                    myLyricsWebView.stopLoading()
                }
            })

            myLyricsWebView.loadUrl(lyricURL)
        }
    }

    private fun getSharedPreferences()
    {
        if (tinyDB.getString("song_title") != null) {
            val id = tinyDB.getLong("song_id")
            val title = tinyDB.getString("song_title")
            val artist =  tinyDB.getString("song_artist")
            val data =  tinyDB.getString("song_data")
            val date =  tinyDB.getLong("song_date")
            val albumId = tinyDB.getLong("song_album_id")
            isSongOpenedFromPreferences = true

            lastSong = SongModel(id, title,artist,data ,date, albumId, false)
            musicService.setSong(lastSong)
        }
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true)
    {
        override fun handleOnBackPressed() {
            if (BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(bottomLayout).state) {
                BottomSheetBehavior.from(bottomLayout).state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(bottomSettingsSheet).state) {
                BottomSheetBehavior.from(bottomSettingsSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(everythingBottomSheet).state) {
                BottomSheetBehavior.from(everythingBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(addToBottomSheet).state) {
                BottomSheetBehavior.from(addToBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                HeaderAdapter.selectedItemList.clear()
            } else if (BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(searchSheet).state) {
                BottomSheetBehavior.from(searchSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                val lManager: InputMethodManager = this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                lManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            } else if (HeaderAdapter.isPiecesSelectionEnable) {
                HeaderAdapter.selectedItemList.clear()
                longSelectDestroyer()
            }
        }
    }

    private fun sheetFunc()
    {
        everythingToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        everythingToolbar.setNavigationOnClickListener {
            BottomSheetBehavior.from(everythingBottomSheet).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }

        settingToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        settingToolbar.setNavigationOnClickListener {
            BottomSheetBehavior.from(bottomSettingsSheet).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }

        addToToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        addToToolbar.setNavigationOnClickListener {
            BottomSheetBehavior.from(addToBottomSheet).state =
                BottomSheetBehavior.STATE_COLLAPSED
                HeaderAdapter.selectedItemList.clear()
        }

        searchToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        searchToolbar.setNavigationOnClickListener {
            BottomSheetBehavior.from(searchSheet).state =
                BottomSheetBehavior.STATE_COLLAPSED
            val lManager: InputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            lManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        BottomSheetBehavior.from(bottomSettingsSheet).apply {
            peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        BottomSheetBehavior.from(addToBottomSheet).apply {
            peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
            this.isDraggable = false
        }

        BottomSheetBehavior.from(searchSheet).apply {
            peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
            this.isDraggable = false
        }


        BottomSheetBehavior.from(bottomLayout).apply {
            val inner = findViewById<ConstraintLayout>(R.id.bottomLayout)
            inner.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    inner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val hidden = inner.getChildAt(4)
                    peekHeight = hidden.bottom + hidden.marginTop
                }
            })
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        if (BottomSheetBehavior.from(bottomLayout).state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomLayout.setOnClickListener {
                BottomSheetBehavior.from(bottomLayout).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        BottomSheetBehavior.from(everythingBottomSheet).apply {
            peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
            this.isDraggable = false
        }

        BottomSheetBehavior.from(bottomLayout).addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                smallSongName.animate().alpha( 1 - slideOffset)
                smallSongArtist.animate().alpha( 1 - slideOffset)
                smallImageAlbumArt.animate().alpha( 1 - slideOffset)
                smallButtonPlay.animate().alpha( 1 - slideOffset)
                smallButtonNext.animate().alpha( 1 - slideOffset)
                textNowPlaying.animate().alpha(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        smallButtonPlay.visibility = View.VISIBLE
                        smallButtonNext.visibility = View.VISIBLE
                        treeDotsButton.visibility = View.GONE
                        textNowPlaying.visibility = View.INVISIBLE
                        BottomSheetBehavior.from(bottomLayout).isDraggable = true
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {}
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        treeDotsButton.visibility = View.VISIBLE
                        smallButtonPlay.visibility = View.GONE
                        smallButtonNext.visibility = View.GONE
                        textNowPlaying.visibility = View.VISIBLE
                        if (playerTabLayout.selectedTabPosition == 1) {
                            BottomSheetBehavior.from(bottomLayout).isDraggable = false
                        } else {
                            BottomSheetBehavior.from(bottomLayout).isDraggable = true
                        }
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
                    BottomSheetBehavior.STATE_SETTLING -> {}
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                }
            }
        })

    }

    override fun onDestroy()
    {
        Log.d("TAG3434", "DESTROYED")
        stopService(playerintent)
        super.onDestroy()
    }

    private fun createTabs()
    {
        val myPlaylistVpAdapter = PlayerVPAdapter(supportFragmentManager, lifecycle)
        playerViewPager2.adapter = myPlaylistVpAdapter
        myPlaylistVpAdapter.addFragment(PlayerFragment(), "Player")
        myPlaylistVpAdapter.addFragment(LyricsFragment(), "Lyrics")
        playerViewPager2.offscreenPageLimit = 2


        playerTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                playerViewPager2.children.find { it is RecyclerView }?.let {
                    if (tab!!.position == 0) {
                        (it as RecyclerView).isNestedScrollingEnabled = false
                        BottomSheetBehavior.from(bottomLayout).isDraggable = true
                    } else {
                        BottomSheetBehavior.from(bottomLayout).isDraggable = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        TabLayoutMediator(playerTabLayout, playerViewPager2) {tab, position ->
            playerViewPager2.setCurrentItem(tab.position, true)

            if (position == 0)
                tab.text = "Player"
            if (position == 1)
                tab.text = "Lyrics"
        }.attach()

        val myVpAdapter = VPAdapter(supportFragmentManager, lifecycle)
        myViewPager2.adapter = myVpAdapter
        myVpAdapter.addFragment(PiecesFragment(),"Songs")
        myVpAdapter.addFragment(AlbumsFrag(),"Albums")
        myVpAdapter.addFragment(ArtistsFrag(),"Artists")
        myVpAdapter.addFragment(FavoritesFrag(),"Favorites")
        myVpAdapter.addFragment(TrackListsFragment(),"TrackLists")
        myViewPager2.offscreenPageLimit = 5



        TabLayoutMediator(tabLayout, myViewPager2) {tab, position ->
            myViewPager2.setCurrentItem(tab.position, true)

            if (position == 0)
                tab.text = "Songs"
            if (position == 1)
                tab.text = "Albums"
            if (position == 2)
                tab.text = "Artists"
            if (position == 3)
                tab.text = "Favorites"
            if (position == 4)
                tab.text = "Playlists"
        }.attach()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            when (Build.VERSION.SDK_INT) {
                in 1..32 -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this,"Permission Granted!", Toast.LENGTH_SHORT).show()
                            createTabs()
                        }
                    } else {
                        Toast.makeText(this,"No Permission Granted!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                else -> {
                    if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"Permission Granted!", Toast.LENGTH_SHORT).show()
                        createApp()
                    }else {
                        Toast.makeText(this,"No Permission Granted!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            return
        }
    }


    //<Searchbar>

//    val menuItem = menu.findItem(R.id.search_option)
//    val searchView: SearchView = menuItem.actionView as SearchView
//    searchView.setOnQueryTextListener(this)


    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        if (tabLayout.selectedTabPosition == 4) {
            menuInflater.inflate(R.menu.track_list_actionbar, menu)
        } else {
            menuInflater.inflate(R.menu.search_menu, menu)
            Log.d("TAG1212", "OPTİONS")
        }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            if (BottomSheetBehavior.from(searchSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.from(searchSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                val lManager: InputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                lManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                BottomSheetBehavior.from(bottomSettingsSheet).state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                BottomSheetBehavior.from(bottomSettingsSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
            true
        }
        R.id.action_createTrackList -> {
            if (BottomSheetBehavior.from(everythingBottomSheet).state ==  BottomSheetBehavior.STATE_COLLAPSED) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Create track list")
                val input = EditText(this);
                input.inputType = InputType.TYPE_CLASS_TEXT;
                builder.setView(input)
                builder.setNegativeButton("Cancel", null)
                builder.setPositiveButton("Create") { dialogInterface, i ->
                    val generator = Date().time
                    if (input.text.toString() != "") {
                        mPlaylistDatabaseViewModel.insertPlaylistEntity(PlaylistEntity(generator, input.text.toString()))
                    }
                    TrackListsFragment.myTrackListAdapter.notifyDataSetChanged()
                }
                builder.show()
            } else {
            }
            true
        }
        R.id.search_option -> {
            if (BottomSheetBehavior.from(bottomSettingsSheet).state == BottomSheetBehavior.STATE_EXPANDED || BottomSheetBehavior.from(everythingBottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.from(bottomSettingsSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.from(everythingBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.from(searchSheet).state = BottomSheetBehavior.STATE_EXPANDED
                searchEditText.requestFocusFromTouch()
                val lManager: InputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                lManager.showSoftInput(searchEditText, 0)
            } else {
                BottomSheetBehavior.from(searchSheet).state = BottomSheetBehavior.STATE_EXPANDED
                searchEditText.requestFocusFromTouch()
                val lManager: InputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                lManager.showSoftInput(searchEditText, 0)
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean
    {
        isSearched = false
        return false
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onQueryTextChange(p0: String?): Boolean
    {
        val userInput = p0!!.lowercase(Locale.getDefault())

        isSearched = true

        if (tabLayout.selectedTabPosition == 0) {
            val myFiles = ArrayList<SongModel>()
            PiecesFragment.songInfoList2.forEach { song ->
                var islist_added = false
                if (song.song_title.lowercase(Locale.getDefault()).contains(userInput)) {
                    myFiles.add(song)
                    islist_added = true
                }
                if (song.song_artist.lowercase(Locale.getDefault()).contains(userInput)) {
                    if(!islist_added)
                        myFiles.add(song)
                }
            }
            PiecesFragment.myMusicListViewAdapter.updateList(myFiles)
            songInfoList = myFiles
        } else if (tabLayout.selectedTabPosition == 1) {
            val myFiles = ArrayList<MyAlbumsDataClass>()
            AlbumsFrag.albumInfoList2.forEach { song ->
                var islist_added = false
                if (song.songName.lowercase(Locale.getDefault()).contains(userInput)) {
                    myFiles.add(song)
                    islist_added = true
                }
                if (song.songArtist.lowercase(Locale.getDefault()).contains(userInput)) {
                    if(!islist_added)
                        myFiles.add(song)
                }
            }
            AlbumsFrag.myAlbumAdapter.updateList(myFiles)
            albumInfoList = myFiles
        } else if (tabLayout.selectedTabPosition == 2) {
            val myFiles = ArrayList<ArtistsModel>()
            ArtistsFrag.artistInfoList2.forEach { song ->
                var islist_added = false
                if (song.artist.lowercase(Locale.getDefault()).contains(userInput)) {
                    myFiles.add(song)
                    islist_added = true
                }
                if (song.artist.lowercase(Locale.getDefault()).contains(userInput)) {
                    if(!islist_added)
                        myFiles.add(song)
                }
            }
            myArtistsRecylerAdapter.updateList(myFiles)
            artistInfoList = myFiles
        } else if (tabLayout.selectedTabPosition == 3) {
            val myFiles = ArrayList<SongModel>()
            FavoritesFrag.favoriteSongInfoList2.forEach { song ->
                var islist_added = false
                if (song.song_title.lowercase(Locale.getDefault()).contains(userInput)) {
                    myFiles.add(song)
                    islist_added = true
                }
                if (song.song_artist.lowercase(Locale.getDefault()).contains(userInput)) {
                    if(!islist_added)
                        myFiles.add(song)
                }
            }
            myFavoriteSongListAdapter.updateList(myFiles)
            favoriteSongInfoList = myFiles
        }
        return true
    }
    //</Searchbar>





    @SuppressLint("NewApi")
    override fun onSongComplete() {
        if (!isSongOpenedFromAlbum && !isSongOpenedFromArtist && !isSongOpenedFromPreferences && !isSongOpenedFromFavorites) {
            if (repeatState == 0) {
                if(songInfoList.size > 0) {
                    if(currentSong != -1) {
                        if(songInfoList.size - 1 == currentSong) {
                            currentSong = 0
                            try {
                                musicService.setSong(songInfoList[currentSong])
                                songModel = songInfoList[currentSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentSong++
                            try {
                                musicService.setSong(songInfoList[currentSong])
                                songModel = songInfoList[currentSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (repeatState == 1) {
                try {
                    musicService.setSong(songInfoList[currentSong])
                    songModel = songInfoList[currentSong]
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 2) {
                if (repeatCount == 0) {
                    repeatCount++
                    try {
                        musicService.setSong(songInfoList[currentSong])
                        songModel = songInfoList[currentSong]
                        currentSong++
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    repeatCount = 0
                    if(songInfoList.size == currentSong) {
                        currentSong = 0
                        try {
                            musicService.setSong(songInfoList[currentSong])
                            songModel = songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        try {
                            musicService.setSong(songInfoList[currentSong])
                            songModel = songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        } else if (isSongOpenedFromAlbum) {
            if (repeatState == 0) {
                if(albumSongInfoList.size > 0) {
                    if(currentAlbumSong != -1) {
                        if(albumSongInfoList.size - 1 == currentAlbumSong) {
                            currentAlbumSong = 0
                            try {
                                musicService.setSong(albumSongInfoList[currentAlbumSong])
                                songModel = albumSongInfoList[currentAlbumSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentAlbumSong++
                            try {
                                musicService.setSong(albumSongInfoList[currentAlbumSong])
                                songModel = albumSongInfoList[currentAlbumSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (repeatState == 1) {
                try {
                    musicService.setSong(albumSongInfoList[currentAlbumSong])
                    songModel = albumSongInfoList[currentAlbumSong]
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 2) {
                if (repeatCount == 0) {
                    repeatCount++
                    try {
                        musicService.setSong(albumSongInfoList[currentAlbumSong])
                        songModel = albumSongInfoList[currentAlbumSong]
                        currentAlbumSong++
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    repeatCount = 0
                    if(albumSongInfoList.size == currentAlbumSong) {
                        currentAlbumSong = 0
                        try {
                            musicService.setSong(albumSongInfoList[currentAlbumSong])
                            songModel = albumSongInfoList[currentAlbumSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        try {
                            musicService.setSong(albumSongInfoList[currentAlbumSong])
                            songModel = albumSongInfoList[currentAlbumSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        } else if (isSongOpenedFromArtist) {
            if (repeatState == 0) {
                if(artistSongInfoList.size > 0) {
                    if(currentArtistSong != -1) {
                        if(artistSongInfoList.size - 1 == currentArtistSong) {
                            currentArtistSong = 0
                            try {
                                musicService.setSong(artistSongInfoList[currentArtistSong])
                                songModel = artistSongInfoList[currentArtistSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentArtistSong++
                            try {
                                musicService.setSong(artistSongInfoList[currentArtistSong])
                                songModel = artistSongInfoList[currentArtistSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (repeatState == 1) {
                try {
                    musicService.setSong(artistSongInfoList[currentArtistSong])
                    songModel = artistSongInfoList[currentArtistSong]
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 2) {
                if (repeatCount == 0) {
                    repeatCount++
                    try {
                        musicService.setSong(artistSongInfoList[currentArtistSong])
                        songModel = artistSongInfoList[currentArtistSong]
                        currentArtistSong++
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    repeatCount = 0
                    if(artistSongInfoList.size == currentArtistSong) {
                        currentArtistSong = 0
                        try {
                            musicService.setSong(artistSongInfoList[currentArtistSong])
                            songModel = artistSongInfoList[currentArtistSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        try {
                            musicService.setSong(artistSongInfoList[currentArtistSong])
                            songModel = artistSongInfoList[currentArtistSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        } else if (isSongOpenedFromPreferences) {
            if (repeatState == 0) {
                isSongOpenedFromPreferences = false
                var lastSongIndex = songInfoList.map { it.song_id }.indexOf(lastSong.song_id)
                lastSongIndex++
                currentSong = lastSongIndex
                try {
                    musicService.setSong(songInfoList[lastSongIndex])
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 1) {
                isSongOpenedFromPreferences = false
                val lastSongIndex = songInfoList.map { it.song_id }.indexOf(lastSong.song_id)
                currentSong = lastSongIndex
                try {
                    musicService.setSong(songInfoList[lastSongIndex])
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 2) {
                if (repeatCount == 0) {
                    repeatCount++
                    isSongOpenedFromPreferences = false
                    val lastSongIndex = songInfoList.map { it.song_id }.indexOf(lastSong.song_id)
                    currentSong = lastSongIndex
                    try {
                        musicService.setSong(songInfoList[lastSongIndex])
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    repeatCount = 0
                    val lastSongIndexx = songInfoList.map { it.song_id }.indexOf(lastSong.song_id)
                    currentSong = lastSongIndexx
                    if(songInfoList.size == currentSong) {
                        isSongOpenedFromPreferences = false
                        currentSong = 0
                        try {
                            musicService.setSong(songInfoList[currentSong])
                            songModel = songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        isSongOpenedFromPreferences = false
                        var lastSongIndex = songInfoList.map { it.song_id }.indexOf(lastSong.song_id)
                        lastSongIndex++
                        currentSong = lastSongIndex
                        try {
                            musicService.setSong(songInfoList[currentSong])
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        } else if (isSongOpenedFromFavorites) {
            if (repeatState == 0) {
                if(favoriteSongInfoList.size > 0) {
                    if(currentFavoriteSong != -1) {
                        if(favoriteSongInfoList.size - 1 == currentFavoriteSong) {
                            currentFavoriteSong = 0
                            try {
                                musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                                songModel = favoriteSongInfoList[currentFavoriteSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentFavoriteSong++
                            try {
                                musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                                songModel = favoriteSongInfoList[currentFavoriteSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (repeatState == 1) {
                try {
                    musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                    songModel = favoriteSongInfoList[currentFavoriteSong]
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (repeatState == 2) {
                if (repeatCount == 0) {
                    repeatCount++
                    try {
                        musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                        songModel = favoriteSongInfoList[currentFavoriteSong]
                        currentFavoriteSong++
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    repeatCount = 0
                    if(favoriteSongInfoList.size == currentFavoriteSong) {
                        currentFavoriteSong = 0
                        try {
                            musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                            songModel = favoriteSongInfoList[currentFavoriteSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        try {
                            musicService.setSong(favoriteSongInfoList[currentFavoriteSong])
                            songModel = favoriteSongInfoList[currentFavoriteSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        playThreadButton()
        nextThreadButton()
        prevThreadButton()
        repeatThreadButton()
        shuffleThreadButton()
        favoriteThreadButton()
        treeDotsThreadButton()
    }

    private fun playThreadButton() {
        playThread = Thread {
            run {
                buttonPlay.setOnClickListener {
                    playPauseBtnClicked()
                }
                smallButtonPlay.setOnClickListener {
                    playPauseBtnClicked()
                }
            }
        }
        playThread.start()
    }

    private fun nextThreadButton() {
        nextThread = Thread {
            run {
                buttonNext.setOnClickListener {
                    nextBtnClicked()
                }
                smallButtonNext.setOnClickListener {
                    nextBtnClicked()
                }
            }
        }
        nextThread.start()
    }

    private fun prevThreadButton() {
        prevThread = Thread {
            run {
                buttonPrevious.setOnClickListener {
                    prevBtnClicked()
                }
            }
        }
        prevThread.start()
    }

    private fun repeatThreadButton() {
        repeatButtonThread = Thread {
            run {
                buttonRepeat.setOnClickListener {
                    repeatButtonClicked()
                }
            }
        }
        repeatButtonThread.start()
    }

    private fun shuffleThreadButton() {
        shuffleButtonThread = Thread {
            run {
                buttonShuffle.setOnClickListener {
                    shuffleButtonClicked()
                }
            }
        }
        shuffleButtonThread.start()
    }



    private fun favoriteThreadButton() {
        favoriteButtonThread = Thread {
            run {
                favoriteButton.setOnClickListener {
                    favoriteButtonClicked()
                }
            }
        }
        favoriteButtonThread.start()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun favoriteButtonClicked() {
        if (favoriteState == 0) {
            favoriteState = 1
            insertDataToDatabase()
            favoriteButton.setImageResource(R.drawable.ic_favorite)
        } else {
            favoriteState = 0
            MainActivity.mFavoritesViewModel.deleteFavorite(
                Favorite(
                    MusicService.currentSongData.song_id, MusicService.currentSongData.song_title,
                    MusicService.currentSongData.song_artist,
                    MusicService.currentSongData.song_data,
                    MusicService.currentSongData.song_date, MusicService.currentSongData.song_album_id)
            )
            FavoritesFrag.myFavoriteSongListAdapter.notifyDataSetChanged()
            favoriteButton.setImageResource(R.drawable.ic_favorite_border)
        }
    }




    @SuppressLint("NotifyDataSetChanged")
    private fun insertDataToDatabase() {
        val songId = MusicService.currentSongData.song_id
        val songTitle = MusicService.currentSongData.song_title
        val songArtist = MusicService.currentSongData.song_artist
        val songData = MusicService.currentSongData.song_data
        val songDate = MusicService.currentSongData.song_date
        val songAlbumId = MusicService.currentSongData.song_album_id

        val favoriteSong = Favorite(songId, songTitle, songArtist ,songData, songDate, songAlbumId)
        MainActivity.mFavoritesViewModel.addToFavorites(favoriteSong)
        FavoritesFrag.myFavoriteSongListAdapter.notifyDataSetChanged()
    }

    fun shuffleButtonClicked() {
        if(shuffleState == 0) {
            shuffleState = 1
            buttonShuffle.setImageResource(R.drawable.ic_shuffle)
        } else if (shuffleState == 1) {
            shuffleState = 0
            buttonShuffle.setImageResource(com.goodman.musicplayer.R.drawable.shuffle_off)
        }
    }

    fun repeatButtonClicked() {
        if(repeatState == 0) {
            repeatState = 1
            buttonRepeat.setImageResource(R.drawable.ic_buu)
        } else if (repeatState == 1) {
            repeatState = 2
            buttonRepeat.setImageResource(R.drawable.repeat_once_white)
        } else if (repeatState == 2) {
            repeatState = 0
            buttonRepeat.setImageResource(R.drawable.ic_dont_repeat)
        }
    }

    @SuppressLint("NewApi")
    override fun playPauseBtnClicked() {
        if(MainActivity.musicService.playerState == 2) {
            //pause
            MainActivity.musicService.pauseSong()
            buttonPlay.setImageResource(R.drawable.ic_play)
            smallButtonPlay.setImageResource(R.drawable.ic_play)
        } else if (MainActivity.musicService.playerState == 1) {
            //resume
            MainActivity.musicService.resumeSong()
            buttonPlay.setImageResource(R.drawable.ic_pause)
            smallButtonPlay.setImageResource(R.drawable.ic_pause)
        }
    }


    override fun setFavoriteButton() {
        MainActivity.mFavoritesViewModel.readAllData.observe(this) { it ->
            val index = it.map { it.song_id }.indexOf( MusicService.currentSongData.song_id )
            if (index == -1) {
                favoriteState = 0
                favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            } else {
                favoriteState = 1
                favoriteButton.setImageResource(R.drawable.ic_favorite)
            }
        }
    }

    override fun nextBtnClicked() {
        if (shuffleState == 0) {
            if (!isSongOpenedFromAlbum && !isSongOpenedFromArtist && !isSongOpenedFromPreferences && !isSongOpenedFromFavorites) {
                if(PiecesFragment.songInfoList.size > 0) {
                    if(currentSong != -1) {
                        if(PiecesFragment.songInfoList.size - 1 == currentSong) {
                            currentSong = 0
                            try {
                                MainActivity.musicService.setSong(PiecesFragment.songInfoList[currentSong])
                                MainActivity.songModel = PiecesFragment.songInfoList[currentSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentSong++
                            try {
                                MainActivity.musicService.setSong(PiecesFragment.songInfoList[currentSong])
                                MainActivity.songModel = PiecesFragment.songInfoList[currentSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (isSongOpenedFromAlbum){
                if(AlbumsFrag.albumSongInfoList.size > 0) {
                    if(currentAlbumSong != -1) {
                        if(AlbumsFrag.albumSongInfoList.size - 1 == currentAlbumSong) {
                            currentAlbumSong = 0
                            try {
                                MainActivity.musicService.setSong(AlbumsFrag.albumSongInfoList[currentAlbumSong])
                                MainActivity.songModel = AlbumsFrag.albumSongInfoList[currentAlbumSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentAlbumSong++
                            try {
                                MainActivity.musicService.setSong(AlbumsFrag.albumSongInfoList[currentAlbumSong])
                                MainActivity.songModel = AlbumsFrag.albumSongInfoList[currentAlbumSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (isSongOpenedFromArtist){
                if(ArtistsFrag.artistSongInfoList.size > 0) {
                    if(currentArtistSong != -1) {
                        if(ArtistsFrag.artistSongInfoList.size - 1 == currentArtistSong) {
                            currentArtistSong = 0
                            try {
                                MainActivity.musicService.setSong(ArtistsFrag.artistSongInfoList[currentArtistSong])
                                MainActivity.songModel = ArtistsFrag.artistSongInfoList[currentArtistSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentArtistSong++
                            try {
                                MainActivity.musicService.setSong(ArtistsFrag.artistSongInfoList[currentArtistSong])
                                MainActivity.songModel = ArtistsFrag.artistSongInfoList[currentArtistSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            } else if (isSongOpenedFromPreferences) {
                isSongOpenedFromPreferences = false
                var lastSongIndex = PiecesFragment.songInfoList.map { it.song_id }.indexOf(
                    MainActivity.lastSong.song_id)
                lastSongIndex++
                currentSong = lastSongIndex
                try {
                    MainActivity.musicService.setSong(PiecesFragment.songInfoList[lastSongIndex])
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (isSongOpenedFromFavorites) {
                if(FavoritesFrag.favoriteSongInfoList.size > 0) {
                    if(currentFavoriteSong != -1) {
                        if(FavoritesFrag.favoriteSongInfoList.size - 1 == currentFavoriteSong) {
                            currentFavoriteSong = 0
                            try {
                                MainActivity.musicService.setSong(FavoritesFrag.favoriteSongInfoList[currentFavoriteSong])
                                MainActivity.songModel = FavoritesFrag.favoriteSongInfoList[currentFavoriteSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            currentFavoriteSong++
                            try {
                                MainActivity.musicService.setSong(FavoritesFrag.favoriteSongInfoList[currentFavoriteSong])
                                MainActivity.songModel = FavoritesFrag.favoriteSongInfoList[currentFavoriteSong]
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                }
            }
        } else if (shuffleState == 1) {
            if (!isSongOpenedFromAlbum && !isSongOpenedFromArtist) {
                if(PiecesFragment.songInfoList.size > 0) {
                    if(currentSong != -1) {
                        currentSong = getRandomNumber(PiecesFragment.songInfoList.size - 1)
                        try {
                            MainActivity.musicService.setSong(PiecesFragment.songInfoList[currentSong])
                            MainActivity.songModel = PiecesFragment.songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            } else if (isSongOpenedFromAlbum) {
                if(AlbumsFrag.albumSongInfoList.size > 0) {
                    if(currentAlbumSong != -1) {
                        currentAlbumSong = getRandomNumber(AlbumsFrag.albumSongInfoList.size - 1)
                        try {
                            MainActivity.musicService.setSong(AlbumsFrag.albumSongInfoList[currentAlbumSong])
                            MainActivity.songModel = AlbumsFrag.albumSongInfoList[currentAlbumSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            } else if (isSongOpenedFromArtist) {
                if(ArtistsFrag.artistSongInfoList.size > 0) {
                    if(currentArtistSong != -1) {
                        currentArtistSong = getRandomNumber(ArtistsFrag.artistSongInfoList.size - 1)
                        try {
                            MainActivity.musicService.setSong(ArtistsFrag.artistSongInfoList[currentArtistSong])
                            MainActivity.songModel = ArtistsFrag.artistSongInfoList[currentArtistSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this,"Song format not supported.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
        }
    }

    fun getRandomNumber(randomNmbrBtwn: Int): Int {
        return (0..randomNmbrBtwn).random()
    }

    private fun treeDotsThreadButton() {
        treeDotsButtonThread = Thread {
            run {
                treeDotsButton.setOnClickListener {
                    treeDotButtonClicked()
                }
            }
        }
        treeDotsButtonThread.start()
    }



    fun treeDotButtonClicked() {
        val popupMenu = PopupMenu(this, treeDotsButton)
        popupMenu.menuInflater.inflate(R.menu.treedotsmenu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                val myMenuItem = item!!.itemId
                if (myMenuItem == R.id.addFavorites) {
                    insertDataToDatabase()
                    return true
                } else if (myMenuItem == R.id.optionsAddToPlaylist) {

                    return true
                } else {
                    return false
                }
            }
        })
        popupMenu.show();
    }




    private lateinit var treeDotsButtonThread: Thread

    override fun prevBtnClicked() {
        if (shuffleState == 0) {
            if (!isSongOpenedFromAlbum && !isSongOpenedFromArtist && !isSongOpenedFromPreferences && !isSongOpenedFromFavorites) {
                if (currentSong != -1) {
                    if (currentSong == 0) {
                        currentSong = PiecesFragment.songInfoList.size - 1
                        try {
                            MainActivity.musicService.setSong(PiecesFragment.songInfoList[currentSong])
                            MainActivity.songModel = PiecesFragment.songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    } else {
                        currentSong--
                        try {
                            MainActivity.musicService.setSong(PiecesFragment.songInfoList[currentSong])
                            MainActivity.songModel = PiecesFragment.songInfoList[currentSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    }
                }
            } else if (isSongOpenedFromAlbum) {
                if (currentAlbumSong != -1) {
                    if (currentAlbumSong == 0) {
                        currentAlbumSong = AlbumsFrag.albumSongInfoList.size - 1
                        try {
                            MainActivity.musicService.setSong(AlbumsFrag.albumSongInfoList[currentAlbumSong])
                            MainActivity.songModel = AlbumsFrag.albumSongInfoList[currentAlbumSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    } else {
                        currentAlbumSong--
                        try {
                            MainActivity.musicService.setSong(AlbumsFrag.albumSongInfoList[currentAlbumSong])
                            MainActivity.songModel = AlbumsFrag.albumSongInfoList[currentAlbumSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    }
                }
            } else if (isSongOpenedFromArtist) {
                if (currentArtistSong != -1) {
                    if (currentArtistSong == 0) {
                        currentArtistSong = ArtistsFrag.artistSongInfoList.size - 1
                        try {
                            MainActivity.musicService.setSong(ArtistsFrag.artistSongInfoList[currentArtistSong])
                            MainActivity.songModel = ArtistsFrag.artistSongInfoList[currentArtistSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    } else {
                        currentArtistSong--
                        try {
                            MainActivity.musicService.setSong(ArtistsFrag.artistSongInfoList[currentArtistSong])
                            MainActivity.songModel = ArtistsFrag.artistSongInfoList[currentArtistSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    }
                }
            } else if (isSongOpenedFromPreferences) {
                isSongOpenedFromPreferences = false
                var lastSongIndex = PiecesFragment.songInfoList.map { it.song_id }.indexOf(
                    MainActivity.lastSong.song_id)
                lastSongIndex--
                currentSong = lastSongIndex
                try {
                    MainActivity.musicService.setSong(PiecesFragment.songInfoList[lastSongIndex])
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT).show()
                    return
                }
            } else if (isSongOpenedFromFavorites) {
                if (currentFavoriteSong != -1) {
                    if (currentFavoriteSong == 0) {
                        currentFavoriteSong = FavoritesFrag.favoriteSongInfoList.size - 1
                        try {
                            MainActivity.musicService.setSong(FavoritesFrag.favoriteSongInfoList[currentFavoriteSong])
                            MainActivity.songModel = FavoritesFrag.favoriteSongInfoList[currentFavoriteSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    } else {
                        currentFavoriteSong--
                        try {
                            MainActivity.musicService.setSong(FavoritesFrag.favoriteSongInfoList[currentFavoriteSong])
                            MainActivity.songModel = FavoritesFrag.favoriteSongInfoList[currentFavoriteSong]
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this, "Song format not supported.", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun longSelectDestroyer() {

        HeaderAdapter.isPiecesSelectionEnable = false
        BottomSheetBehavior.from(bottomLayout).apply {
            val inner = findViewById<ConstraintLayout>(R.id.bottomLayout)
            inner.viewTreeObserver.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    inner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val hidden = inner.getChildAt(4)
                    peekHeight = hidden.bottom + hidden.marginTop
                }
            })
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        navigation.visibility = View.GONE
        val tabStrip = tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = true
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = true
        }
        myViewPager2.isUserInputEnabled = true

        title = "Music Player"
        HeaderAdapter.selectedItemList.clear()
        if (once2 == 0) {
            PiecesFragment.myMusicListViewAdapter.notifyDataSetChanged()
            once2++
            once = 0
        }
    }

    private lateinit var playThread: Thread
    private lateinit var prevThread: Thread
    private lateinit var nextThread: Thread
    private lateinit var repeatButtonThread: Thread
    private lateinit var shuffleButtonThread: Thread
    private lateinit var favoriteButtonThread: Thread
    private lateinit var settingsClickThread: Thread

}
