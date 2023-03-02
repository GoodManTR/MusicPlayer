package com.goodman.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.nterface.onSongSelect
import com.goodman.musicplayer.services.MusicService
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import java.io.File
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList


interface MainActivityInterface {
    fun recyclerViewListClickHandler(v: View?, position: Int)
    fun getAlbumCover(albumid: Long) : Bitmap?
    fun onAddToClicked(position: Int)
    fun onLongClickEnabled()
    fun longSelectDestroyer()
    fun setNowPlayingText(holder: HeaderAdapter.VHItem, currentItem: SongModel)
}

class HeaderAdapter(private val mainActivity: MainActivityInterface, var onSongSelect: onSongSelect) :
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


    private fun onItemLongClicked(holder: HeaderAdapter.VHItem, currentItem: SongModel, position: Int) {
        isPiecesSelectionEnable = true
        holder.isSelectedImageView.visibility = View.VISIBLE
        selectedItemList.add(currentItem)
        currentItem.is_selected = true
        mainActivity.onLongClickEnabled()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        if (holder is VHItem) {
            publicMainActivity = mainActivity
            val currentItem = getItem(position)

            var image = mainActivity.getAlbumCover(currentItem.song_album_id);
            if (image == null)
                holder.musicImage.setImageResource(R.drawable.asdas)
            else
            {
                holder.musicImage.setImageBitmap(image)
            }



            holder.listSongName.setText(currentItem.song_title)
            if (currentItem.song_artist == "<unknown>") {
                holder.artistName.text = "Unknown"
            } else {
                holder.artistName.text = currentItem.song_artist
            }


            if (!selectedItemList.contains(currentItem)) {
                holder.isSelectedImageView.visibility = View.GONE
            } else if (selectedItemList.contains(currentItem)) {
                holder.isSelectedImageView.visibility = View.VISIBLE
            }

            if (isPiecesSelectionEnable) {
                holder.listButton.visibility = View.GONE
            } else {
                holder.listButton.visibility = View.VISIBLE
                holder.isSelectedImageView.visibility = View.GONE
            }



            mainActivity.setNowPlayingText(holder, currentItem)


            holder.itemView.setOnClickListener{
                if (selectedItemList.contains(currentItem)) {
                    selectedItemList.remove(currentItem)
                    currentItem.is_selected = false
                    mainActivity.onLongClickEnabled()
                    holder.isSelectedImageView.visibility = View.GONE
                    if (selectedItemList.isEmpty()) {
                        isPiecesSelectionEnable = false
                        mainActivity.onLongClickEnabled()
                    }
                } else if (isPiecesSelectionEnable && !selectedItemList.contains(currentItem)) {
                    onItemLongClicked(holder,currentItem,position - 1)
                } else {
                    currentSong = position - 1
                    onSongSelect.onSelect(data2[position -1 ], position - 1, holder, currentItem)
                }
            }

            holder.itemView.setOnLongClickListener {
                if (!selectedItemList.contains(currentItem))
                    onItemLongClicked(holder,currentItem,position - 1)
                true
            }

            holder.listButton.setOnClickListener {

                    val wrapper: Context = ContextThemeWrapper(publicParent.context, R.style.Goodman_PopupMenu)
                    val popupMenu = PopupMenu(wrapper, it)

                    popupMenu.menuInflater.inflate(R.menu.recylerview_menu, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onMenuItemClick(item: MenuItem?): Boolean {
                            val myMenuItem = item!!.itemId

                            if (myMenuItem == R.id.optionsDelete) {
                                val builder: AlertDialog.Builder = AlertDialog.Builder(publicParent.context)
                                builder.setTitle("Are you sure?")
                                builder.setMessage(currentItem.song_title +" will be deleted.")
                                builder.setNegativeButton("Cancel", null)
                                builder.setPositiveButton("Delete",
                                    DialogInterface.OnClickListener { dialogInterface, i ->
                                        val fdelete: File = File(currentItem.song_data)
                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                Log.d("TAG3434", data2[position].song_title.toString())
                                                notifyItemRemoved(position)
                                                data2.removeAt(position - 1)
                                                notifyItemRangeChanged(position, data2.lastIndex)
                                            } else {
                                                Log.d("TAG3434", "couldnt delete?")
                                            }
                                        } else {
                                            Log.d("TAG3434", "dont exist")
                                        }
                                    })
                                builder.show()
                                return true
                            } else if (myMenuItem == R.id.optionsShare) {
                                Log.d("TAG3434", currentItem.song_artist)
                                val sharingIntent = Intent(Intent.ACTION_SEND)
                                val songUri: Uri = Uri.parse(currentItem.song_data)
                                sharingIntent.type = "audio/*"
                                sharingIntent.putExtra(Intent.EXTRA_STREAM, songUri)
                                publicParent.context.startActivity(Intent.createChooser(sharingIntent, "Share using"))
                                return true
                            } else if (myMenuItem == R.id.optionsAddTo) {
                                calledFromListBtn = 1
                                mainActivity.onAddToClicked(position - 1)
                                return true
                            } else {
                                return false
                            }
                        }
                    })
                    popupMenu.show();
                }
        } else if (holder is VHHeader) {
            holder.sortMenuButton.isEnabled = !isPiecesSelectionEnable
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
                                data2,
                                Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
                            //notifyItemRangeChanged(0, itemCount)
                            notifyDataSetChanged()
                            return true
                        } else if (myMenuItem == R.id.item2) {
                            holder.sortMenuButton.setText("Artıst")
                            Collections.sort(
                                data2,
                                Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_artist, obj2.song_artist) })
                            //notifyItemRangeChanged(0, itemCount)
                            notifyDataSetChanged()
                            return true
                        } else if (myMenuItem == R.id.item3) {
                            holder.sortMenuButton.setText("Date")
                            Collections.sort(
                                data2,
                                Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj2.song_date.toString(), obj1.song_date.toString()) })
                            //notifyItemRangeChanged(0, itemCount)
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
        return data2.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): SongModel {
        if (position == 0)
            return data2[position]
        else
            return data2[position - 1]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(myDataClassFiles: ArrayList<SongModel>) {
        data2 = ArrayList()
        data2.addAll(myDataClassFiles)
        PiecesFragment.noCollator.strength = Collator.SECONDARY
        Collections.sort(
            data2,
            Comparator<SongModel> { obj1, obj2 -> PiecesFragment.noCollator.compare(obj1.song_title, obj2.song_title) })
        notifyDataSetChanged()
    }


    inner class VHItem(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val listSongName: TextView = itemView.listSongName
        val artistName: TextView = itemView.songArtistName
        //val isSongPlayingInfo: TextView = itemView!!.isSongPlayingInfo
        val musicImage: ImageView = itemView.songMusicImage
        val listButton: ImageButton = itemView.listTreeDots
        val isSelectedImageView: ImageView = itemView.isSelectedImageView

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = this.layoutPosition
            mainActivity.recyclerViewListClickHandler(v, position)

        }
    }

    internal inner class VHHeader(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val sortMenuButton: Button = itemView!!.sortMenuButton

    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        var data2: ArrayList<SongModel> = ArrayList()
        lateinit var publicMainActivity: MainActivityInterface
        val selectedItemList = ArrayList<SongModel>()
        var isPiecesSelectionEnable = false
    }

    lateinit var publicParent: View

}

