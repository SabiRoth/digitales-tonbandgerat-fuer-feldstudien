package de.ur.mi.audidroid.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The MarkerEntity represents the table with the marks a user made.
 * @author: Lisa Sanladerer
 */

@Entity(tableName = "markerTimeTable")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val mid: Int,
    val recordingId: Int,
    val markName: String,
    val markTime: String
)
