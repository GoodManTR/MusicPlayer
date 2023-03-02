package com.goodman.musicplayer

import android.graphics.Bitmap
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.onAlbumSongSelect
import kotlinx.android.synthetic.main.albums_recylerview.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import kotlinx.android.synthetic.main.header.view.*
import java.text.Collator

import java.util.*
import kotlin.collections.ArrayList


interface AlbumSongActitivityInterface {
    fun albumSongRecylerViewListClickHandler(v: View?, position: Int)
    fun getAlbumSongCover(albumid: Long) : Bitmap?
}

class AlbumSongRecylerAdapter( private val albumSongActivity: AlbumSongActitivityInterface,var  onAlbumSongSelect: onAlbumSongSelect) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VHItem) {
            val currentItem = getItem(position)
            var image = albumSongActivity.getAlbumSongCover(currentItem.song_album_id);
            //cast holder to VHItem and set data
            if (image == null)
                holder.musicImage.setImageResource(R.drawable.asdas)
            else
            {
                holder.musicImage.setImageBitmap(image)
            }


            holder.listSongName.setText(currentItem.song_title)
            holder.artistName.setText(currentItem.song_artist)
            holder.itemView.setOnClickListener{
                currentAlbumSong = position - 1
                onAlbumSongSelect.onAlbumSelect(albumSongAdapterData[position - 1])
            }
            //holder.isSongPlayingInfo.setText(currentItem.isPlaying)
        } else if (holder is VHHeader) {
            //cast holder to VHHeader and set data for header.
            holder.sortMenuButton.setOnClickListener() {
                var popupMenu = PopupMenu(publicParent.context, it)

                popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        var myMenuItem = item!!.itemId
                        PiecesFragment.noCollator.strength = Collator.SECONDARY
                        if (myMenuItem == R.id.srtByName) {
                            holder.sortMenuButton.setText("Name")
                            Collections.sort(
                                albumSongAdapterData,
                                Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
                            notifyDataSetChanged()
                            return true
                        } else if (myMenuItem == R.id.item2) {
                            holder.sortMenuButton.setText("Artıst")
                            Collections.sort(
                                albumSongAdapterData,
                                Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_artist, obj2.song_artist) })
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
        return albumSongAdapterData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): SongModel {
        if (position == 0)
            return albumSongAdapterData[position + 1]
        else
            return albumSongAdapterData[position - 1]
    }

    internal inner class VHItem(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        val listSongName: TextView = itemView!!.listSongName
        val artistName: TextView = itemView!!.songArtistName
        //val isSongPlayingInfo: TextView = itemView!!.isSongPlayingInfo
        val musicImage: ImageView = itemView!!.songMusicImage


        init {
            itemView!!.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = this.layoutPosition
            albumSongActivity.albumSongRecylerViewListClickHandler(v, position)

        }
    }

    internal inner class VHHeader(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val sortMenuButton: Button = itemView!!.sortMenuButton

    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        var albumSongAdapterData: ArrayList<SongModel> = ArrayList()
    }
    lateinit var publicParent: View
}

