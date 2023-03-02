package com.goodman.musicplayer.data

import androidx.lifecycle.LiveData


class FavoriteRepository(private var favoriteDao: FavoriteDao) {

    val readAllData: LiveData<List<Favorite>> = favoriteDao.readAllData()

    suspend fun addToFavorite(favorite: Favorite) {
        favoriteDao.addToFavorites(favorite)
    }

    suspend fun updateFavorites(favorite: Favorite) {
        favoriteDao.updateFavorites(favorite)
    }

    suspend fun deleteFavorite(favorite: Favorite) {
        favoriteDao.deleteFavorites(favorite)
    }
}