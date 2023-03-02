package com.goodman.musicplayer.nterface

import com.goodman.musicplayer.models.SongModel

interface LyricsInterface {
    fun lyrics(songModel: SongModel)
    fun lyrics2(url: String)
}

interface setLyricsInterface {
    fun setLyrics(lyrics: ArrayList<String>)
}