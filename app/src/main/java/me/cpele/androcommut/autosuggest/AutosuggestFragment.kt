package me.cpele.androcommut.autosuggest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.FlowPreview
import me.cpele.afk.CustomApp
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.FakeNavitiaService
import me.cpele.androcommut.R

@FlowPreview
class AutosuggestFragment : Fragment() {

    companion object {
        fun newInstance() = AutosuggestFragment()
    }

    private lateinit var viewModel: AutosuggestViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.autosuggest_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { AutosuggestViewModel(FakeNavitiaService(), CustomApp.instance) }
        ).get(
            AutosuggestViewModel::class.java
        )

        view.findViewById<EditText>(R.id.autosuggest_search_edit).addTextChangedListener {
            viewModel.dispatch(AutosuggestViewModel.Intention.QueryEdited(it))
        }

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            Log.d(javaClass.simpleName, "State: $state")
        }
    }
}