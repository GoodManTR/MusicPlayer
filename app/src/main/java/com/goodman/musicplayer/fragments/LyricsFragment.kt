package com.goodman.musicplayer.fragments

import android.content.Context
import android.net.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.goodman.musicplayer.R
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.nterface.setLyricsInterface
import com.goodman.musicplayer.services.MusicService.Companion.currentSongData
import kotlinx.android.synthetic.main.fragment_lyrics.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LyricsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LyricsFragment : Fragment(), setLyricsInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLyricsInterface = this
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
        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        lateinit var setLyricsInterface: setLyricsInterface
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LyricsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun network() {
        val connectivityManager = requireActivity().getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val linkProperties = connectivityManager.getLinkProperties(currentNetwork)

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {
                Log.e("TAG", "The default network is now: " + network)

            }

            override fun onLost(network : Network) {
                Log.e("TAG", "The application no longer has a default network. The last default network was " + network)
            }

            override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
                Log.e("TAG", "The default network changed capabilities: $networkCapabilities")
            }

            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
                Log.e("TAG", "The default network changed link properties: $linkProperties")
            }
        })
    }



    override fun setLyrics(lyrics: ArrayList<String>) {
        var lastLyrics: String = ""
        for (lyric in lyrics) {
            lastLyrics += lyric + "\n"
        }
        lyricsTextView.text = ""
        lyricsTextView.text = lastLyrics.replace("[", "").replace("]", "")
//        Log.d("TAG3445", "LYRICS SET")
    }
}