package com.goodman.musicplayer

var currentSong = -1 //for prev next button
var currentAlbumSong = -1 // same
var currentArtistSong = -1 //same
var currentFavoriteSong = -1
var repeatState = 0 // repeat button
var repeatCount = 0 // for repeat only once
var isSongOpenedFromAlbum = false //for using album song list
var isSongOpenedFromArtist = false
var shuffleState = 0
var isSongOpenedFromPreferences = false
var isSongOpenedFromFavorites = false
var favoriteState = 0
var calledFromListBtn = 0
var calledFromNavigationBtn = 0
var once = 0
var once2 = 0

