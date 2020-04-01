package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

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

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :copiedRecordingId AND markTimeInMilli NOT BETWEEN :startTimeInMilli AND :endTimeInMilli")
    suspend fun deleteOuterMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int)

    @Query("UPDATE markerTimeTable SET markTimeInMilli = markTimeInMilli - :startTimeInMilli WHERE recordingId = :copiedRecordingId")
    suspend fun updateInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int)

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :copiedRecordingId AND markTimeInMilli BETWEEN :startTimeInMilli AND :endTimeInMilli")
    suspend fun deleteInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int)

    @Query("UPDATE markerTimeTable SET markTimeInMilli = markTimeInMilli - :durationInMilli WHERE recordingId = :copiedRecordingId AND markTimeInMilli > :durationInMilli")
    suspend fun updateOuterMarks(copiedRecordingId: Int, durationInMilli: Int)

    @Query("INSERT INTO markerTimeTable (mid, recordingId, markerId, markComment, markTimeInMilli) SELECT null, :copiedRecordingId, markerId, markComment, markTimeInMilli FROM markerTimeTable WHERE recordingId = :key")
    suspend fun copyMarks(key: Int, copiedRecordingId: Int)

    @Transaction
    @Query("SELECT DISTINCT * FROM markerTable INNER JOIN markerTimeTable ON markerTable.uid = markerTimeTable.markerId WHERE markerTimeTable.recordingId LIKE :key ORDER BY markTimeInMilli")
    fun getMarksById(key: Int): LiveData<List<MarkAndTimestamp>>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)

    @Query("DELETE FROM markerTimeTable WHERE mid = :key")
    suspend fun deleteMark(key: Int)
}
