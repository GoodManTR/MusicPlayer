package com.goodman.musicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_song_table")
data class Favorite (
    @PrimaryKey(autoGenerate = true)
    val song_id: Long,
    val song_title: String,
    val song_artist: String,
    val song_data: String,
    val song_date: Long,
    val song_album_id: Long
)

