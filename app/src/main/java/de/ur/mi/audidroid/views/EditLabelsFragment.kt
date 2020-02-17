package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.EditLabelsFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.EditLabelsViewModel
import java.lang.IllegalArgumentException

class EditLabelsFragment : Fragment() {

    private lateinit var viewModel: EditLabelsViewModel

    companion object {
        fun newInstance() = EditLabelsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        val dataSource = Repository(application)
        val binding: EditLabelsFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.edit_labels_fragment, container, false)

        val viewModelFactory = EditLabelsViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditLabelsViewModel::class.java)
        binding.editLabelsViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    class EditLabelsViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(EditLabelsViewModel::class.java)) {
                EditLabelsViewModel(dataSource, application) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

}
