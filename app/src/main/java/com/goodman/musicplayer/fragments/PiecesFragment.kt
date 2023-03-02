package com.goodman.musicplayer.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goodman.musicplayer.*
import com.goodman.musicplayer.HeaderAdapter.Companion.data2
import com.goodman.musicplayer.HeaderAdapter.Companion.isPiecesSelectionEnable
import com.goodman.musicplayer.HeaderAdapter.Companion.selectedItemList
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.activities.MainActivity.Companion.mFavoritesViewModel
import com.goodman.musicplayer.activities.MainActivity.Companion.mPlaylistDatabaseViewModel
import com.goodman.musicplayer.activities.MainActivity.Companion.musicService
import com.goodman.musicplayer.activities.MainActivity.Companion.songModel
import com.goodman.musicplayer.adapters.AddToRecylerAdapter
import com.goodman.musicplayer.adapters.AddToRecylerAdapter.Companion.addToList
import com.goodman.musicplayer.adapters.addToSheetInterface
import com.goodman.musicplayer.data.*
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.models.TrackListModel
import com.goodman.musicplayer.nterface.onSongSelect
import com.goodman.musicplayer.services.MusicService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_pieces.*
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.text.Collator
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [PiecesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class PiecesFragment : Fragment(), MainActivityInterface, onSongSelect, addToSheetInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var addSong: Favorite


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_pieces, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstRun()
        val dividerItemDecoration = DividerItemDecoration(
            requireActivity().addToRecylerView.context,
            LinearLayoutManager(requireActivity().applicationContext).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(R.drawable.recylerview_divider)!!)
        requireActivity().addToRecylerView.addItemDecoration(dividerItemDecoration)

    }


    @SuppressLint("StaticFieldLeak")
    companion object {
        @SuppressLint("DefaultLocale")
        @JvmStatic
        var songInfoList : ArrayList<SongModel> = ArrayList()
        var songInfoList2 : ArrayList<SongModel> = ArrayList()
        val noCollator = Collator.getInstance(Locale("tr", "TR"))
        @SuppressLint("StaticFieldLeak")

        lateinit var myMusicListViewAdapter : HeaderAdapter
        @SuppressLint("StaticFieldLeak")
        lateinit var myPlayListViewAdapter : AddToRecylerAdapter
        var addToInfoList : ArrayList<TrackListModel> = ArrayList()
        fun newInstance(param1: String, param2: String) =
            PiecesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(),"Permission Granted!", Toast.LENGTH_SHORT).show()
                    firstRun()
                }
            } else {
                Toast.makeText(requireContext(),"No Permission Granted!", Toast.LENGTH_SHORT).show()
            }
            return
        }
    }

    private fun giveMeContentResolverPlease() : ContentResolver {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            val applicationContext = requireActivity().contentResolver
            return applicationContext
        } else
            return throw java.lang.RuntimeException("Hata!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    }

    override fun recyclerViewListClickHandler(v: View?, position: Int) {
    }




    @SuppressLint("DefaultLocale", "SetTextI18n")


    override fun getAlbumCover(albumid: Long) : Bitmap? {

        if (albumCovers.containsKey(albumid))
        {
            return albumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {
                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(1080, 1920), null)
                albumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }
        return returnvar;
    }

    override fun onAddToClicked(position: Int) {
        BottomSheetBehavior.from(requireActivity().addToBottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

        addSong = Favorite(songInfoList[position].song_id, songInfoList[position].song_title,songInfoList[position].song_artist,songInfoList[position].song_data,songInfoList[position].song_date,songInfoList[position].song_album_id)
        //-------------------------------------------------------------------------------

        myPlayListViewAdapter = AddToRecylerAdapter( this)
        addToInfoList = addToList
        requireActivity().addToRecylerView.adapter = myPlayListViewAdapter;
        requireActivity().addToRecylerView.layoutManager = LinearLayoutManager(context)
        requireActivity().addToRecylerView.setHasFixedSize(true)
        requireActivity().addToRecylerView.setClickable(true)

        //-------------------------------------------------------------------------------

        val oldListSongs: ArrayList<SongEntity> = ArrayList()

        mPlaylistDatabaseViewModel.readAllData.observe(viewLifecycleOwner) {
            addToList.clear()
            it.map { playlistWithSongs ->
                oldListSongs.clear()
                playlistWithSongs.songs.map { Song ->
                    oldListSongs.add(SongEntity(Song.songId, Song.songTitle, Song.songArtist, Song.songData, Song.songDate, Song.songAlbumID))
                }
                addToList.add(TrackListModel(playlistWithSongs.playlist!!.playlistId!!, playlistWithSongs.playlist!!.playlistTitle!!, oldListSongs.size.toString()))
            }
            myPlayListViewAdapter.notifyDataSetChanged()
        }
    }


    override fun onLongClickEnabled() {
        if (isPiecesSelectionEnable) {
            if (once == 0) {
                myMusicListViewAdapter.notifyDataSetChanged()
                once++
                once2 = 0
            }
            BottomSheetBehavior.from(requireActivity().bottomLayout).apply {
                peekHeight = 0
                this.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            requireActivity().navigation.visibility = View.VISIBLE

            val tabStrip = requireActivity().tabLayout.getChildAt(0) as LinearLayout
            tabStrip.isEnabled = false
            for (i in 0 until tabStrip.childCount) {
                tabStrip.getChildAt(i).isClickable = false
            }
            requireActivity().myViewPager2.isUserInputEnabled = false

            requireActivity().title = (selectedItemList.size).toString() + " Selected"


            requireActivity().navigation.setOnItemSelectedListener { item ->
                calledFromNavigationBtn = 1
                when (item.itemId) {
                    R.id.action_navigation_play -> {
                        Log.d("TAG3434", "Navigation Play")
                        true
                    }
                    R.id.action_navigation_AddTo  -> {
                        BottomSheetBehavior.from(requireActivity().addToBottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                        //-------------------------------------------------------------------------------

                        myPlayListViewAdapter = AddToRecylerAdapter( this)
                        addToInfoList = addToList
                        requireActivity().addToRecylerView.adapter = myPlayListViewAdapter;
                        requireActivity().addToRecylerView.layoutManager = LinearLayoutManager(context)
                        requireActivity().addToRecylerView.setHasFixedSize(true)
                        requireActivity().addToRecylerView.setClickable(true)

                        //-------------------------------------------------------------------------------

                        var oldListSongs: ArrayList<SongEntity> = ArrayList()

                        mPlaylistDatabaseViewModel.readAllData.observe(viewLifecycleOwner) {
                            addToList.clear()
                            it.map { playlistWithSongs ->
                                oldListSongs.clear()
                                playlistWithSongs.songs.map { Song ->
                                    oldListSongs.add(SongEntity(Song.songId, Song.songTitle, Song.songArtist, Song.songData, Song.songDate, Song.songAlbumID))
                                }
                                addToList.add(TrackListModel(playlistWithSongs.playlist!!.playlistId!!, playlistWithSongs.playlist!!.playlistTitle!!, oldListSongs.size.toString()))
                            }
                            myPlayListViewAdapter.notifyDataSetChanged()
                        }
                        this.longSelectDestroyer()
                        true
                    }
                    R.id.action_navigation_share  -> {

                        true
                    }
                    R.id.action_navigation_delete  -> {

                        true
                    }
                    else -> {false}
                }
            }

        } else {
            if (once2 == 0) {
                myMusicListViewAdapter.notifyDataSetChanged()
                once2++
                once = 0
            }
            BottomSheetBehavior.from(requireActivity().bottomLayout).apply {
                val inner = requireActivity().findViewById<ConstraintLayout>(R.id.bottomLayout)
                inner.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        inner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val hidden = inner.getChildAt(4)
                        peekHeight = hidden.bottom + hidden.marginTop
                    }
                })
                this.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            requireActivity().navigation.apply {
                this.visibility = View.GONE
            }
            val tabStrip = requireActivity().tabLayout.getChildAt(0) as LinearLayout
            tabStrip.isEnabled = true
            for (i in 0 until tabStrip.childCount) {
                tabStrip.getChildAt(i).isClickable = true
            }
            requireActivity().myViewPager2.isUserInputEnabled = true

            requireActivity().title = "Music Player"
        }
    }

    override fun longSelectDestroyer() {

        isPiecesSelectionEnable = false
        BottomSheetBehavior.from(requireActivity().bottomLayout).apply {
            val inner = requireActivity().findViewById<ConstraintLayout>(R.id.bottomLayout)
            inner.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    inner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val hidden = inner.getChildAt(4)
                    peekHeight = hidden.bottom + hidden.marginTop
                }
            })
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        requireActivity().navigation.visibility = View.GONE
        val tabStrip = requireActivity().tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = true
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = true
        }
        requireActivity().myViewPager2.isUserInputEnabled = true

        requireActivity().title = "Music Player"
        myMusicListViewAdapter.notifyDataSetChanged()
    }

    override fun setNowPlayingText(holder: HeaderAdapter.VHItem, currentItem: SongModel) {
        if (currentItem.song_id == MusicService.currentSongData.song_id) {
            holder.listSongName.setTextColor(Color.parseColor("#FF03DAC5"))
            holder.artistName.setTextColor(Color.parseColor("#FF03DAC5"))
        } else {
            holder.listSongName.setTextColor(Color.parseColor("#B0B0B0"))
            holder.artistName.setTextColor(Color.parseColor("#B0B0B0"))
        }
    }

    override fun addToViewListClickHandler(v: View?, position: Int) {
        Log.d("TAG3434", addToInfoList[position].trackListTitle)
        var oldListSongs: ArrayList<SongEntity> = ArrayList()

        mPlaylistDatabaseViewModel.readAllData.observe(viewLifecycleOwner) {
            it.map {
                if (addToInfoList[position].trackListId == it.playlist.playlistId) {
                    oldListSongs.clear()
                    it.songs.map { t ->
                        oldListSongs.add(SongEntity(t.songId, t.songTitle,t.songArtist,t.songData,t.songDate,t.songAlbumID))
                    }
                }
            }
        }

        if (calledFromListBtn == 1) {
            oldListSongs.add(SongEntity(addSong.song_id,addSong.song_title,addSong.song_artist,addSong.song_data,addSong.song_date,addSong.song_album_id))
        } else if (calledFromNavigationBtn == 1) {
            selectedItemList.map {
                oldListSongs.add(SongEntity(it.song_id, it.song_title,it.song_artist, it.song_data, it.song_date, it.song_album_id))
            }
        }



        oldListSongs.map {

                    mPlaylistDatabaseViewModel.insertSongEntity(SongEntity(it.songId,it.songTitle, it.songArtist,it.songData,it.songDate,it.songAlbumID))
                    mPlaylistDatabaseViewModel.insertPlaylistWithSongs(PlaylistSongCrossEntity(addToInfoList[position].trackListId, it.songId))

        }

//        dao.insert(Playlist(addToInfoList[position].trackListId, addToInfoList[position].trackListTitle), oldListSongs)
        selectedItemList.clear()

        BottomSheetBehavior.from(requireActivity().addToBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
        Toast.makeText(requireContext(),"Succesfully added.", Toast.LENGTH_SHORT).show()
        calledFromListBtn = 0
        calledFromNavigationBtn = 0
    }


    override fun addToFavorite() {
        insertDataToFavorite()
        BottomSheetBehavior.from(requireActivity().addToBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
        Toast.makeText(requireContext(),"Succesfully added to favorites.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun insertDataToFavorite() {
        mFavoritesViewModel.addToFavorites(addSong)
        FavoritesFrag.myFavoriteSongListAdapter.notifyDataSetChanged()
    }

    fun getSongs() {
        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = giveMeContentResolverPlease().query(songUri,null,null,null,null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            songInfoList.clear()
            songInfoList2.clear()
            do {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentData = songCursor.getString(songData)
                val currentDate = songCursor.getLong(songDate)
                val currentAlbumId = songCursor.getString(songAlbumId)

                var data0 = SongModel(currentId,currentTitle,currentArtist!!,currentData,currentDate, currentAlbumId.toLong(), false)
                songInfoList.add(data0)
                songInfoList2.add(data0)

                noCollator.strength = Collator.SECONDARY
                Collections.sort(songInfoList,
                    Comparator<SongModel> { obj1, obj2 -> noCollator.compare(obj1.song_title, obj2.song_title) })
                Collections.sort(songInfoList2,
                    Comparator<SongModel> { obj1, obj2 -> noCollator.compare(obj1.song_title, obj2.song_title) })
                songPaths.add(currentData)

            } while (songCursor.moveToNext())
            songCursor.close()
        }


    }

    override fun onSelect(song: SongModel, position: Int, holder: HeaderAdapter.VHItem, currentItem: SongModel) {
        try {
            isSongOpenedFromAlbum = false
            isSongOpenedFromArtist = false
            isSongOpenedFromPreferences = false
            isSongOpenedFromFavorites = false
            musicService.setSong(song)
            songModel = song
            myMusicListViewAdapter.notifyItemChanged(position + 1)
        } catch (e: java.lang.Exception) {
            Toast.makeText(context,"Song format not supported.", Toast.LENGTH_SHORT).show()
            musicService.setSong(songInfoList[currentSong + 1])
            songModel = songInfoList[currentSong + 1]
            return
        }
    }



    private fun startEngine()
    {
        myMusicListViewAdapter = HeaderAdapter( this, this)
        songInfoList = data2
        musicListView.adapter = myMusicListViewAdapter;
        musicListView.layoutManager = LinearLayoutManager(context)
        musicListView.setHasFixedSize(true)
        musicListView.setClickable(true)
        val dividerItemDecoration = DividerItemDecoration(
            musicListView.context,
            LinearLayoutManager(requireActivity().applicationContext).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(R.drawable.recylerview_divider)!!)
        musicListView.addItemDecoration(dividerItemDecoration)
    }





    @SuppressLint("DefaultLocale")
    private fun firstRun()
    {
        startEngine();
        getSongs()
    }


    private var songPaths: ArrayList<String> = ArrayList<String>()
    private val albumCovers = mutableMapOf<Long, Bitmap>()


}