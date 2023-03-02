package com.goodman.musicplayer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlaylistEntity(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongEntity(songEntity: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun PlaylistWithSongs(playlistSongCrossEntity: PlaylistSongCrossEntity)

    @Update
    fun updatePlaylist(playlist: PlaylistEntity)
    @Delete
    fun deletePlaylist(playlist: PlaylistEntity)

    @Transaction
    @Query("SELECT * FROM PlaylistEntity")
    fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>>
}