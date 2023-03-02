package com.goodman.musicplayer.data

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.NO_ACTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity
data class SongEntity (
    @PrimaryKey
    @ColumnInfo(name = "songId")
    val songId: Long,
    @ColumnInfo(name = "title")
    val songTitle: String,
    @ColumnInfo(name = "artist")
    val songArtist: String,
    @ColumnInfo(name = "data")
    val songData: String,
    @ColumnInfo(name = "date")
    val songDate: Long,
    @ColumnInfo(name = "albumId")
    val songAlbumID: Long
)

@Entity
data class PlaylistEntity (
    @PrimaryKey
    @ColumnInfo(name = "playlistId")
    var playlistId: Long,
    @ColumnInfo(name = "playlistTitle")
    var playlistTitle: String?
)

@Entity(
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = arrayOf("playlistId"),
        childColumns = arrayOf("playlistId"),
        onDelete = CASCADE
    ), ForeignKey(
        entity = SongEntity::class,
        parentColumns = arrayOf("songId"),
        childColumns = arrayOf("songId"),
        onDelete = CASCADE
    )]
)
data class PlaylistSongCrossEntity (
    var playlistId: Long,
    @ColumnInfo(index = true)
    var songId: Long
)


data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongCrossEntity::class)
    )
    val songs: List<SongEntity>
)



@Database(entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongCrossEntity::class], version = 1, exportSchema = false)
abstract class PlaylistDatabase: RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: PlaylistDatabase? = null

        fun getDatabase(context: Context): PlaylistDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaylistDatabase::class.java,
                    "playlist_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

class PlaylistDataRepository(private var playlistDao: PlaylistDao) {

    val readAllData: LiveData<List<PlaylistWithSongs>> = playlistDao.getPlaylistsWithSongs()

    fun insertPlaylistEntity(playlist: PlaylistEntity) {
        playlistDao.insertPlaylistEntity(playlist)
    }

    fun insertSongEntity(songEntity: SongEntity) {
        playlistDao.insertSongEntity(songEntity)
    }

    fun PlaylistWithSongs(playlistSongCrossEntity: PlaylistSongCrossEntity) {
        playlistDao.PlaylistWithSongs(playlistSongCrossEntity)
    }
}

class PlaylistDatabaseViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<PlaylistWithSongs>>
    private val repository: PlaylistDataRepository

    init {
        val playlistDao = PlaylistDatabase.getDatabase(application).playlistDao()
        repository = PlaylistDataRepository(playlistDao)
        readAllData = repository.readAllData
    }

    fun insertPlaylistEntity(playlist: PlaylistEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPlaylistEntity(playlist)
        }
    }

    fun insertSongEntity(songEntity: SongEntity)  {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSongEntity(songEntity)
        }
    }

    fun insertPlaylistWithSongs(playlistSongCrossEntity: PlaylistSongCrossEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.PlaylistWithSongs(playlistSongCrossEntity)
        }
    }
}
