package com.goodman.musicplayer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.ArtistsFrag
import com.goodman.musicplayer.R
import com.goodman.musicplayer.adapters.SearchArtistsAdapter
import com.goodman.musicplayer.adapters.SearchArtistsInterface
import com.goodman.musicplayer.models.ArtistsModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search_artists.*
import kotlinx.android.synthetic.main.fragment_search_songs.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchArtistsFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchArtistsFrag : Fragment(), SearchArtistsInterface {
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
        return inflater.inflate(R.layout.fragment_search_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startEngine()

        val dividerItemDecoration2 = DividerItemDecoration(
            searchArtistsRecylerView.context,
            LinearLayoutManager(context).orientation
        )
        dividerItemDecoration2.setDrawable(requireContext().getDrawable(R.drawable.recylerview_divider)!!)
        searchArtistsRecylerView.addItemDecoration(dividerItemDecoration2)

        requireActivity().searchEditText.addTextChangedListener {
            getSearchArtists()
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var searchArtistAdapter: SearchArtistsAdapter
        var searchArtistInfoList: ArrayList<ArtistsModel> = ArrayList()
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchArtistsFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun startEngine()
    {

        searchArtistAdapter = SearchArtistsAdapter( this)
        searchArtistInfoList = SearchArtistsAdapter.searchArtistData
        searchArtistsRecylerView.adapter = searchArtistAdapter;
        searchArtistsRecylerView.layoutManager = LinearLayoutManager(context)
        searchArtistsRecylerView.setHasFixedSize(true)
        searchArtistsRecylerView.setClickable(true)
    }

    private fun getSearchArtists() {
        searchArtistsRecylerView.visibility = View.VISIBLE
        noArtistResult.visibility = View.INVISIBLE
        val userInput = requireActivity().searchEditText.text.toString()

        val myFiles = java.util.ArrayList<ArtistsModel>()
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
        searchArtistAdapter.updateList(myFiles)
        searchArtistInfoList = myFiles
        if (userInput == "") {
            myFiles.clear()
            searchArtistAdapter.updateList(myFiles)
            searchArtistInfoList = myFiles
        }
        if (userInput == "" || searchArtistInfoList.isEmpty()) {
            searchArtistsRecylerView.visibility = View.INVISIBLE
            noArtistResult.visibility = View.VISIBLE
        }
    }

    override fun searchArtistListClickHandler(v: View?, position: Int) {

    }

    override fun onSearchArtistSelect(artist: ArtistsModel) {

    }
}