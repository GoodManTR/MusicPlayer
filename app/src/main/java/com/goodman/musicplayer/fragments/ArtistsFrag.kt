package com.goodman.musicplayer

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.adapters.ArtistSongActitivityInterface
import com.goodman.musicplayer.adapters.ArtistsInterface
import com.goodman.musicplayer.adapters.ArtistsRecylerAdapter
import com.goodman.musicplayer.adapters.ArtistsSongRecylerAdapter

import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.ArtistsModel
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.onArtistSongSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.artists_songs.*
import kotlinx.android.synthetic.main.fragment_artists.*
import kotlinx.android.synthetic.main.fragment_pieces.*
import java.text.Collator
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistsFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArtistsFrag : Fragment(), ArtistsInterface, ArtistSongActitivityInterface,
    onArtistSongSelect {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.goodman.musicplayer.R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstRun()

        val dividerItemDecoration = DividerItemDecoration(
            requireActivity().everythingRecylerView.context,
            LinearLayoutManager(context).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(com.goodman.musicplayer.R.drawable.recylerview_divider)!!)
        requireActivity().everythingRecylerView.addItemDecoration(dividerItemDecoration)
    }

    private fun giveMeContentResolverPlease() : ContentResolver {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            val applicationContext = requireActivity().contentResolver
            return applicationContext
        } else
            return throw java.lang.RuntimeException("Hata!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun recyclerViewListClickHandler(v: View?, position: Int) {
        myArtistID = artistInfoList[position -1].artist_id

        requireActivity().everythingToolbar.title = artistInfoList[position -1].artist
        requireActivity().everythingBottomSheet.visibility = View.VISIBLE

        startArtistSongsEngine()
        getArtistSongs()

        BottomSheetBehavior.from(requireActivity().everythingBottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }


    override fun getAlbumCover(albumid: Long) : Bitmap? {

        if (albumCovers2.containsKey(albumid))
        {
            return albumCovers2.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {
                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(1080, 1920), null)
                albumCovers2.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }
        }
        return returnvar;
    }


    fun getArtists() {
        val songUri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val songCursor = giveMeContentResolverPlease().query(songUri,null,null,null,null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val artistId = songCursor.getColumnIndex(MediaStore.Audio.Artists._ID)
            val Artist = songCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
            val ArtistTrackCount = songCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val ArtistAlbumCount = songCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            //val ArtistAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ARTIST_ID)

            artistInfoList.clear()
            artistInfoList2.clear()
            do {
                val currentId = songCursor.getLong(artistId)
                val currentArtist = songCursor.getString(Artist)
                val currentSongCount = songCursor.getString(ArtistTrackCount)
                val currentAlbumCount = songCursor.getString(ArtistAlbumCount)
                //val currentAlbumId = songCursor.getString(ArtistAlbumId)

                var data0 = ArtistsModel(currentId ,currentArtist,currentSongCount,currentAlbumCount /*currentAlbumId.toLong()*/)
                artistInfoList.add(data0)
                artistInfoList2.add(data0)


                PiecesFragment.noCollator.strength = Collator.SECONDARY
                Collections.sort(
                    artistInfoList,
                    Comparator<ArtistsModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.artist, obj2.artist) })
                Collections.sort(
                    artistInfoList2,
                    Comparator<ArtistsModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.artist, obj2.artist) })

            } while (songCursor.moveToNext())
            songCursor.close()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun startEngine()
    {
        myArtistsRecylerAdapter = ArtistsRecylerAdapter(this)
        artistInfoList = ArtistsRecylerAdapter.artistsData
        artistsRecylerView.adapter = myArtistsRecylerAdapter;
        artistsRecylerView.layoutManager = LinearLayoutManager(context)
        artistsRecylerView.setHasFixedSize(true)
        artistsRecylerView.setClickable(true)
        val dividerItemDecoration = DividerItemDecoration(
            artistsRecylerView.context,
            LinearLayoutManager(requireActivity().applicationContext).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(com.goodman.musicplayer.R.drawable.recylerview_divider)!!)
        artistsRecylerView.addItemDecoration(dividerItemDecoration)
    }




    @SuppressLint("DefaultLocale")
    private fun firstRun()
    {
        startEngine();
        getArtists()
    }

    companion object {
        var artistInfoList : ArrayList<ArtistsModel> = ArrayList()
        var artistInfoList2 : ArrayList<ArtistsModel> = ArrayList()
        var myArtistID: Long = 1
        var artistSongInfoList : ArrayList<SongModel> = ArrayList<SongModel>()
        @SuppressLint("StaticFieldLeak")
        lateinit var myArtistsRecylerAdapter : ArtistsRecylerAdapter
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ArtistsFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }
    private val albumCovers2 = mutableMapOf<Long, Bitmap>()
    override fun artistsRecyclerViewListClickHandler(v: View?, position: Int) {
        TODO("Not yet implemented")
    }

    private fun getArtistSongs()
    {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )
        var selection = "is_music != 0"

        if (myArtistID > 0) {
            selection = "$selection and artist_id = $myArtistID"
        }

        var songCursor = giveMeContentResolverPlease().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,null);



        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)


            artistSongInfoList.clear()
            do {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentData = songCursor.getString(songData)
                val currentDate = songCursor.getLong(songDate)
                val currentAlbumId = songCursor.getString(songAlbumId)

                var data0 = SongModel(currentId,currentTitle,currentArtist,currentData,currentDate, currentAlbumId.toLong(), false)
                artistSongInfoList.add(data0)

                if (!(AlbumsFrag.albumSongInfoList.size <= 2)){
                    PiecesFragment.noCollator.strength = Collator.SECONDARY
                    Collections.sort(
                        artistSongInfoList,
                        Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
                }
            } while (songCursor.moveToNext())
        }
    }

    override fun getArtistsAlbumCover(albumid: Long): Bitmap? {
        if (artistAlbumCovers.containsKey(albumid))
        {
            return artistAlbumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {

                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(100, 100), null)
                artistAlbumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }

        return returnvar;
    }

    private fun startArtistSongsEngine()
    {
        myArtistSongAdapter = ArtistsSongRecylerAdapter( this,this)
        artistSongInfoList = ArtistsSongRecylerAdapter.artistSongAdapterData
        requireActivity().everythingRecylerView.adapter = myArtistSongAdapter;
        requireActivity().everythingRecylerView.layoutManager = LinearLayoutManager(context)
        requireActivity().everythingRecylerView.setHasFixedSize(true)
        requireActivity().everythingRecylerView.setClickable(true)

    }

    override fun onArtistSelect(song: SongModel) {
        try {
            isSongOpenedFromArtist = true
            isSongOpenedFromAlbum = false
            isSongOpenedFromPreferences = false
            isSongOpenedFromFavorites = false
            MainActivity.musicService.setSong(song)
            MainActivity.songModel = song

//            PiecesFragment.tinyDB.putString("song_title", MainActivity.musicService.getSongTitle())
//            PiecesFragment.tinyDB.putString("song_artist", MainActivity.musicService.getSongArtist())
//            PiecesFragment.tinyDB.putLong("song_id", MainActivity.musicService.songs.song_id)
//            PiecesFragment.tinyDB.putString("song_data", MainActivity.musicService.songs.song_data)
//            PiecesFragment.tinyDB.putLong("song_date", MainActivity.musicService.songs.song_date)
//            PiecesFragment.tinyDB.putLong("song_album_id", MainActivity.musicService.songs.song_album_id)
//            PiecesFragment.tinyDB.putBoolean("isLastSongOepenedFromAlbum", true)
//            PiecesFragment.tinyDB.putInt("currentAlbumSong", currentAlbumSong)
        } catch (e: java.lang.Exception) {
            Toast.makeText(context,"Song format not supported.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    val artistAlbumCovers = mutableMapOf<Long, Bitmap>()
    lateinit var myArtistSongAdapter : ArtistsSongRecylerAdapter

}