package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

/**
 * The [RecordingDao] is the data access object for the [RecordingEntity]
 * The DAO provide methods that offer abstract access to the app's database
 * @author: Sabine Roth
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordingsTable")
    fun getAllRecordings(): LiveData<List<RecordingEntity>>

    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingById(key: Int): LiveData<RecordingEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(recordingEntity: RecordingEntity): Long

    @Query("INSERT INTO recordingsTable (uid, recordingName, recordingPath, date, duration) SELECT null, recordingName, recordingPath, date, duration FROM recordingsTable WHERE uid = :key")
    suspend fun getCopiedRecordingById(key: Int): Long

    @Query("UPDATE recordingsTable SET recordingName = :name, recordingPath = :path WHERE uid = :copiedRecordingId")
    suspend fun updatePreviousRecording(copiedRecordingId: Int, name: String, path: String)

    @Update
    suspend fun updateRecording(recordingEntity: RecordingEntity)

    @Query("UPDATE recordingsTable SET recordingName = :name, recordingPath = :path, date = :date WHERE uid = :key")
    suspend fun updateNameAndPath(key: Int, name: String, path: String, date: String)

    @Query("DELETE FROM recordingsTable WHERE uid = :key")
    suspend fun delete(key: Int)
}
