package de.ur.mi.audidroid.utils

import android.content.Context
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.viewmodels.FilesViewModel

/**
 * The [RenameDialog] for renaming recordings.
 * @author: Sabine Roth
 */

object RenameDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var editText: EditText

    fun createDialog(
        context: Context,
        viewModel: FilesViewModel,
        recording: RecordingAndLabels?,
        errorMessage: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        if (errorMessage != null) {
            builder.setMessage(errorMessage)
        }
        with(builder) {
            setTitle(
                context.getString(R.string.rename_dialog_title)
            )
            setView(R.layout.rename_dialog)
            setPositiveButton(context.getString(R.string.popup_menu_option_rename)) { _, _ ->
                viewModel.saveRename(recording!!, editText.text.toString())
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel.cancelRename()
            }
        }
        dialog = builder.create()
        dialog.setOnCancelListener {
            viewModel.cancelRename()
        }
        dialog.show()
        preFillEditText(recording)
    }

    private fun preFillEditText(recording: RecordingAndLabels?) {
        editText = dialog.findViewById(R.id.dialog_rename)!!
        editText.setText(recording?.recordingName)
        editText.showKeyboard()
        editText.setSelection(editText.text.length)
    }
}
