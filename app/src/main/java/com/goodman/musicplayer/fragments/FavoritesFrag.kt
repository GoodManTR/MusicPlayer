package com.goodman.musicplayer.fragments

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.*
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.activities.MainActivity.Companion.mFavoritesViewModel
import com.goodman.musicplayer.activities.MainActivity.Companion.musicService
import com.goodman.musicplayer.adapters.FavoriteFragmentInterface
import com.goodman.musicplayer.adapters.FavoriteSongsRecylerAdapter
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.onFavoriteSongSelect
import com.goodman.musicplayer.services.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_favorites.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFrag : Fragment(), FavoriteFragmentInterface, onFavoriteSongSelect {
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
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstRun()
    }

    @SuppressLint("StaticFieldLeak")
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var myFavoriteSongListAdapter : FavoriteSongsRecylerAdapter
        var favoriteSongInfoList : ArrayList<SongModel> = ArrayList()
        var favoriteSongInfoList2 : ArrayList<SongModel> = ArrayList()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoritesFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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
        TODO("Not yet implemented")
    }

    override fun getAlbumCover(albumid: Long): Bitmap? {
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

    private fun startEngine()
    {
        myFavoriteSongListAdapter = FavoriteSongsRecylerAdapter( this, this)
        favoriteSongInfoList = FavoriteSongsRecylerAdapter.favoriteSongData
        favoritesRecylerView.adapter = myFavoriteSongListAdapter;
        favoritesRecylerView.layoutManager = LinearLayoutManager(context)
        favoritesRecylerView.setHasFixedSize(true)
        favoritesRecylerView.setClickable(true)
        val dividerItemDecoration = DividerItemDecoration(
            favoritesRecylerView.context,
            LinearLayoutManager(requireActivity().applicationContext).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(com.goodman.musicplayer.R.drawable.recylerview_divider)!!)
        favoritesRecylerView.addItemDecoration(dividerItemDecoration)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getSongs() {
        mFavoritesViewModel.readAllData.observe(viewLifecycleOwner) { it ->
            Log.d("TAG3434", "database observer")
            favoriteSongInfoList.clear()
            it.map {
                val lastSongIndex = PiecesFragment.songInfoList.map { song -> song.song_id }.indexOf(it.song_id)
                if (lastSongIndex != -1) {
                    favoriteSongInfoList.add(SongModel(it.song_id,it.song_title,it.song_artist,it.song_data,it.song_date,it.song_album_id, false))
                }
            }
            myFavoriteSongListAdapter.notifyDataSetChanged()
            favoriteSongInfoList2 = favoriteSongInfoList
            if (favoriteSongInfoList.isEmpty()) {
                favoritesRecylerView.visibility = View.GONE
                noFavoriteInfo.visibility = View.VISIBLE
            } else {
                noFavoriteInfo.visibility = View.GONE
                favoritesRecylerView.visibility = View.VISIBLE
            }
        }
    }

    private fun firstRun()
    {
        startEngine();
        getSongs()
    }

    override fun onFavoriteSelect(song: SongModel) {
        try {
            isSongOpenedFromFavorites = true
            isSongOpenedFromAlbum = false
            isSongOpenedFromArtist = false
            isSongOpenedFromPreferences = false
            MainActivity.musicService.setSong(song)
            MainActivity.songModel = song
        } catch (e: java.lang.Exception) {
            Toast.makeText(context,"Song format not supported.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private val albumCovers = mutableMapOf<Long, Bitmap>()
}