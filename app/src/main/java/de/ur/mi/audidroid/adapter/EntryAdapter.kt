package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.databinding.EntryItemBinding
import de.ur.mi.audidroid.viewmodels.FilesViewModel

/**
 * ViewModel for PlayerFragment.
 * @author
 */
class EntryAdapter(private val filesViewModel: FilesViewModel) :
    ListAdapter<EntryEntity, EntryAdapter.ViewHolder>(RecordingDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    val userActionsListener = object : RecordingUserActionsListener {
        override fun onRecordingClicked(entryEntity: EntryEntity) {
            filesViewModel.onRecordingClicked(entryEntity.recordingPath)
        }

        override fun onButtonClicked(entryEntity: EntryEntity, view: View) {
            filesViewModel.onButtonClicked(entryEntity, view)
        }
    }

    class ViewHolder private constructor(private val binding: EntryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: EntryEntity,
            listener: RecordingUserActionsListener
        ) {
            binding.recording = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EntryItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class RecordingDiffCallback : DiffUtil.ItemCallback<EntryEntity>() {

    override fun areItemsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem == newItem
    }
}