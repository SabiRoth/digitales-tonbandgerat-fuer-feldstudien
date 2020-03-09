package de.ur.mi.audidroid.views


import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.MarkItemAdapter
import androidx.fragment.app.Fragment
import de.ur.mi.audidroid.databinding.PlayerFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import kotlinx.android.synthetic.main.player_fragment.*

/**
 * The fragment allows the user to play a voice recording.
 * @author: Theresa Strohmeier
 */
class PlayerFragment : Fragment() {

    private lateinit var adapter: MarkItemAdapter
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var binding: PlayerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false)
        val application = this.activity!!.application
        val dataSource = Repository(application)

        val args = PlayerFragmentArgs.fromBundle(arguments!!)
        val viewModelFactory = PlayerViewModelFactory(args.recordingId, dataSource, application)

        playerViewModel = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)

        binding.playerViewModel = playerViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        playerViewModel.recording.observe(viewLifecycleOwner, Observer {
            it?.let {
                playerViewModel.recordingPath = it.recordingPath
                playerViewModel.initializeMediaPlayer()
                playerViewModel.initializeSeekBar(binding.seekBar)
                playerViewModel.initializeFrameLayout(player_layout)
            }
        })
        setupAdapter()
    }

    private fun setupAdapter() {

        adapter = MarkItemAdapter(playerViewModel)
        binding.markerList.adapter = adapter

        playerViewModel.getAllMarkers.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    /**
     * Provides the Repository, recordingId and context to the PlayerViewModel.
     */
    class PlayerViewModelFactory(
        private val recordingId: Int,
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(recordingId, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
