package me.cpele.androcommut.origdest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.R
import me.cpele.androcommut.origdest.OriginDestinationViewModel.Effect

class OriginDestinationFragment : Fragment() {

    companion object {
        fun newInstance() = OriginDestinationFragment()
    }

    private val viewModel: OriginDestinationViewModel by viewModels {
        ViewModelFactory {
            OriginDestinationViewModel(
                activity?.application
                    ?: throw IllegalStateException("Parent Activity of ${this::class.qualifiedName} should be attached")
            )
        }
    }

    private lateinit var originButton: Button
    private lateinit var destinationButton: Button
    private lateinit var instructionsText: TextView

    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.origin_destination_fragment, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as? Listener
            ?: throw IllegalStateException(
                "${context::class.qualifiedName} has to implement ${Listener::class.qualifiedName}"
            )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originButton = view.findViewById(R.id.od_origin_button)
        destinationButton = view.findViewById(R.id.od_destination_button)
        instructionsText = view.findViewById(R.id.od_instructions_text)

        val intention = OriginDestinationFragmentArgs
            .fromBundle(requireArguments())
            .let { args ->
                OriginDestinationViewModel.Intention.Load(
                    args.origin,
                    args.destination
                )
            }

        viewModel.dispatch(intention)

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            originButton.text = state?.origin
            destinationButton.text = state?.destination
            instructionsText.text = state?.instructions
        }

        viewModel.effectLive.observe(viewLifecycleOwner) { event ->
            event.consume { effect ->
                when (effect) {
                    is Effect.NavigateToAutosuggest.Origin ->
                        listener?.openAutosuggestOrigin(this)
                    is Effect.NavigateToAutosuggest.Destination ->
                        listener?.openAutosuggestDestination(this)
                }
            }
        }

        originButton.setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.OriginClicked)
        }

        destinationButton.setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.DestinationClicked)
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun openAutosuggestOrigin(fragment: Fragment)
        fun openAutosuggestDestination(fragment: Fragment)
    }
}