package me.cpele.androcommut.autosuggest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ViewFlipper
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
                CustomApp.instance.navitiaService
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

        val queryEdit = view.findViewById<EditText>(R.id.autosuggest_search_edit)
        queryEdit.apply {
            addTextChangedListener {
                viewModel.dispatch(AutosuggestViewModel.Intention.QueryEdited(it))
            }
            setText(args.query)
            isFocusableInTouchMode = true
            requestFocus()
            setSelection(0)
            context?.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
        }

        val clearButton = view.findViewById<ImageButton>(R.id.autosuggest_clear_query)
        clearButton.setOnClickListener { queryEdit.setText("") }

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

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.autosuggest_refresh)
        refreshLayout?.setOnRefreshListener { refreshLayout.isRefreshing = false }

        view.findViewById<View>(R.id.autosuggest_retry_button)?.let {
            it.setOnClickListener {
                viewModel.dispatch(AutosuggestViewModel.Intention.QueryRetry)
            }
        }

        viewModel.stateLive.observe(viewLifecycleOwner) { state: AutosuggestViewModel.State ->
            render(state, adapter, clearButton, refreshLayout)
        }
    }

    private fun render(
        state: AutosuggestViewModel.State,
        adapter: AutosuggestAdapter,
        clearButton: ImageButton,
        refreshLayout: SwipeRefreshLayout
    ) {
        val viewFlipper = view?.findViewById<ViewFlipper>(R.id.autosuggest_view_flipper)
        when (val answer = state.answer) {
            is SuggestAnswerUiModel.Some -> {
                adapter.submitList(answer.places)
                viewFlipper?.displayedChild = 2
            }
            SuggestAnswerUiModel.None -> {
                viewFlipper?.displayedChild = 1
            }
            is SuggestAnswerUiModel.Fail -> {
                viewFlipper?.displayedChild = 0
            }
        }

        clearButton.visibility =
            if (state.isQueryClearable) View.VISIBLE
            else View.GONE
        refreshLayout.isRefreshing = state.isRefreshing
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