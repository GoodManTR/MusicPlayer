package com.goodman.musicplayer.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.*
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.adapters.SearchAlbumsAdapter
import com.goodman.musicplayer.adapters.SearchArtistsAdapter
import com.goodman.musicplayer.adapters.SearchSongsInterface
import com.goodman.musicplayer.adapters.SearchTracksAdapter
import com.goodman.musicplayer.models.SongModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search_songs.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchSongsFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchSongsFrag : Fragment(), SearchSongsInterface {
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
        return inflater.inflate(R.layout.fragment_search_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startEngine()

        val dividerItemDecoration = DividerItemDecoration(
            searchTracksRecylerView.context,
            LinearLayoutManager(context).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(R.drawable.recylerview_divider)!!)
        searchTracksRecylerView.addItemDecoration(dividerItemDecoration)

        requireActivity().searchEditText.addTextChangedListener {
            getSearchSongs()
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var searchSongAdapter: SearchTracksAdapter
        var searchSongInfoList: ArrayList<SongModel> = ArrayList()
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchSongsFrag().apply {
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


    private fun startEngine()
    {
        searchSongAdapter = SearchTracksAdapter( this)
        searchSongInfoList = SearchTracksAdapter.searchSongsData
        searchTracksRecylerView.adapter = searchSongAdapter;
        searchTracksRecylerView.layoutManager = LinearLayoutManager(context)
        searchTracksRecylerView.setHasFixedSize(true)
        searchTracksRecylerView.setClickable(true)

    }

    private fun getSearchSongs() {
        searchTracksRecylerView.visibility = View.VISIBLE
        noSongResult.visibility = View.INVISIBLE
        val userInput = requireActivity().searchEditText.text.toString()

        val myFiles = java.util.ArrayList<SongModel>()
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
        searchSongAdapter.updateList(myFiles)
        searchSongInfoList = myFiles
        if (userInput == "") {
            myFiles.clear()
            searchSongAdapter.updateList(myFiles)
            searchSongInfoList = myFiles

        }
        if (userInput == "" || searchSongInfoList.isEmpty()) {
            searchTracksRecylerView.visibility = View.INVISIBLE
            noSongResult.visibility = View.VISIBLE
        }
    }

    override fun searchSongListClickHandler(v: View?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun searchSongListAlbumCover(albumid: Long): Bitmap? {
        if (searchAlbumCovers.containsKey(albumid))
        {
            return searchAlbumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {
                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(1080, 1920), null)
                searchAlbumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }
        return returnvar;
    }

    override fun onSearchSongSelect(song: SongModel) {
        try {
            isSongOpenedFromAlbum = false
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

    private val searchAlbumCovers = mutableMapOf<Long, Bitmap>()
}