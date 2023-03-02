package com.goodman.musicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.AlbumsRecylerAdapter.Companion.albumData
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.onAlbumSongSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.album_songs.*
import kotlinx.android.synthetic.main.fragment_albums.*
import kotlinx.android.synthetic.main.fragment_pieces.*
import java.text.Collator
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

data class MyAlbumsDataClass( var songName: String,
                        var songArtist: String,
                        var albumid: Long)

class AlbumsFrag : Fragment(), AlbumsFragmentInterface, AlbumSongActitivityInterface,
    onAlbumSongSelect {
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
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstRun()
        Log.d("TAG3434", "VİEW CREATED")
        val dividerItemDecoration = DividerItemDecoration(
            requireActivity().everythingRecylerView.context,
            LinearLayoutManager(context).orientation
        )
        dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.recylerview_divider)!!)
        requireActivity().everythingRecylerView.addItemDecoration(dividerItemDecoration)


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumsFrag.
         */
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
         lateinit var myAlbumAdapter : AlbumsRecylerAdapter
         var albumInfoList : ArrayList<MyAlbumsDataClass> = ArrayList<MyAlbumsDataClass>()
         var albumInfoList2 : ArrayList<MyAlbumsDataClass> = ArrayList<MyAlbumsDataClass>()
        var myAlbumID: Long = 1
        @SuppressLint("StaticFieldLeak")
        lateinit var myAlbumSongAdapter : AlbumSongRecylerAdapter
        var albumSongInfoList : ArrayList<SongModel> = ArrayList<SongModel>()
        val albumCovers = mutableMapOf<Long, Bitmap>()
        fun newInstance(param1: String, param2: String) =
            AlbumsFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    private fun startAlbumEngine()
    {
        myAlbumSongAdapter = AlbumSongRecylerAdapter( this,this)
        albumSongInfoList = AlbumSongRecylerAdapter.albumSongAdapterData
        requireActivity().everythingRecylerView.adapter = myAlbumSongAdapter;
        requireActivity().everythingRecylerView.layoutManager = LinearLayoutManager(context)
        requireActivity().everythingRecylerView.setHasFixedSize(true)
        requireActivity().everythingRecylerView.setClickable(true)
    }



    private fun getTheAlbumsSongs()
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

        if (myAlbumID > 0) {
            selection = "$selection and album_id = $myAlbumID"
        }

        var songCursor = giveMeContentResolverPlease().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,null);


        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            albumSongInfoList.clear()
            do {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentData = songCursor.getString(songData)
                val currentDate = songCursor.getLong(songDate)
                val currentAlbumId = songCursor.getString(songAlbumId)

                var data0 = SongModel(currentId,currentTitle,currentArtist,currentData,currentDate, currentAlbumId.toLong(), false)
                albumSongInfoList.add(data0)

                if (!(albumSongInfoList.size <= 2)){
                    PiecesFragment.noCollator.strength = Collator.SECONDARY
                    Collections.sort(
                        albumSongInfoList,
                        Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
                }
            } while (songCursor.moveToNext())
        }
    }

    override fun albumSongRecylerViewListClickHandler(v: View?, position: Int) {
        TODO("Not yet implemented")
    }



    override fun getAlbumSongCover(albumid: Long): Bitmap? {
        if (albumCovers.containsKey(albumid))
        {
            return albumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {

                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(100, 100), null)
                albumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }

        return returnvar;
    }

    override fun onAlbumSelect(song: SongModel) {
        try {
            isSongOpenedFromAlbum = true
            isSongOpenedFromArtist = false
            isSongOpenedFromPreferences = false
            isSongOpenedFromFavorites = false
            MainActivity.musicService.setSong(song)
            MainActivity.songModel = song
        } catch (e: java.lang.Exception) {
            Toast.makeText(context,"Song format not supported.", Toast.LENGTH_SHORT).show()
            return
        }
    }


    override fun recyclerViewListClickHandler(v: View?, position: Int) {
        myAlbumID = albumInfoList[position].albumid

        requireActivity().everythingToolbar.title = albumInfoList[position].songName
        requireActivity().everythingBottomSheet.visibility = View.VISIBLE

        startAlbumEngine()
        getTheAlbumsSongs()

        BottomSheetBehavior.from(requireActivity().everythingBottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

    }



    override fun getAlbumCover(albumid: Long) : Bitmap? {

        if (albumCovers.containsKey(albumid))
        {
            return albumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {

                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(100, 100), null)
                albumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }

        return returnvar;
    }


    private fun giveMeContentResolverPlease() : ContentResolver {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            val applicationContext = requireActivity().contentResolver
            return applicationContext
        } else
            return throw java.lang.RuntimeException("Hata!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    }

    @SuppressLint("Range")
    private fun turnOnIgnitionn()
    {
        val songCursor = giveMeContentResolverPlease().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,null,null,null,null);

        if (songCursor != null && songCursor.moveToFirst()) {
            val title = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
            val art = songCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
            val albumId = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)


            albumInfoList.clear()
            albumInfoList2.clear()
            do {
                val currentTitle = songCursor.getString(title)
                val currentArtist = songCursor.getString(art)
                val currentid = songCursor.getString(albumId)

                val data0 = MyAlbumsDataClass(currentTitle, currentArtist,  currentid.toLong())
                albumInfoList.add(data0)
                albumInfoList2.add(data0)

//                PiecesFragment.noCollator.strength = Collator.SECONDARY
                Collections.sort(
                    albumInfoList,
                    Comparator<MyAlbumsDataClass> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.songName, obj2.songName) })
                Collections.sort(
                    albumInfoList2,
                    Comparator<MyAlbumsDataClass> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.songName, obj2.songName) })
            } while (songCursor.moveToNext())
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n", "UseCompatLoadingForDrawables")
    private fun startEngine()
    {
        myAlbumAdapter = AlbumsRecylerAdapter( this)
        albumInfoList = albumData
        albumsRecylerView.adapter = myAlbumAdapter;
        albumsRecylerView.layoutManager = GridLayoutManager(context, 2)
        albumsRecylerView.setHasFixedSize(true)
        albumsRecylerView.setClickable(true)
    }

    @SuppressLint("DefaultLocale")
    private fun firstRun()
    {
        startEngine();
        turnOnIgnitionn();
    }


    private val albumCovers = mutableMapOf<Long, Bitmap>()



}