package de.ur.mi.audidroid.models

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class Repository(application: Application) : CoroutineScope {

    private var entryDao: EntryDao
    private var labelDao: LabelDao
    private var labelAssignmentDao: LabelAssignmentDao
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
        labelAssignmentDao = database.labelAssignmentDao()
        allRecordings = entryDao.getAllRecordings()
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }

    fun delete(entryEntity: EntryEntity) {
        DeleteAsyncTask(entryDao).execute(entryEntity)
    }

    private class DeleteAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.delete(params[0]!!)
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.delete(labelEntity)
        }
    }

    fun insert(entryEntity: EntryEntity): Long {
        return InsertAsyncTask(entryDao).execute(entryEntity).get()
    }

    private class InsertAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Long>() {

        override fun doInBackground(vararg params: EntryEntity?): Long {
            return entryDao.insert(params[0]!!)
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

    fun getRecordingWithId(entryEntity: EntryEntity) {
        GetRecordingWithId(entryDao).execute(entryEntity)
    }

    private class GetRecordingWithId(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.getRecordingWithId(params[0]!!.uid)
        }
    }

    fun getLabelById(uid: Int): LiveData<LabelEntity> {
        return labelDao.getLabelById(uid)
    }

    //TODO: Question to everyone: Does anybody knows why the call beneath doesn't work?
/*   fun insertRecLabels(labelAssignment: LabelAssignmentEntity){
        CoroutineScope(coroutineContext).launch {
            labelAssignmentDao.insertRecLabels(labelAssignment)
        }
    }*/

    fun insertRecLabels(labelAssignment: LabelAssignmentEntity) {
        InsertLabelAssignmentAsyncTask(labelAssignmentDao).execute(labelAssignment)
    }

    private class InsertLabelAssignmentAsyncTask(val labelAssignmentDao: LabelAssignmentDao) :
        AsyncTask<LabelAssignmentEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: LabelAssignmentEntity?) {
            return labelAssignmentDao.insertRecLabels(params[0]!!)
        }
    }

}
