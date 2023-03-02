package com.goodman.musicplayer.adapters

import com.goodman.musicplayer.R
import com.goodman.musicplayer.currentSong
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.goodman.musicplayer.fragments.PiecesFragment
import com.goodman.musicplayer.models.SongModel
import com.goodman.musicplayer.models.TrackListModel
import com.goodman.musicplayer.nterface.onSongSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.addto_recyler_adapter.view.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.recylerview_tool_adapter.view.*
import java.io.File
import java.text.Collator
import java.util.*


interface addToSheetInterface {
    fun addToViewListClickHandler(v: View?, position: Int)
    fun getAlbumCover(albumid: Long) : Bitmap?
    fun addToFavorite()
}

class AddToRecylerAdapter(private val addToSheetInterface: addToSheetInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View
        publicParent = parent
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.addto_recyler_adapter, parent, false)
            return VHItem(itemView)
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.addto_recyleradapter_header, parent, false)
            return VHHeader(itemView)
        }
        throw java.lang.RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        if (holder is VHItem) {
            val currentItem = getItem(position)
            holder.listTitle.text = currentItem.trackListTitle
            holder.songCount.text = currentItem.numberoOfSongs
//            holder.itemView.setOnClickListener{
//                addToSheetInterface.onAddToSelect(addToList[position -1 ])
//            }
            holder.itemView.setOnClickListener{
                addToSheetInterface.addToViewListClickHandler(holder.itemView, position - 1)
            }

        } else if (holder is VHHeader) {
            holder.itemView.setOnClickListener{
                addToSheetInterface.addToFavorite()
            }
        }
    }

    override fun getItemCount(): Int {
        return addToList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): TrackListModel {
        if (position == 0)
            return addToList[position]
        else
            return addToList[position - 1]
    }


    internal inner class VHItem(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        val listTitle = itemView!!.listTitle
        val songCount = itemView!!.listSongCount

        init {
            itemView!!.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = this.layoutPosition
            addToSheetInterface.addToViewListClickHandler(v, position)
        }
    }

    internal inner class VHHeader(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {


    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
        var addToList: ArrayList<TrackListModel> = ArrayList()
    }
    lateinit var publicParent: View

}

