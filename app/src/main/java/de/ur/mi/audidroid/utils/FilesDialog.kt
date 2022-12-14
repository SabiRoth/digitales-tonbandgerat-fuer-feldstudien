package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.viewmodels.FilesViewModel

/**
 * Dialog to confirm deleting of recording.
 * @author Theresa Strohmeier
 */
object FilesDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        type: Int,
        recording: RecordingAndLabels? = null,
        viewModel: FilesViewModel? = null,
        errorMessage: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        if (type == R.string.confirm_dialog) {
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setMessage(
                    String.format(
                        context.getString(R.string.delete_recording_dialog_header),
                        recording!!.recordingName
                    )
                )
                setPositiveButton(context.getString(R.string.delete)) { _: DialogInterface, _: Int ->
                    viewModel?.deleteRecording(recording)
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _: DialogInterface, _: Int ->
                    viewModel?.cancelSaving()
                }
            }
        }
        dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
        dialog.setOnDismissListener {
            viewModel!!.cancelSaving()
        }
    }
}
