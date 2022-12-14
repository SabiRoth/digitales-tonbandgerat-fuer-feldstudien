package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/** The [MarkerEntity] represents the table with the marks a user made.
 *  [MarkTimestamp] represents a mark made with a given marker, attached to a given recording.
 *  [RecordingAndMarks] maps the one-to-many relationship between a recording and its marks.
 *  [MarkAndTimestamp] represents the relation between a MarkTimestamp and its corresponding [MarkerEntity].
 *  @author: Jonas Puchinger, Lisa Sanladerer
 */

@Entity(tableName = "markerTable")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "markerName") val markerName: String
)

@Entity(tableName = "markerTimeTable")
data class MarkTimestamp(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "mid") val mid: Int,
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @ColumnInfo(name = "markerId") val markerId: Int,
    @ColumnInfo(name = "markComment") val markComment: String? = null,
    @ColumnInfo(name = "markTimeInMilli") val markTimeInMilli: Int
)

data class RecordingAndMarks(
    @Embedded val recordingEntity: RecordingEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkTimestamp>
)

data class RecordingAndMarkTuple(
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @ColumnInfo(name = "markerId") val markerId: Int,
    @ColumnInfo(name = "markerName") val markerName: String
)

data class MarkAndTimestamp(
    @Embedded val marker: MarkerEntity,
    @Embedded val markTimestamp: MarkTimestamp
)
