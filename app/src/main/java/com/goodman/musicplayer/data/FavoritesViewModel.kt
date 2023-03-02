package com.goodman.musicplayer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<Favorite>>
    private val repository: FavoriteRepository

    init {
        val favoriteDao = FavoritesDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)
        readAllData = repository.readAllData
    }

    fun addToFavorites(favorite: Favorite){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToFavorite(favorite)
        }
    }

    fun updateFavorites(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavorites(favorite)
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavorite(favorite)
        }
    }
}