package com.goodman.musicplayer


import android.graphics.Bitmap
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.SongModel
import kotlinx.android.synthetic.main.albums_recylerview.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import kotlinx.android.synthetic.main.header.view.*
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList


interface AlbumsFragmentInterface {
    fun recyclerViewListClickHandler(v: View?, position: Int)
    fun getAlbumCover(albumid: Long) : Bitmap?
}

class AlbumsRecylerAdapter(private val fragmentActivity: AlbumsFragmentInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View
        publicParent = parent
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.albums_recylerview, parent, false)
            return VHItem(itemView)
        } else if (viewType == TYPE_BLANK) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyler_blank, parent, false)
            return VHBlank(itemView)
        } else if (viewType == TYPE_HEADER) {
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.header, parent, false)
            return VHHeader(itemView)
        }
        throw java.lang.RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VHItem) {
            val currentItem = getItem(position)
            var image = fragmentActivity.getAlbumCover(currentItem.albumid);
            //cast holder to VHItem and set data
            if (image == null)
                holder.musicImage.setImageResource(R.drawable.asdas)
            else
            {
                holder.musicImage.setImageBitmap(image)
            }


            holder.listAlbumName.setText(currentItem.songName)
            holder.artistName.setText(currentItem.songArtist)
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
                                albumData,
                                Comparator<MyAlbumsDataClass> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.songName, obj2.songName) })
                            notifyDataSetChanged()
                            return true
                        } else if (myMenuItem == R.id.item2) {
                            holder.sortMenuButton.setText("Artıst")
                            Collections.sort(
                                albumData,
                                Comparator<MyAlbumsDataClass> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.songArtist, obj2.songArtist) })
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
        return albumData.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position) ) {
            return TYPE_HEADER
        } else if (isPositionBlank(position)) {
            return TYPE_BLANK
        } else {
            return TYPE_ITEM
        }
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun isPositionBlank(position: Int): Boolean {
        return position == 1
    }

    private fun getItem(position: Int): MyAlbumsDataClass {
        if (position <= 1)
            return albumData[position]
        else
            return albumData[position - 2]
    }

    fun updateList(myDataClassFiles: ArrayList<MyAlbumsDataClass>) {
        albumData = ArrayList()
        albumData.addAll(myDataClassFiles)
        PiecesFragment.noCollator.strength = Collator.SECONDARY
        Collections.sort(
            albumData,
            Comparator<MyAlbumsDataClass> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.songName, obj2.songName) })
        notifyDataSetChanged()
    }

    internal inner class VHItem(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        val listAlbumName: TextView = itemView!!.listAlbumName
        val artistName: TextView = itemView!!.albumArtistName
        //val isSongPlayingInfo: TextView = itemView!!.isSongPlayingInfo
        val musicImage: ImageView = itemView!!.albumMusicImage


        init {
            itemView!!.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = this.layoutPosition - 2
            fragmentActivity.recyclerViewListClickHandler(v, position)

        }
    }

    internal inner class VHHeader(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val sortMenuButton: Button = itemView!!.sortMenuButton
    }

    internal inner class VHBlank(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {


    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        private const val TYPE_BLANK = 3
        var albumData: ArrayList<MyAlbumsDataClass> = ArrayList()
    }
    lateinit var publicParent: View
}

