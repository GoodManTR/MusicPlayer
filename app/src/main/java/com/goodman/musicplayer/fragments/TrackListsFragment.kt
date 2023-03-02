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
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodman.musicplayer.*
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.activities.MainActivity.Companion.mPlaylistDatabaseViewModel
import com.goodman.musicplayer.adapters.TrackListAdapter
import com.goodman.musicplayer.adapters.TrackListAdapter.Companion.trackListData
import com.goodman.musicplayer.adapters.TrackListInterface
import com.goodman.musicplayer.adapters.TrackListSongAdapter
import com.goodman.musicplayer.adapters.TrackListSongAdapter.Companion.tracklistSongAdapterData
import com.goodman.musicplayer.adapters.trackListFragmentInterface
import com.goodman.musicplayer.data.*
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.models.TrackListModel
import com.goodman.musicplayer.nterface.onTracklistSongSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_pieces.*
import kotlinx.android.synthetic.main.fragment_track_lists.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
//lateinit var mPlaylistViewModel: PlaylistViewModel
/**
 * A simple [Fragment] subclass.
 * Use the [TrackListsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrackListsFragment : Fragment(), TrackListInterface, trackListFragmentInterface, onTracklistSongSelect {
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
        setHasOptionsMenu(true)
        publicViewLifecycleOwner = viewLifecycleOwner
//        mPlaylistViewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)
        return inflater.inflate(R.layout.fragment_track_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startEngine()
        getPlaylists()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var myTrackListAdapter : TrackListAdapter
        var trackListInfoList : ArrayList<TrackListModel> = ArrayList()
        @SuppressLint("StaticFieldLeak")
        lateinit var myTrackListSongAdapter : TrackListSongAdapter
        var trackListSongInfoList : ArrayList<SongModel> = ArrayList()

        lateinit var publicViewLifecycleOwner: LifecycleOwner
        var myPlaylistID: Long = 1
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TrackListsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getPlaylists() {
        val oldListSongs: ArrayList<SongEntity> = ArrayList()

        mPlaylistDatabaseViewModel.readAllData.observe(viewLifecycleOwner) {
            trackListInfoList.clear()
            it.map { item ->
                oldListSongs.clear()
                item.songs.map { Song ->
                    oldListSongs.add(SongEntity(Song.songId, Song.songTitle, Song.songArtist, Song.songData, Song.songDate, Song.songAlbumID))
                }
                trackListInfoList.add(TrackListModel(item.playlist.playlistId!!, item.playlist.playlistTitle!!, oldListSongs.size.toString()))
            }
            myTrackListAdapter.notifyDataSetChanged()
            if (trackListInfoList.isEmpty()) {
                trackListsRecylerView.visibility = View.GONE
                noPlaylistInfo.visibility = View.VISIBLE
            } else {
                noPlaylistInfo.visibility = View.GONE
                trackListsRecylerView.visibility = View.VISIBLE
            }
        }
    }

    private fun startEngine()
    {
        myTrackListAdapter = TrackListAdapter( this)
        trackListInfoList = trackListData
        trackListsRecylerView.adapter = myTrackListAdapter;
        trackListsRecylerView.layoutManager = LinearLayoutManager(context)
        trackListsRecylerView.setHasFixedSize(true)
        trackListsRecylerView.setClickable(true)
        val dividerItemDecoration = DividerItemDecoration(
            trackListsRecylerView.context,
            LinearLayoutManager(requireActivity().applicationContext).orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(com.goodman.musicplayer.R.drawable.recylerview_divider)!!)
        trackListsRecylerView.addItemDecoration(dividerItemDecoration)
    }

    private fun startPlaylistEngine()
    {
        myTrackListSongAdapter = TrackListSongAdapter( this,this)
        trackListSongInfoList = tracklistSongAdapterData
        requireActivity().everythingRecylerView.adapter = myTrackListSongAdapter;
        requireActivity().everythingRecylerView.layoutManager = LinearLayoutManager(context)
        requireActivity().everythingRecylerView.setHasFixedSize(true)
        requireActivity().everythingRecylerView.setClickable(true)
    }


    private fun getPlaylistSongs() {
        mPlaylistDatabaseViewModel.readAllData.observe(viewLifecycleOwner) {
            it.map {
                if (myPlaylistID == it.playlist.playlistId) {
                    trackListSongInfoList.clear()
                    it.songs.map { t ->
                        trackListSongInfoList.add(SongModel(t.songId!!, t.songTitle!!,t.songArtist!!,t.songData!!,t.songDate!!,t.songAlbumID!!, false))
                    }
                }
            }
        }
    }

    override fun trackListListClickHandler(v: View?, position: Int) {
        myPlaylistID = trackListInfoList[position - 1].trackListId
        Log.d("TAG3434", trackListInfoList[position - 1].trackListId.toString())

        requireActivity().everythingToolbar.title = trackListInfoList[position - 1].trackListTitle
        requireActivity().everythingBottomSheet.visibility = View.VISIBLE

        startPlaylistEngine()
        getPlaylistSongs()

        BottomSheetBehavior.from(requireActivity().everythingBottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTrackListAlbumCover(albumid: Long): Bitmap? {
        if (trackListAlbumCovers.containsKey(albumid))
        {
            return trackListAlbumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {

                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(100, 100), null)
                trackListAlbumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }

        return returnvar;
    }

    override fun trackListSongRecylerViewListClickHandler(v: View?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onTrackListSelect(song: SongModel) {
        try {
            isSongOpenedFromArtist = false
            isSongOpenedFromAlbum = false
            isSongOpenedFromPreferences = false
            isSongOpenedFromFavorites = false
            MainActivity.musicService.setSong(song)
            MainActivity.songModel = song
        } catch (e: java.lang.Exception) {
            Toast.makeText(context,"Song format not supported.", Toast.LENGTH_SHORT).show()
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

    override fun getTrackListSongCover(albumid: Long): Bitmap? {
        if (trackListSongsAlbumCovers.containsKey(albumid))
        {
            return trackListSongsAlbumCovers.get(albumid)
        }

        var returnvar: Bitmap? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumid)

            try {

                returnvar = giveMeContentResolverPlease().loadThumbnail(uri, Size(100, 100), null)
                trackListSongsAlbumCovers.putIfAbsent(albumid,returnvar);
            } catch (e: java.lang.Exception) {

            }

        }

        return returnvar;
    }


    val trackListSongsAlbumCovers = mutableMapOf<Long, Bitmap>()
    val trackListAlbumCovers = mutableMapOf<Long, Bitmap>()

}