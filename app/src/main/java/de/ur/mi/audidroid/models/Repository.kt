package de.ur.mi.audidroid.models

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class Repository(application: Application): CoroutineScope {

    private var entryDao: EntryDao
    private var labelDao: LabelDao
    private var markerDao: MarkerDao
    private var allRecordings: LiveData<List<EntryEntity>>

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        entryDao = database.entryDao()
        labelDao = database.labelDao()
        markerDao = database.markerDao()
        allRecordings = entryDao.getAllRecordings()
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }

    fun getAllMarkers(): LiveData<List<MarkerEntity>> {
        return markerDao.getAllMarkers()
    }

    fun delete(entryEntity: EntryEntity) {
        CoroutineScope(coroutineContext).launch {
            entryDao.delete(entryEntity)
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.delete(labelEntity)
        }
    }

    fun deleteMarker(markerEntity: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteMarker(markerEntity)
        }
    }

    fun insert(entryEntity: EntryEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = entryDao.insert(entryEntity)
            }
        }
        return temp!!
    }

    fun insertMarker(markerEntity: MarkerEntity){
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMarker(markerEntity)
        }
    }

    fun insertMark(marker: MarkerTimeRelation){
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMark(marker)
        }
    }

    fun insertLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.insert(labelEntity)
        }
    }

    fun updateLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.update(labelEntity)
        }
    }

    fun updateMarker(markerEntity: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarker(markerEntity)
        }
    }

    fun getRecordingFromIdInclMarks(uid : Int): List<RecordingAndMarker> {
        return markerDao.getRecordingFromIdInclMarks(uid)
    }

    fun getLabelById(labelEntity: LabelEntity): LiveData<LabelEntity> {
        return labelDao.getLabelById(labelEntity.uid)
    }
}

