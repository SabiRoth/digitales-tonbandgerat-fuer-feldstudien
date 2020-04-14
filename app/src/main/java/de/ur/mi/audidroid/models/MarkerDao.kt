package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * The [MarkerDao] is the data access object to access the tables storing [MarkerEntity], [MarkTimestamp] and [MarkAndTimestamp] in the apps' room database.
 * Functions returning [LiveData] are incompatible wit Kotlin coroutines, therefore these functions are not suspended.
 * @author: Jonas Puchinger
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markerTable")
    fun getAllMarkers(): LiveData<List<MarkerEntity>>

    @Query("SELECT COUNT(uid) FROM markerTable")
    suspend fun getMarkerCount(): Int

    @Query("SELECT * FROM markerTable WHERE uid = :key")
    fun getMarkerById(key: Int): LiveData<MarkerEntity>

    @Query("SELECT * FROM markerTable WHERE markerName = :name")
    suspend fun getMarkerByName(name: String): List<MarkerEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertMarker(markerEntity: MarkerEntity)

    @Update
    suspend fun updateMarker(markerEntity: MarkerEntity)

    @Delete
    suspend fun deleteMarker(markerEntity: MarkerEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkTimestamp)

    @Update
    suspend fun updateMarkTimestamp(markTimestamp: MarkTimestamp)

    @Transaction
    @Query("SELECT DISTINCT * FROM markerTable INNER JOIN markerTimeTable ON markerTable.uid = markerTimeTable.markerId WHERE markerTimeTable.recordingId LIKE :key")
    fun getMarksById(key: Int): LiveData<List<MarkAndTimestamp>>

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key IN (SELECT DISTINCT(mid) FROM markerTimeTable)")
    fun getRecordingFromIdInclMarks(key: Int): LiveData<List<RecordingAndMarks>>

    @Query("SELECT * FROM markerTimeTable WHERE recordingId = :key")
    fun allMarks(key: Int): LiveData<List<MarkTimestamp>>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)

    @Query("DELETE FROM markerTimeTable WHERE mid = :key")
    suspend fun deleteMark(key: Int)
}
