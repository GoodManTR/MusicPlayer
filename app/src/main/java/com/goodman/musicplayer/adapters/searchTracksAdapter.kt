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
import com.goodman.musicplayer.R
import com.goodman.musicplayer.currentSong
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.ArtistsModel
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.models.TrackListModel
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import kotlinx.android.synthetic.main.search_sheet_list_header.view.*
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


interface SearchSongsInterface {
    fun searchSongListClickHandler(v: View?, position: Int)
    fun searchSongListAlbumCover(albumid: Long) : Bitmap?
    fun onSearchSongSelect(song : SongModel)
}

class SearchTracksAdapter(private val searchFragment: SearchSongsInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View
        publicParent = parent

            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.recylerview_tool_adapter, parent, false)
            return VHItem(itemView)


    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VHItem) {
            val currentItem = getItem(position)
            publicSearchInterface = searchFragment

            var image = searchFragment.searchSongListAlbumCover(currentItem.song_album_id);
            if (image == null)
                holder.musicImage.setImageResource(R.drawable.asdas)
            else
            {
                holder.musicImage.setImageBitmap(image)
            }

            holder.playlistName.setText(currentItem.song_title)
            holder.songCount.setText(currentItem.song_artist)

            holder.itemView.setOnClickListener {
                searchFragment.onSearchSongSelect(searchSongsData[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return searchSongsData.size
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM
    }



    private fun getItem(position: Int): SongModel {
        return searchSongsData[position]
    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateList(myDataClassFiles: ArrayList<SongModel>) {
        searchSongsData = ArrayList()
        searchSongsData.addAll(myDataClassFiles)
        PiecesFragment.noCollator.strength = Collator.SECONDARY
        Collections.sort(
            searchSongsData,
            Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
        notifyDataSetChanged()
    }


    internal inner class VHItem(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        val playlistName: TextView = itemView!!.listSongName
        val songCount: TextView = itemView!!.songArtistName
        val musicImage: ImageView = itemView!!.songMusicImage

        init {
            itemView!!.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = this.layoutPosition
            searchFragment.searchSongListClickHandler(v, position)

        }
    }


    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        var searchSongsData: ArrayList<SongModel> = ArrayList()
        lateinit var publicSearchInterface: SearchSongsInterface
    }
    lateinit var publicParent: View

}