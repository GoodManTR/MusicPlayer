package com.goodman.musicplayer.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.AlbumsFrag
import com.goodman.musicplayer.MyAlbumsDataClass
import com.goodman.musicplayer.R
import com.goodman.musicplayer.adapters.SearchAlbumsAdapter
import com.goodman.musicplayer.adapters.SearchAlbumsInterface
import com.goodman.musicplayer.adapters.SearchArtistsAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search_albums.*
import kotlinx.android.synthetic.main.fragment_search_artists.*
import kotlinx.android.synthetic.main.fragment_search_songs.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchAlbumsFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchAlbumsFrag : Fragment(), SearchAlbumsInterface {
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
        return inflater.inflate(R.layout.fragment_search_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startEngine()

        val dividerItemDecoration2 = DividerItemDecoration(
            searchAlbumsRecylerView.context,
            LinearLayoutManager(context).orientation
        )
        dividerItemDecoration2.setDrawable(requireContext().getDrawable(R.drawable.recylerview_divider)!!)
        searchAlbumsRecylerView.addItemDecoration(dividerItemDecoration2)

        requireActivity().searchEditText.addTextChangedListener {
            getSearchAlbums()
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var searchAlbumAdapter: SearchAlbumsAdapter
        var searchAlbumInfoList: ArrayList<MyAlbumsDataClass> = ArrayList()
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchAlbumsFrag().apply {
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

        searchAlbumAdapter = SearchAlbumsAdapter( this)
        searchAlbumInfoList = SearchAlbumsAdapter.searchAlbumsData
        searchAlbumsRecylerView.adapter = searchAlbumAdapter;
        searchAlbumsRecylerView.layoutManager = LinearLayoutManager(context)
        searchAlbumsRecylerView.setHasFixedSize(true)
        searchAlbumsRecylerView.setClickable(true)

    }

    private fun getSearchAlbums() {
        searchAlbumsRecylerView.visibility = View.VISIBLE
        noAlbumResult.visibility = View.INVISIBLE
        val userInput = requireActivity().searchEditText.text.toString()

        val myFiles = java.util.ArrayList<MyAlbumsDataClass>()
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
        searchAlbumAdapter.updateList(myFiles)
        searchAlbumInfoList = myFiles
        if (userInput == "") {
            myFiles.clear()
            searchAlbumAdapter.updateList(myFiles)
            searchAlbumInfoList = myFiles
        }
        if (userInput == "" || searchAlbumInfoList.isEmpty()) {
            searchAlbumsRecylerView.visibility = View.INVISIBLE
            noAlbumResult.visibility = View.VISIBLE
        }
    }

    override fun searchAlbumListClickHandler(v: View?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun searchAlbumListAlbumCover(albumid: Long): Bitmap? {
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

    override fun onSearchAlbumSelect(album: MyAlbumsDataClass) {
        TODO("Not yet implemented")
    }

    private val albumCovers = mutableMapOf<Long, Bitmap>()
}