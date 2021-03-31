package me.cpele.androcommut.autosuggest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.FlowPreview
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R

@FlowPreview
class AutosuggestFragment : Fragment() {

    private var listener: Listener? = null

    private val viewModel: AutosuggestViewModel by viewModels {
        ViewModelFactory {
            AutosuggestViewModel(
                CustomApp.instance.navitiaService,
                CustomApp.instance
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener
            ?: throw IllegalStateException("${context::class.qualifiedName} has to implement ${Listener::class.qualifiedName}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.autosuggest_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { AutosuggestFragmentArgs.fromBundle(it) }
            ?: throw IllegalStateException("Fragment args incorrect: $arguments")

        view.findViewById<EditText>(R.id.autosuggest_search_edit).also { editText ->
            editText.addTextChangedListener {
                viewModel.dispatch(AutosuggestViewModel.Intention.QueryEdited(it))
            }
            editText.setText(args.query)
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            context?.getSystemService<InputMethodManager>()
                ?.showSoftInput(editText, 0)
        }

        val adapter = AutosuggestAdapter { uiModel ->
            context?.getSystemService<InputMethodManager>()
                ?.hideSoftInputFromWindow(view.windowToken, 0)
            listener?.takeAutosuggestion(
                this,
                args.trigger,
                uiModel.id,
                uiModel.label
            )
        }
        val recycler = view.findViewById<RecyclerView>(R.id.autosuggest_results_recycler)
        recycler.adapter = adapter

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state?.places)
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun takeAutosuggestion(
            fragment: Fragment,
            trigger: AutosuggestTrigger,
            id: String,
            label: String
        )
    }
}