package de.ur.mi.audidroid.views

import android.app.Application
import android.content.ClipDescription
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.MenuInflater
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.RecordingAndFolderActionsListener
import de.ur.mi.audidroid.adapter.RecordingAndFolderAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FilesDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.utils.FilterDialog
import de.ur.mi.audidroid.utils.RenameDialog
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.files_fragment.folder_back_target
import kotlinx.android.synthetic.main.files_fragment.files_layout

/**
 * The FilesFragment displays all recordings and folders.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {

    private lateinit var adapter: RecordingAndFolderAdapter
    private lateinit var binding: FilesFragmentBinding
    private lateinit var filesViewModel: FilesViewModel
    private lateinit var dataSource: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)

        val application: Application = requireActivity().application

        dataSource = Repository(application)

        val viewModelFactory = FilesViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, viewModelFactory).get(FilesViewModel::class.java)

        binding.filesViewModel = filesViewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        /**
         *  Observer on the state variable for showing SnackBar message when a list-item is deleted.
         */
        filesViewModel.allRecordingsWithMarker.observe(viewLifecycleOwner, Observer {})

        filesViewModel.sortMode.observe(viewLifecycleOwner, Observer {
            filesViewModel.setSorting(it)
        })

        filesViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(requireView(), R.string.recording_deleted, Snackbar.LENGTH_SHORT)
                    .show()
                filesViewModel.doneShowingSnackbar()
            }
        })

        filesViewModel.navigateToPlayerFragment.observe(
            viewLifecycleOwner,
            Observer { recordingId: MutableList<String> ->
                recordingId.let {
                    this.findNavController().navigate(
                        FilesFragmentDirections
                            .actionFilesToPlayer(
                                recordingId[0].toInt(),
                                recordingId[1],
                                recordingId[2]
                            )
                    )
                    filesViewModel.onPlayerFragmentNavigated()
                }
            })

        filesViewModel.createAlertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                ConvertDialog.createDialog(
                    context = requireContext(),
                    layoutId = R.layout.convert_dialog,
                    viewModel = filesViewModel
                )
            }
        })

        filesViewModel._currentlyInFolder.value = false

        return binding.root
    }

    fun openRecordingPopupMenu(recordingAndLabels: RecordingAndLabels, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_rename_recording ->
                    filesViewModel.rename(recordingAndLabels)
                R.id.action_delete_recording ->
                    filesViewModel.delete(recordingAndLabels)
                R.id.action_edit_recording ->
                    navigateToEditFragment(recordingAndLabels)
                R.id.action_share_recording -> {
                    filesViewModel.recordingToBeExported = recordingAndLabels
                    filesViewModel._createAlertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_files, menu)

        val searchItem: MenuItem = menu.findItem(R.id.action_search)

        val searchView: SearchView = searchItem.actionView as SearchView
        val sortItem: MenuItem = menu.findItem(R.id.action_sort)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                sortItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                sortItem.isVisible = true
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) {
                    filesViewModel.setSearchResult(newText)
                } else {
                    filesViewModel._sortMode.value = null
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                filesViewModel._createFilterDialog.value = true
                true
            }
            R.id.action_sort_name_asc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_name_asc
                true
            }
            R.id.action_sort_name_desc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_name_desc
                true
            }
            R.id.action_sort_date_asc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_date_asc
                true
            }
            R.id.action_sort_date_desc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_date_desc
                true
            }
            R.id.action_sort_duration_asc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_duration_asc
                true
            }
            R.id.action_sort_duration_desc -> {
                filesViewModel._sortMode.value = R.integer.sort_by_duration_desc
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToEditFragment(recordingAndLabels: RecordingAndLabels) {
        this.findNavController().navigate(
            FilesFragmentDirections.actionFilesToEdit(
                recordingAndLabels.uid,
                recordingAndLabels.recordingName,
                recordingAndLabels.recordingPath
            )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filesViewModel.initializeFrameLayout(files_layout)
        filesViewModel.setSorting(null)
        setupAdapter()
        adaptObservers()
        FilterDialog.clearDialog()
    }

    private fun setupAdapter() {
        adapter = RecordingAndFolderAdapter(requireContext(), filesViewModel, userActionsListener)
        binding.recordingList.adapter = adapter

        filesViewModel.displayRecordingsAndFolders.observe(viewLifecycleOwner, Observer {
            it?.let {
                val filterEntries: List<Any> = filesViewModel.getCorrectList(it)
                adapter.submitList(filterEntries)
            }
        })
    }

    private fun adaptObservers() {
        filesViewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilesDialog.createDialog(
                    context = requireContext(),
                    type = R.string.confirm_dialog,
                    recording = filesViewModel.recording,
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage
                )
            }
        })

        filesViewModel.createFilterDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilterDialog.createDialog(
                    context = requireContext(),
                    layoutId = R.layout.filter_dialog,
                    viewModel = filesViewModel,
                    dataSource = dataSource,
                    fragment = this
                )
            }
        })

        filesViewModel.folderDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FolderDialog.createDialog(
                    context = requireContext(),
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage,
                    layoutId = R.layout.folder_dialog_create,
                    folderToBeEdited = filesViewModel.folderToBeEdited,
                    deleteFolder = filesViewModel.deleteFolder
                )
            }
        })

        filesViewModel.createRenameDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                RenameDialog.createDialog(
                    context = requireContext(),
                    viewModel = filesViewModel,
                    recording = filesViewModel.recording,
                    errorMessage = filesViewModel.errorMessage
                )
            }
        })

        filesViewModel.currentlyInFolder.observe(viewLifecycleOwner, Observer {
            if (it) {
                folder_back_target.setOnDragListener { v: View, event: DragEvent ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED -> {
                            if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                                v.setBackgroundColor(Color.TRANSPARENT)
                                v.invalidate()
                                true
                            } else {
                                false
                            }
                        }
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            v.setBackgroundColor(requireContext().getColor(R.color.color_primary))
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DRAG_LOCATION ->
                            true
                        DragEvent.ACTION_DRAG_EXITED -> {
                            v.setBackgroundColor(Color.TRANSPARENT)
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DROP -> {
                            filesViewModel.removeRecordingFromFolder(filesViewModel.recordingToBeMoved!!)
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DRAG_ENDED -> {
                            v.setBackgroundColor(Color.TRANSPARENT)
                            v.invalidate()
                            true
                        }
                        else -> false
                    }
                }
            }
        })

        filesViewModel.deleteRecordings.observe(viewLifecycleOwner, Observer {
        })
    }

    private val userActionsListener: RecordingAndFolderActionsListener =
        object : RecordingAndFolderActionsListener {
            override fun onRecordingClicked(recordingAndLabels: RecordingAndLabels) {
                filesViewModel.onRecordingClicked(
                    recordingAndLabels.uid,
                    recordingAndLabels.recordingName,
                    recordingAndLabels.recordingPath
                )
            }

            override fun popUpRecording(recordingAndLabels: RecordingAndLabels, view: View) {
                openRecordingPopupMenu(recordingAndLabels, view)
            }

            override fun onFolderClicked(folder: FolderEntity) {
                filesViewModel.onFolderClicked(folder)
            }

            override fun popUpFolder(folder: FolderEntity, view: View) {
                filesViewModel.openFolderMenu(folder, view)
            }
        }

    override fun onPause() {
        filesViewModel._currentlyInFolder.value = false
        super.onPause()
    }

    /**
     * Provides the Repository and context to the FilesViewModel.
     */
    class FilesViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                return FilesViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
