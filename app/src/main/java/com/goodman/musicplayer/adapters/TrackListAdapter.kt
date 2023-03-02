package com.goodman.musicplayer.adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goodman.musicplayer.HeaderAdapter
import com.goodman.musicplayer.R
import com.goodman.musicplayer.activities.MainActivity
import com.goodman.musicplayer.currentSong
import com.goodman.musicplayer.data.PlaylistWithSongs
import com.goodman.musicplayer.data.SongEntity
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.fragments.TrackListsFragment
import com.goodman.musicplayer.models.ArtistsModel
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.models.TrackListModel
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


interface TrackListInterface {
    fun trackListListClickHandler(v: View?, position: Int)
    fun getTrackListAlbumCover(albumid: Long) : Bitmap?
}

class TrackListAdapter(private val trackListFragment: TrackListInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View
        publicParent = parent
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.recylerview_tool_adapter, parent, false)
            return VHItem(itemView)
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.header, parent, false)
            return VHHeader(itemView)
        }
        throw java.lang.RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VHItem) {
            val currentItem = getItem(position)
            publicMainActivity = trackListFragment

            MainActivity.mPlaylistDatabaseViewModel.readAllData.observe(TrackListsFragment.publicViewLifecycleOwner) {
                it.map { PlaylistWithSongs ->
                    if (currentItem.trackListId == PlaylistWithSongs.playlist.playlistId) {
                        for (song in PlaylistWithSongs.songs) {
                            if (trackListFragment.getTrackListAlbumCover(song.songAlbumID) != null) {
                                holder.musicImage.setImageBitmap(trackListFragment.getTrackListAlbumCover(song.songAlbumID))
                                break
                            }
                        }
                    }
                }
            }

            holder.playlistName.setText(currentItem.trackListTitle)
            holder.songCount.setText(currentItem.numberoOfSongs + " Songs")

        } else if (holder is VHHeader) {
            //cast holder to VHHeader and set data for header.
            holder.sortMenuButton.setOnClickListener() {
                var popupMenu = PopupMenu(publicParent.context, it)

                popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        var myMenuItem = item!!.itemId
                        PiecesFragment.noCollator.strength = Collator.SECONDARY
                        if (myMenuItem == R.id.srtByName) {
                            holder.sortMenuButton.setText("Name")
                            Collections.sort(
                                trackListData,
                                Comparator<TrackListModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.trackListTitle, obj2.trackListTitle) })
                            notifyDataSetChanged()
                            return true
                        } else if (myMenuItem == R.id.item2) {
                            holder.sortMenuButton.setText("Artıst")
                            Collections.sort(
                                trackListData,
                                Comparator<TrackListModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.trackListTitle, obj2.trackListTitle) })
                            notifyDataSetChanged()
                            return true
                        } else {
                            return false
                        }
                    }
                })
                popupMenu.show();
            }


        }
    }

    override fun getItemCount(): Int {
        return trackListData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): TrackListModel {
        if (position == 0)
            return trackListData[position]
        else
            return trackListData[position - 1]
    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateList(myDataClassFiles: ArrayList<TrackListModel>) {
        trackListData = ArrayList()
        trackListData.addAll(myDataClassFiles)
        PiecesFragment.noCollator.strength = Collator.SECONDARY
        Collections.sort(
            trackListData,
            Comparator<TrackListModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.trackListTitle, obj2.trackListTitle) })
        notifyDataSetChanged()
    }


    inner class VHItem(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        val playlistName: TextView = itemView!!.listSongName
        val songCount: TextView = itemView!!.songArtistName
        val musicImage: ImageView = itemView!!.songMusicImage

        init {
            itemView!!.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = this.layoutPosition
            trackListFragment.trackListListClickHandler(v, position)

        }
    }

    internal inner class VHHeader(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val sortMenuButton: Button = itemView!!.sortMenuButton

    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        var trackListData: ArrayList<TrackListModel> = ArrayList()
        lateinit var publicMainActivity: TrackListInterface
    }
    lateinit var publicParent: View

}