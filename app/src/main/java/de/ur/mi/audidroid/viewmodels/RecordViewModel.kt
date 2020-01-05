package de.ur.mi.audidroid.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecorderDatabase
import de.ur.mi.audidroid.models.EntryEntity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RecordViewModel : ViewModel() {

    private var isRecording = false
    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var outputFile = ""
    private lateinit var db: RecorderDatabase
    private lateinit var timer: Chronometer
    private var currentRecordTime: String = ""


    fun initializeRecorder(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(context as Activity, permissions, 0)
            //TODO: check what happens if the permission is denied -> maybe a popup and closing the app?
        }

        outputFile =
            context.getFilesDir().getAbsolutePath() + "/" + "recording" + ".aac" //TODO: Change path to users preferred save location
        with (mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)}

        try {
            mediaRecorder.prepare()
        } catch (e: IllegalStateException) {
        } catch (e: IOException) {
        }
    }

    fun recordPauseButtonClicked(button: ImageButton) =
        when (!isRecording) {
            true -> {
                button.setImageResource(R.mipmap.pause_button_foreground)
                isRecording = true
                if (!resumeRecord) {
                    resetTimer()
                    timer.start()
                    startRecording()
                } else {
                    timer.base = SystemClock.elapsedRealtime() - getStoppedTime()
                    timer.start()
                    resumeRecording()
                }
            }

            false -> {
                button.setImageResource(R.mipmap.record_button_foreground)
                isRecording = false
                resumeRecord = true
                pauseRecording()
                timer.stop()
                currentRecordTime = timer.text.toString()
            }
        }

    private fun startRecording(){
        mediaRecorder.start()
    }

    private fun pauseRecording(){
        mediaRecorder.pause()
    }

    private fun resumeRecording(){
        mediaRecorder.resume()
    }

    fun cancelRecord(context: Context){
        isRecording = false
        resumeRecord = false
        mediaRecorder.reset()
        resetTimer()
        sendToast(context, R.string.record_removed)
    }

    fun confirmRecord(context: Context){
        isRecording = false
        resumeRecord = false
        mediaRecorder.stop()
        mediaRecorder.reset()
        resetTimer()
        sendToast(context, R.string.record_saved)
        getLastUID(context)
    }

    private fun getLastUID(context: Context){
        db = RecorderDatabase.getInstance(context)
        doAsync {
            val count =  db.entryDao().getRowCount()
            uiThread{
                saveRecordInDB(count)
            }
        }
    }

    private fun saveRecordInDB(count: Int) {
        val audio =
            EntryEntity(count, outputFile, getDate())
        doAsync{
            db.entryDao().insert(audio)
        }
    }

    private fun getDate() : String{
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    private fun sendToast(context: Context, text: Int){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    /** Returns the last stopped time as an Integer value */
    private fun getStoppedTime(): Int {
        val timeArray = currentRecordTime.split(":")
        return if (timeArray.size == 2) {
            (Integer.parseInt(timeArray[0]) * 60 * 1000) + (Integer.parseInt(timeArray[1]) * 1000)
        } else {
            (Integer.parseInt(timeArray[0]) * 60 * 60 * 1000) + (Integer.parseInt(timeArray[1]) * 60 * 1000) + (Integer.parseInt(
                timeArray[2]
            ) * 1000)
        }
    }

    /** Resets timer to 00:00 */
    private fun resetTimer(){
        timer.stop()
        timer.base = SystemClock.elapsedRealtime()
    }
}
