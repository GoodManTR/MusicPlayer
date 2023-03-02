package com.goodman.musicplayer.nterface

import com.goodman.musicplayer.data.Favorite
import com.goodman.musicplayer.models.SongModel

interface onFavoriteSongSelect {

    fun onFavoriteSelect(song : SongModel)

}