package com.goodman.musicplayer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.goodman.musicplayer.*
import com.goodman.musicplayer.adapters.SearchAlbumsAdapter
import com.goodman.musicplayer.adapters.SearchArtistsAdapter
import com.goodman.musicplayer.adapters.SearchTracksAdapter
import com.goodman.musicplayer.adapters.SearchVPAdapter
import com.goodman.musicplayer.models.ArtistsModel
import com.goodman.musicplayer.models.SongModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createTabs()
    }

    private fun createTabs()
    {
        val myVpAdapter = SearchVPAdapter(parentFragmentManager, lifecycle)
        mySearchViewPager.adapter = myVpAdapter
        myVpAdapter.addFragment(SearchSongsFrag(),"Songs")
        myVpAdapter.addFragment(SearchAlbumsFrag(),"Albums")
        myVpAdapter.addFragment(SearchArtistsFrag(),"Artists")
        mySearchViewPager.offscreenPageLimit = 4

        TabLayoutMediator(searchTabLayout, mySearchViewPager) {tab, position ->
            mySearchViewPager.setCurrentItem(tab.position, true)

            if (position == 0)
                tab.text = "Songs"
            if (position == 1)
                tab.text = "Albums"
            if (position == 2)
                tab.text = "Artists"
        }.attach()
    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }





}