package com.goodman.musicplayer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToFavorites (favorite: Favorite)

    @Update
    suspend fun updateFavorites(favorite: Favorite)

    @Delete
    suspend fun deleteFavorites(favorite: Favorite)

    @Query("SELECT * FROM favorite_song_table ORDER BY song_title ASC")
    fun readAllData(): LiveData<List<Favorite>>
}