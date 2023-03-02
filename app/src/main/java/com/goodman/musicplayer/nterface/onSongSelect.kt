package com.goodman.musicplayer.nterface

import com.goodman.musicplayer.HeaderAdapter
import com.goodman.musicplayer.models.SongModel

interface onSongSelect {

    fun onSelect(song : SongModel, position: Int, holder: HeaderAdapter.VHItem, currentItem: SongModel)
}