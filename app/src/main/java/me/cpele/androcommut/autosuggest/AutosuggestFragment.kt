package me.cpele.androcommut.autosuggest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.FlowPreview
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R

@FlowPreview
class AutosuggestFragment : Fragment() {

    companion object {
        fun newInstance() = AutosuggestFragment()
    }

    private val viewModel: AutosuggestViewModel by viewModels {
        ViewModelFactory {
            AutosuggestViewModel(
                CustomApp.instance.navitiaService,
                CustomApp.instance
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.autosuggest_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<EditText>(R.id.autosuggest_search_edit).addTextChangedListener {
            viewModel.dispatch(AutosuggestViewModel.Intention.QueryEdited(it))
        }

        val adapter = AutosuggestAdapter {
            findNavController().navigate(
                R.id.action_autosuggestFragment_to_originDestinationFragment,
                bundleOf(
                    "origin" to it.label,
                    "destination" to it.label
                )
            )
        }
        val recycler = view.findViewById<RecyclerView>(R.id.autosuggest_results_recycler)
        recycler.adapter = adapter

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state?.places)
        }
    }
}